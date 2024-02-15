package com.acm431proje.hesapp.Room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.acm431proje.hesapp.Model.UserDetails

@Database(entities = [UserDetails::class], version = 4)
abstract class UserDetailDB: RoomDatabase() {

    abstract fun userDetailDao(): UserDetailDao

    companion object {

        @Volatile private var instance: UserDetailDB? = null

        private val lock = Any()
        operator fun invoke(context: Context) = instance ?: synchronized(lock) {
            instance ?: makeDatabase(context).also {
                instance = it
            }
        }

        private fun makeDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext, UserDetailDB::class.java, "UserDetail"
        ).build()
    }
}