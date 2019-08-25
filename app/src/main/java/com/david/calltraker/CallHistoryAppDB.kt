package com.david.calltraker

import android.content.Context
import android.telecom.Call
import androidx.lifecycle.LiveData
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.david.calltraker.DAO.CallHistoryDao
import com.david.calltraker.DAO.SmsEntityDao
import com.david.calltraker.Entity.CallHistoryEntity
import com.david.calltraker.Entity.SmsEntity
import com.david.calltraker.Utils.Constants

//This annotation to tell room what is the entity/table of the database
@Database(entities = arrayOf(CallHistoryEntity::class,SmsEntity::class), version = 1, exportSchema = true)
abstract class CallHistoryAppDB : RoomDatabase() {

    abstract fun callHistoryDao(): CallHistoryDao
    abstract fun SmsEntityDao(): SmsEntityDao

    companion object {
        private var INSTANCE: CallHistoryAppDB? = null

        fun getInstance(context: Context): CallHistoryAppDB? {
            if (INSTANCE == null) {
                synchronized(CallHistoryAppDB::class) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        CallHistoryAppDB::class.java, Constants.DB_NAME)
                        .build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}