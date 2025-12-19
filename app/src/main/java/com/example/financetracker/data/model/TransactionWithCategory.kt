package com.example.financetracker.data.model

import com.example.financetracker.data.entities.CategoryEntity
import com.example.financetracker.data.entities.TransactionEntity

data class TransactionWithCategory(
    val transaction: TransactionEntity,
    val category: CategoryEntity
)