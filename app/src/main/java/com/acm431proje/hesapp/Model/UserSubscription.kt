package com.acm431proje.hesapp.Model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_subs")
data class UserSubscription(
    @PrimaryKey()
    val serviceName: String,
    @ColumnInfo("plan_name")
    val planName: String,
    @ColumnInfo("plan_price")
    val planPrice: Float )

