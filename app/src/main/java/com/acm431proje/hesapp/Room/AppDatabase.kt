package com.acm431proje.hesapp.Room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.acm431proje.hesapp.Model.UserDetails
import com.acm431proje.hesapp.Model.UserSubscription

@Database(entities = [UserDetails::class, UserSubscription::class], version = 7)
abstract class AppDatabase: RoomDatabase() {

    abstract fun userDetailDao(): UserDetailDao
    abstract fun userSubsDao(): UserSubsDao

    companion object {

        @Volatile private var instance: AppDatabase? = null

        private val lock = Any()
        operator fun invoke(context: Context) = instance ?: synchronized(lock) {
            instance ?: makeDatabase(context).also {
                instance = it
            }
        }

        private fun makeDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext, AppDatabase::class.java, "app-database"
        ).fallbackToDestructiveMigration().build()
    }
}