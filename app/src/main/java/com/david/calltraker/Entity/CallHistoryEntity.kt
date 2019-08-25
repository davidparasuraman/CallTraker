package com.david.calltraker.Entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

//Annotation Parcelize for make the data class parcelable
@Parcelize
//Annotation Entity to declare that this class is a table with name = students
@Entity(tableName = "callHistory")
data class CallHistoryEntity(

    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id: Long? =null,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "mobileNo") var mobileNo: String,
    @ColumnInfo(name = "callStatus") var callStatus: String,
    @ColumnInfo(name = "callDate") var callDate: String,
    @ColumnInfo(name = "count") var count: String,
    @ColumnInfo(name = "message") var message: String): Parcelable

//collumn use @CollumnInfo Annotation
//You can aslo declare the primary key by adding @PrimaryKey Annotation