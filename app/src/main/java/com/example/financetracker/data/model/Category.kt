package com.example.financetracker.data.model

data class Category(
    val id: Long,
    val name: String,
    val type: CategoryType,

)

enum class CategoryType {
    INCOME,
    EXPENSE
}
