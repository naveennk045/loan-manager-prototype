package com.rkfinance.loanmanager.data.entities


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Loan::class,
            parentColumns = ["loanId"],
            childColumns = ["loanId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val paymentId: Int = 0,
    val loanId: Int, // Foreign Key
    val amount: Double,
    val paymentDate: Long // Unix timestamp
)