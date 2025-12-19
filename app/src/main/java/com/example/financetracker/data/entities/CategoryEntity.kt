package com.example.financetracker.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.financetracker.data.model.CategoryType
import kotlin.time.Clock

@Entity(
    tableName = "categories",

)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val type: TransactionType,
    val createdAt: Long = System.currentTimeMillis()
)
