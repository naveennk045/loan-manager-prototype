package com.rkfinance.loanmanager.data.database // Or wherever you placed it

import androidx.room.TypeConverter
import com.rkfinance.loanmanager.data.enums.Area
import com.rkfinance.loanmanager.data.enums.Frequency

class Converters { // This is YOUR class
    @TypeConverter
    fun fromArea(value: Area?): String? {
        return value?.name
    }

    @TypeConverter
    fun toArea(value: String?): Area? {
        return value?.let { Area.valueOf(it) }
    }

    @TypeConverter
    fun fromFrequency(value: Frequency?): String? {
        return value?.name
    }

    @TypeConverter
    fun toFrequency(value: String?): Frequency? {
        return value?.let { Frequency.valueOf(it) }
    }

    // You can add more converters here if needed, e.g., for Date/Long
    @TypeConverter
    fun fromTimestamp(value: Long?): java.util.Date? {
        return value?.let { java.util.Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: java.util.Date?): Long? {
        return date?.time
    }
}