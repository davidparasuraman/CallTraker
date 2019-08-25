package com.david.calltraker.DAO

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.david.calltraker.Entity.CallHistoryEntity
import com.david.calltraker.Entity.SmsEntity

@Dao
interface SmsEntityDao {
    @Query("SELECT * FROM smsHistory order by id desc")
    fun getAll(): List<SmsEntity>

    @Query("SELECT * FROM smsHistory where status='1'")
    fun getSelect(): List<SmsEntity>

    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insertAll(vararg todo: SmsEntity)

    @Query("delete from smsHistory")
    fun delete()

    @Update
    fun updateTodo(vararg todos: SmsEntity)
}