package com.david.calltraker.Adaptor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.david.calltraker.DAO.CallHistoryDao
import com.david.calltraker.Entity.CallHistoryEntity
import com.david.calltraker.R;
import kotlinx.android.synthetic.main.list_view.view.*
import android.widget.Toast
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat.startActivity
import android.net.Uri
import android.os.AsyncTask
import androidx.core.content.ContextCompat.startActivity
import androidx.room.Room
import com.david.calltraker.CallHistoryAppDB
import com.david.calltraker.Entity.SmsEntity
import com.david.calltraker.Utils.Constants
import kotlinx.android.synthetic.main.list_sms.view.*


class SmsViewAdaptor(val userList: List<SmsEntity>, val context: Context, val studentDatabase:CallHistoryAppDB?) : RecyclerView.Adapter<SmsViewAdaptor.ViewHolder>() {


    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmsViewAdaptor.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_sms, parent, false)
        return ViewHolder(v)

    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: SmsViewAdaptor.ViewHolder, position: Int) {
        holder.bindItems(userList[position],context,studentDatabase)
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return userList.size
    }

    //the class is hodling the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(user: SmsEntity,context: Context,studentDatabase: CallHistoryAppDB?) {
            itemView.message.text=user.message
            if(user.status.equals("1")){
                itemView.msgSelection.isChecked=true;
            }
            itemView.msgSelection.setOnClickListener(View.OnClickListener {

                var select=0;
                if(itemView.msgSelection.isChecked)
                     select=1
                else
                     select=0

                val db = Room.databaseBuilder(
                    context,
                    CallHistoryAppDB::class.java, Constants.DB_NAME
                ).build()

                AsyncTask.execute {
                    var  studentDatabase = CallHistoryAppDB.getInstance(context)
                    user.status=select.toString();
                    studentDatabase!!.SmsEntityDao().updateTodo(user)
                }
                if(select==1)
                    Toast.makeText(context,"Selected",Toast.LENGTH_LONG).show()
                else
                    Toast.makeText(context,"Removed",Toast.LENGTH_LONG).show()

            })

        }
    }

}