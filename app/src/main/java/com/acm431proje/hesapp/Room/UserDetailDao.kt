package com.acm431proje.hesapp.Room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.acm431proje.hesapp.Model.UserDetails

@Dao
interface UserDetailDao {

    @Query("SELECT * FROM user_detail")
    suspend fun getAll(): List<UserDetails>

    @Query("SELECT * FROM user_detail WHERE uid = :userId")
    suspend fun getUserDetail(userId: String): UserDetails?

    @Insert
    suspend fun insert(userDetails: UserDetails)

    @Update
    suspend fun update(userDetail: UserDetails)

    @Delete
    suspend fun delete(userDetails: UserDetails)
}