package com.david.calltraker

import android.app.AlertDialog
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
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
import com.david.calltraker.Utils.Constants
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.content_home.*

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
            val i = Intent(this@HomeActivity, HomeActivity::class.java)
            startActivity(i)
            this.overridePendingTransition(0, 0);
            finish()
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
