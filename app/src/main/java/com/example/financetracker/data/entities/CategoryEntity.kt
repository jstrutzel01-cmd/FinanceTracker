package com.example.financetracker.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "categories",

)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val userId: String,
    val name: String,
    val type: TransactionType,
    val color: String,
    val createdAt: Long = System.currentTimeMillis()
)
