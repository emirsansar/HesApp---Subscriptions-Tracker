package com.acm431proje.hesapp.Room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.acm431proje.hesapp.Model.UserSubscription

@Dao
interface UserSubsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userSub: UserSubscription)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<UserSubscription>)

    @Query("SELECT * FROM user_subs")
    suspend fun getAllUserSubs(): List<UserSubscription>

    @Query("DELETE FROM user_subs WHERE serviceName = :serviceName")
    suspend fun deleteUserSub(serviceName: String)
}