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
import com.david.calltraker.Utils.Constants
import android.content.DialogInterface
import android.app.AlertDialog
import android.telephony.SmsManager
import com.david.calltraker.Entity.SmsEntity


class ListViewAdaptor(val userList: List<CallHistoryEntity>,val smsList:List<SmsEntity>,val context: Context,val studentDatabase:CallHistoryAppDB?) : RecyclerView.Adapter<ListViewAdaptor.ViewHolder>() {


    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewAdaptor.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_view, parent, false)
        return ViewHolder(v)

    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: ListViewAdaptor.ViewHolder, position: Int) {
        holder.bindItems(userList[position],smsList,context,studentDatabase)
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return userList.size
    }

    //the class is hodling the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(user: CallHistoryEntity, smsList: List<SmsEntity>, context: Context, studentDatabase: CallHistoryAppDB?) {
            itemView.name.text=user.name+" ("+user.count+")"
            itemView.mobileNumber.text=user.mobileNo
            itemView.date.text=user.callDate
            if(user.message.equals("1"))
            itemView.ListPanel.setBackgroundColor(context.resources.getColor(R.color.selected))
            itemView.whatsAppMsg.setOnClickListener {
                val pm = context.getPackageManager()
                try {

                    val uri = Uri.parse("smsto:${user.mobileNo}")
                    val i = Intent(Intent.ACTION_SENDTO, uri)
                    i.setPackage("com.whatsapp.w4b")
                    context.startActivity(Intent.createChooser(i, ""))
                    user.message="1"
                    AsyncTask.execute {
                        studentDatabase!!.callHistoryDao().updateTodo(user)
                    }
                    itemView.ListPanel.setBackgroundColor(context.resources.getColor(R.color.selected))

                } catch (e:Exception) {
                    Toast.makeText(context, "WhatsApp Business not Installed", Toast.LENGTH_SHORT)
                        .show()
                }

            }
            itemView.call.setOnClickListener {
                try {
                val intent = Intent(Intent.ACTION_CALL)
                intent.data = Uri.parse("tel:" + user.mobileNo)
                context.startActivity(intent)
                    user.message="1"
                    AsyncTask.execute {
                        studentDatabase!!.callHistoryDao().updateTodo(user)
                    }
                    itemView.ListPanel.setBackgroundColor(context.resources.getColor(R.color.selected))
                } catch (e: PackageManager.NameNotFoundException) {

                }
            }
            itemView.smsMsg.setTag(user.mobileNo.toString())
            itemView.smsMsg.setOnClickListener(View.OnClickListener {

                val selectedSms: ArrayList<String> = ArrayList()
                var list:String="";
                for (i in smsList.indices){

                    if(list.equals(""))
                        list=smsList.get(i).message
                    else
                        list=list+","+smsList.get(i).message
                }
                val builder = AlertDialog.Builder(context)
                builder.setTitle("SMS")
                builder.setMultiChoiceItems(
                    list.split(",").toTypedArray(),null,
                    DialogInterface.OnMultiChoiceClickListener { dialog, which, isChecked ->

                        if(isChecked)
                            selectedSms.add(smsList[which].message)
                        else
                            selectedSms.remove(smsList[which].message)

                    })

                builder.setPositiveButton("Done", DialogInterface.OnClickListener {

                        dialog, which ->
                    val smsManager = SmsManager.getDefault() as SmsManager
                    for (i in smsList.indices){
                        smsManager.sendTextMessage(it.getTag().toString(), null, selectedSms.get(i).toString(), null, null)
                    }

                    dialog.dismiss()



                })

                val dialog = builder.create()
                dialog.show()

            })

        }
    }

}