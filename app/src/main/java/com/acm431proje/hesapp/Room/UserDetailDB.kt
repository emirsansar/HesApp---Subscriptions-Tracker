package com.acm431proje.hesapp.Room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.acm431proje.hesapp.Model.UserDetails

@Database(entities = [UserDetails::class], version = 4)
abstract class UserDetailDB: RoomDatabase() {

    abstract fun userDetailDao(): UserDetailDao
}