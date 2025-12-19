package com.example.financetracker.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("categoryId"),
            onDelete = ForeignKey.RESTRICT,
        )
    ],
    indices = [
        Index(value = ["categoryId"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val amount: Double,
    val type: TransactionType,
    val date: Long,
    val categoryId: Long,
    val notes: String?,
    val createdAt: Long = System.currentTimeMillis()
    )
enum class TransactionType {
    INCOME,
    EXPENSE
}