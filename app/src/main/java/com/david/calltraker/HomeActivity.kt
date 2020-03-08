package com.david.calltraker

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentProviderOperation
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.provider.CallLog
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.david.calltraker.Adaptor.ListViewAdaptor
import com.david.calltraker.Entity.CallHistoryEntity
import com.david.calltraker.Entity.SmsEntity

import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.content_home.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import android.widget.ProgressBar
import com.david.calltraker.Utils.Constants


class HomeActivity : AppCompatActivity() {

    private val unknownCalls: ArrayList<CallHistoryEntity> = ArrayList()
    private val smsLists: ArrayList<SmsEntity> = ArrayList()
    private var studentDatabase: CallHistoryAppDB? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(toolbar)
        studentDatabase = CallHistoryAppDB.getInstance(this)
        call_list.layoutManager = LinearLayoutManager(applicationContext)
        call_list.adapter = ListViewAdaptor(unknownCalls,smsLists, this@HomeActivity,studentDatabase)

        createDB()
        fab.setOnClickListener { view ->
            saveUnsavedFromCallLog()
        }
    }

    fun dateTimeToMillis(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE,-1)
        calendar.set(Calendar.HOUR, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        return calendar.getTimeInMillis().toString()
    }

    class saveTask(mobile:ArrayList<String>,ctx:Context) : AsyncTask<Void, Void, String>() {
        var mobileArr=mobile
        var context=ctx
        var progress=ProgressDialog(ctx)
        override fun doInBackground(vararg params: Void?): String? {
            var i=0;
            for(index in mobileArr)
            {
                var home=HomeActivity()
                home.saveContactIntiate(context,index,"")
                i++;
            }
            return ""
        }

        override fun onPreExecute() {
            super.onPreExecute()
            progress.setMessage("Please wailt...")
            progress.show()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            progress.dismiss()
            val i = Intent(context, HomeActivity::class.java)
            context!!.startActivity(i)
            (context!! as Activity).overridePendingTransition(0, 0);
            (context!! as Activity).finish()
        }
    }

    fun saveUnsavedFromCallLog()
    {
        try
        {

            var mobileArr=ArrayList<String>();
            val cursor = getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                null,
                CallLog.Calls.DATE + " > ?" , Array<String>(1){dateTimeToMillis()},
                CallLog.Calls.DATE + " DESC");
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if(cursor.getString(cursor.getColumnIndex("name"))==null || cursor.getString(cursor.getColumnIndex("name")).toString().trim().equals("")) {
                        mobileArr.add(cursor.getString(
                            cursor.getColumnIndex("number")));

                    }
                }
                if(mobileArr.size>0)
                {
                    for(index in mobileArr)
                    {
                        saveContactIntiate(this@HomeActivity,index,"")
                    }
                }
                val i = Intent(this@HomeActivity, HomeActivity::class.java)
                startActivity(i)
                overridePendingTransition(0, 0);
                finish()
            }

        }
        catch(e:Exception)
        {

        }
    }
    fun saveContactIntiate(ctx:Context,number:String,name:String)
    {
        try {
            if(!number.equals("null") && !number.isNullOrEmpty()) {
                if (!contactExists(ctx, number)) {
                    val db = Room.databaseBuilder(
                        ctx,
                        CallHistoryAppDB::class.java, Constants.DB_NAME
                    ).build()
                    val inputDateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy")
                    val outputDateFormat = SimpleDateFormat("dd MMM yyyy hh:mm a")
                    val outputDateFormat1 = SimpleDateFormat("MMM dd yyyy-HHmmss")
                    var callHistory = CallHistoryEntity(
                        id = null,
                        name = outputDateFormat1.format(Date()).split("-")[0] + " Enquiry " + outputDateFormat1.format(
                            Date()
                        ).split("-")[1],
                        mobileNo = number,
                        callStatus = "1",
                        callDate = outputDateFormat.format(Date()),
                        count = "1",
                        message = "0"
                    );
                    AsyncTask.execute {
                        saveContact(ctx, callHistory.name, number)
                        studentDatabase!!.callHistoryDao().insertAll(callHistory)
                    }
                }
            }
        }
        catch (e:java.lang.Exception)
        {
            Log.d("",e.message)
        }
    }
    fun contactExists(ctx: Context, number: String?): Boolean {
        if (number != null) {
            val lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
            val mPhoneNumberProjection = arrayOf(ContactsContract.PhoneLookup._ID, ContactsContract.PhoneLookup.NUMBER, ContactsContract.PhoneLookup.DISPLAY_NAME)
            val cur = ctx.contentResolver.query(lookupUri, mPhoneNumberProjection, null, null, null)
            try {
                if (cur!!.moveToFirst()) {
                    return true
                }
            } finally {
                cur?.close()
            }
            return false
        } else {
            return false
        }
    }// contactExists
    fun saveContact(context: Context, name:String, number: String)
    {
        val ops = java.util.ArrayList<ContentProviderOperation>()

        ops.add(
            ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        //------------------------------------------------------ Names
        ops.add(
            ContentProviderOperation.newInsert(
                ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(
                    ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                    name).build());
        ops.add(
            ContentProviderOperation.
                newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                .withValue(
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                .build());
        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops)
            //Toast.makeText(context, "Contact Saved", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            //Toast.makeText(context, "Contact not Saved", Toast.LENGTH_SHORT).show()
        }

    }

    fun createDB() {
        AsyncTask.execute {

            try {
                val callDetails = studentDatabase!!.callHistoryDao().getAll()
                val smsList=studentDatabase!!.SmsEntityDao().getAll()
                val obj_adapter = ListViewAdaptor(callDetails,smsList, this@HomeActivity,studentDatabase)
                call_list.adapter = obj_adapter
            } catch (e: Exception) {
                val s = e.message;
            }
        }

    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu to use in the action bar
        val inflater = menuInflater
        inflater.inflate(R.menu.activity_home, menu)

        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.delote -> {
                // Initialize a new instance of
                val builder = AlertDialog.Builder(this@HomeActivity)

                // Set the alert dialog title
                builder.setTitle(getString(R.string.app_name))

                // Display a message on alert dialog
                builder.setMessage("Are you sure you want to delete records?")

                // Set a positive button and its click listener on alert dialog
                builder.setPositiveButton("YES"){dialog, which ->
                    AsyncTask.execute {

                        try {
                            val callDetails = studentDatabase!!.callHistoryDao().delete()
                            val callDetails1 = studentDatabase!!.callHistoryDao().getAll()
                            val smsList=studentDatabase!!.SmsEntityDao().getAll()
                            val obj_adapter = ListViewAdaptor(callDetails1,smsList, this@HomeActivity,studentDatabase)
                            call_list.adapter = obj_adapter
                        } catch (e: Exception) {
                            val s = e.message;
                        }
                    }

                }


                // Display a negative button on alert dialog
                builder.setNegativeButton("No"){dialog,which ->
                    Toast.makeText(applicationContext,"You are not agree.",Toast.LENGTH_SHORT).show()
                }

                // Finally, make the alert dialog using builder
                val dialog: AlertDialog = builder.create()

                // Display the alert dialog on app interface
                dialog.show()
            }
            R.id.settings -> {

                val i = Intent(this@HomeActivity, SmsActivity::class.java)
                startActivity(i)
                this.overridePendingTransition(0, 0);
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        CallHistoryAppDB.destroyInstance()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }

}
