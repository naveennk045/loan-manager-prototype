package com.rkfinance.loanmanager.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rkfinance.loanmanager.data.enums.Area

@Entity(tableName = "clients")
data class Client(
    @PrimaryKey(autoGenerate = true)
    val clientId: Int = 0,
    val name: String,
    val contact: String,
    val address: String?,
    val area: Area
)