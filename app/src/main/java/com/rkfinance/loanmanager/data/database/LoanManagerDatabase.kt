package com.rkfinance.loanmanager.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters // Ensure this import is correct
import com.rkfinance.loanmanager.data.daos.ClientDao
import com.rkfinance.loanmanager.data.daos.LoanDao
import com.rkfinance.loanmanager.data.daos.PaymentDao
import com.rkfinance.loanmanager.data.entities.Client
import com.rkfinance.loanmanager.data.entities.Loan
import com.rkfinance.loanmanager.data.entities.Payment
// import java.util.concurrent.Executors // Already present from your file

// Make sure the import below points to YOUR Converters class
import com.rkfinance.loanmanager.data.database.Converters // <<< CHANGE THIS IMPORT

@Database(
    entities = [Client::class, Loan::class, Payment::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LoanManagerDatabase : RoomDatabase() {

    abstract fun clientDao(): ClientDao
    abstract fun loanDao(): LoanDao
    abstract fun paymentDao(): PaymentDao

    companion object {
        @Volatile
        private var INSTANCE: LoanManagerDatabase? = null

        fun getDatabase(context: Context): LoanManagerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LoanManagerDatabase::class.java,
                    "loan_manager_db"
                )
                    // .addCallback(...) // Your existing callback
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}