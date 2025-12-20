package com.example.financetracker.data.local.database

import androidx.room.TypeConverter
import com.example.financetracker.data.entities.TransactionType

class RoomConverters {

    @TypeConverter
    fun fromTransactionType(value : TransactionType): String {
        return value.name;
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value);
    }
}