package com.example.financetracker.data.model

import com.google.firebase.Timestamp

data class Transaction(
    val id : Long,
    val amount: Double,
    val note : String? = null,
    val timestamp: Long,
    val isIncome: Boolean,
    val categoryId: Long,
)
