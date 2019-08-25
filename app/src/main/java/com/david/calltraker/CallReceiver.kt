package com.david.calltraker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Handler
import android.provider.CallLog
import android.telephony.TelephonyManager
import java.util.*
import androidx.core.app.NotificationCompat.getExtras
import androidx.room.Room
import com.david.calltraker.Entity.CallHistoryEntity
import com.david.calltraker.Utils.Constants
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.Contacts.Entity.RAW_CONTACT_ID
import android.content.ContentProviderOperation
import android.widget.Toast
import java.text.SimpleDateFormat
import android.provider.ContactsContract.PhoneLookup
import android.net.Uri.withAppendedPath
import android.app.Activity
import android.net.Uri
import android.telephony.SmsManager


class CallReceiver : BroadcastReceiver() {

    private var lastState = TelephonyManager.CALL_STATE_IDLE
    private var callStartTime: Date = Date()
    private var isIncoming: Boolean = false
    private var savedNumber: String = ""  //because the passed incoming is only valid in ringing
    private var studentDatabase:CallHistoryAppDB? = null
    override fun onReceive(context: Context, intent: Intent) {
        //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
        if (intent.action.equals("android.intent.action.NEW_OUTGOING_CALL")) {
            savedNumber = intent.extras?.getString("android.intent.extra.PHONE_NUMBER").toString()
        } else {
            val stateStr = intent.extras?.getString(TelephonyManager.EXTRA_STATE)
            val number = intent.extras?.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)
            var state = 0
            if (stateStr == TelephonyManager.EXTRA_STATE_IDLE) {
                state = TelephonyManager.CALL_STATE_IDLE
            } else if (stateStr == TelephonyManager.EXTRA_STATE_OFFHOOK) {
                state = TelephonyManager.CALL_STATE_OFFHOOK
            } else if (stateStr == TelephonyManager.EXTRA_STATE_RINGING) {
                state = TelephonyManager.CALL_STATE_RINGING
            }


            onCallStateChanged(context, state, number.toString())
        }
    }
    fun contactExists(_activity: Context, number: String?): Boolean {
        if (number != null) {
            val lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
            val mPhoneNumberProjection = arrayOf(PhoneLookup._ID, PhoneLookup.NUMBER, PhoneLookup.DISPLAY_NAME)
            val cur = _activity.contentResolver.query(lookupUri, mPhoneNumberProjection, null, null, null)
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

    fun getContactName(phoneNumber: String, context: Context): String {
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))

        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

        var contactName = ""
        val cursor = context.contentResolver.query(uri, projection, null, null, null)

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(0)
            }
            cursor.close()
        }

        return contactName
    }

    //Derived classes should override these to respond to specific events of interest
    protected fun onIncomingCallStarted(ctx: Context, number: String, start: Date) {

        studentDatabase = CallHistoryAppDB.getInstance(ctx)
        if(!number.equals("null") && !number.isNullOrEmpty()) {
            if(!contactExists(ctx,number)) {
                val db = Room.databaseBuilder(
                    ctx,
                    CallHistoryAppDB::class.java, Constants.DB_NAME
                ).build()
                val inputDateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy")
                val outputDateFormat = SimpleDateFormat("dd MMM yyyy hh:mm a")
                val outputDateFormat1 = SimpleDateFormat("MMM dd yyyy-HHmmss")
                var callHistory = CallHistoryEntity(
                    id = null,
                    name = outputDateFormat1.format(Date()).split("-")[0]+" Enquiry "+outputDateFormat1.format(Date()).split("-")[1],
                    mobileNo = number,
                    callStatus = "1",
                    callDate = outputDateFormat.format(inputDateFormat.parse(start.toString())),
                    count="1",
                    message = "0"
                );
                AsyncTask.execute {
                    saveContact(ctx,callHistory.name, number)
                    studentDatabase!!.callHistoryDao().insertAll(callHistory)

                }
            }
            else
            {
                val db = Room.databaseBuilder(
                    ctx,
                    CallHistoryAppDB::class.java, Constants.DB_NAME
                ).build()

                AsyncTask.execute {
                    var callHistory=studentDatabase!!.callHistoryDao().getSelect(number)
                    if(callHistory!=null) {
                        var count: Int = callHistory.count.toString().toInt();
                        count++;
                        callHistory.count = count.toString();
                        callHistory.message = "0"
                        studentDatabase!!.callHistoryDao().updateTodo(callHistory)
                    }
                    else
                    {
                        var name=getContactName(number,ctx);
                        if(!name.equals(""))
                        {
                            val db = Room.databaseBuilder(
                                ctx,
                                CallHistoryAppDB::class.java, Constants.DB_NAME
                            ).build()
                            val inputDateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy")
                            val outputDateFormat = SimpleDateFormat("dd MMM yyyy hh:mm a")
                            val outputDateFormat1 = SimpleDateFormat("MMM dd yyyy-HHmmss")
                            var callHistory = CallHistoryEntity(
                                id = null,
                                name = name,
                                mobileNo = number,
                                callStatus = "1",
                                callDate = outputDateFormat.format(inputDateFormat.parse(start.toString())),
                                count="1",
                                message = "0"
                            );
                            AsyncTask.execute {
                                saveContact(ctx,callHistory.name, number)
                                studentDatabase!!.callHistoryDao().insertAll(callHistory)

                            }
                        }
                    }
                }
            }

            sendSms(number);
        }
        CallHistoryAppDB.destroyInstance()

    }
    fun sendSms(number: String)
    {
        AsyncTask.execute {
            val smsList=studentDatabase!!.SmsEntityDao().getSelect()
            if(smsList.size>0)
            {
                val smsManager = SmsManager.getDefault() as SmsManager
                for (i in smsList.indices){
                    smsManager.sendTextMessage(number, null, smsList.get(i).message, null, null)
                }
            }

        }
    }
    fun saveContact(context: Context,name:String,number: String)
    {
        val ops = ArrayList<ContentProviderOperation>()

        ops.add(ContentProviderOperation.newInsert(
            ContactsContract.RawContacts.CONTENT_URI)
            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
            .build());

        //------------------------------------------------------ Names
        ops.add(ContentProviderOperation.newInsert(
            ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            .withValue(
                ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                name).build());
        ops.add(ContentProviderOperation.
            newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
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
    protected fun onOutgoingCallStarted(ctx: Context, number: String, start: Date) {


    }
    protected fun onIncomingCallEnded(ctx: Context, number: String, start: Date, end: Date) {


    }
    protected fun onOutgoingCallEnded(ctx: Context, number: String, start: Date, end: Date) {}
    protected fun onMissedCall(ctx: Context, number: String, start: Date) {

    }

    //Deals with actual events

    //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
    //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
    fun onCallStateChanged(context: Context, state: Int, number: String) {
        if(lastState == state){
            //No change, debounce extras
            return;
        }
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                isIncoming = true
                callStartTime = Date()
                savedNumber = number
                onIncomingCallStarted(context, number, callStartTime)
            }
            TelephonyManager.CALL_STATE_OFFHOOK ->
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if (lastState !== TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false
                    callStartTime = Date()
                    onOutgoingCallStarted(context, savedNumber, callStartTime)
                }
            TelephonyManager.CALL_STATE_IDLE ->
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if (lastState === TelephonyManager.CALL_STATE_RINGING) {
                    //Ring but no pickup-  a miss
                    onMissedCall(context, savedNumber, callStartTime)
                } else if (isIncoming) {
                    onIncomingCallEnded(context, savedNumber, callStartTime, Date())
                } else {
                    onOutgoingCallEnded(context, savedNumber, callStartTime, Date())
                }


        }
        lastState = state
    }
}
