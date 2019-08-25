package com.david.calltraker

import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.david.calltraker.Adaptor.ListViewAdaptor
import com.david.calltraker.Adaptor.SmsViewAdaptor
import com.david.calltraker.Entity.CallHistoryEntity
import com.david.calltraker.Entity.SmsEntity
import com.david.calltraker.Utils.Constants
import kotlinx.android.synthetic.main.activity_home.*

import kotlinx.android.synthetic.main.activity_sms.*
import kotlinx.android.synthetic.main.activity_sms.fab
import kotlinx.android.synthetic.main.activity_sms.toolbar
import kotlinx.android.synthetic.main.content_home.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SmsActivity : AppCompatActivity() {

    private val unknownCalls: ArrayList<SmsEntity> = ArrayList()
    private var studentDatabase: CallHistoryAppDB? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms)
        setSupportActionBar(toolbar)
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true);
        studentDatabase = CallHistoryAppDB.getInstance(this)
        call_list.layoutManager = LinearLayoutManager(applicationContext)
        call_list.adapter = SmsViewAdaptor(unknownCalls, this@SmsActivity,studentDatabase)

        createDB()

        fab.setOnClickListener { view ->
            showCreateCategoryDialog()
        }
    }
    fun createDB() {
        AsyncTask.execute {

            try {
                val callDetails = studentDatabase!!.SmsEntityDao().getAll()
                val obj_adapter = SmsViewAdaptor(callDetails, this@SmsActivity,studentDatabase)
                call_list.adapter = obj_adapter
            } catch (e: Exception) {
                val s = e.message;
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu to use in the action bar
        val inflater = menuInflater
        inflater.inflate(R.menu.activity_sms, menu)

        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        finish()
        return super.onOptionsItemSelected(item)
    }

    fun showCreateCategoryDialog() {
        val context = this
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Add SMS Conent")

        // https://stackoverflow.com/questions/10695103/creating-custom-alertdialog-what-is-the-root-view
        // Seems ok to inflate view with null rootView
        val view = layoutInflater.inflate(R.layout.add_sms_view, null)

        val categoryEditText = view.findViewById(R.id.categoryEditText) as EditText

        builder.setView(view);

        // set up the ok button
        builder.setPositiveButton(android.R.string.ok) { dialog, p1 ->
            val newCategory = categoryEditText.text
            var isValid = true
            if (newCategory.isBlank()) {
                categoryEditText.error = "Please enter message"
                isValid = false
            }

            if (isValid) {

                val db = Room.databaseBuilder(
                    this@SmsActivity,
                    CallHistoryAppDB::class.java, Constants.DB_NAME
                ).build()

                var callHistory = SmsEntity(
                    id = null,
                    message = newCategory.toString(),
                    status = "0"
                );
                AsyncTask.execute {

                    studentDatabase!!.SmsEntityDao().insertAll(callHistory)

                }
                studentDatabase = CallHistoryAppDB.getInstance(this)
                call_list.layoutManager = LinearLayoutManager(applicationContext)
                call_list.adapter = SmsViewAdaptor(unknownCalls, this@SmsActivity,studentDatabase)
                createDB()


            }

            if (isValid) {
                dialog.dismiss()
            }
        }

        builder.setNegativeButton(android.R.string.cancel) { dialog, p1 ->
            dialog.cancel()
        }

        builder.show();
    }

}
