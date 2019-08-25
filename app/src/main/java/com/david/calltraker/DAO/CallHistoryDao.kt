package com.david.calltraker.DAO

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.david.calltraker.Entity.CallHistoryEntity

@Dao
interface CallHistoryDao {
    @Query("SELECT * FROM callHistory order by id desc")
    fun getAll(): List<CallHistoryEntity>

    @Query("SELECT * FROM callHistory where mobileNo=:mobile order by id desc")
    fun getSelect(mobile:String): CallHistoryEntity

    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun insertAll(vararg todo: CallHistoryEntity)

    @Query("delete from callHistory")
    fun delete()

    @Update
    fun updateTodo(vararg todos: CallHistoryEntity)
}