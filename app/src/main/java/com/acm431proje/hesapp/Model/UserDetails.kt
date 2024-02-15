package com.acm431proje.hesapp.Model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_detail")
class UserDetails(
    @PrimaryKey()
    var uid: String,
    @ColumnInfo("full_name")
    var fullName: String,
    @ColumnInfo("sub_count")
    var subCount: Int,
    @ColumnInfo("spending_month")
    var spendingMonth: Float,
    @ColumnInfo("spending_annual")
    var spendingAnnual: Float
) {
}