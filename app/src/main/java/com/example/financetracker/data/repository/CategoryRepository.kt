package com.example.financetracker.data.repository

import com.example.financetracker.data.entities.CategoryEntity
import com.example.financetracker.data.entities.TransactionType
import com.example.financetracker.data.local.dao.CategoryDao
import com.example.financetracker.data.local.dao.TransactionDao
import kotlinx.coroutines.flow.Flow

class CategoryRepository(
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao
) {

    fun getAllCategories(userId: String): Flow<List<CategoryEntity>> {
        return categoryDao.getAllCategories(userId)
    }

    fun getCategoriesByType(userId: String, type: TransactionType): Flow<List<CategoryEntity>> {
        return categoryDao.getCategoriesByType(userId, type)
    }

    suspend fun getCategoryById(categoryId: String): CategoryEntity? {
        return categoryDao.getCategoryById(categoryId)
    }

    fun getCategoryByIdFlow(categoryId: String): Flow<CategoryEntity?> {
        return categoryDao.getCategoryByIdFlow(categoryId)
    }

    suspend fun insertCategory(category: CategoryEntity) {
        categoryDao.insertCategory(category)
    }

    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(categoryId: String): Result<Unit> {
        return try {
            // Check if category has transactions
            val transactionCount = categoryDao.getTransactionCount(categoryId)
            if (transactionCount > 0) {
                return Result.failure(
                    Exception("Category has $transactionCount transactions. Please reassign them first.")
                )
            }
            categoryDao.deleteCategoryById(categoryId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategoryAndReassign(
        categoryId: String,
        newCategoryId: String
    ): Result<Unit> {
        return try {
            // First, reassign all transactions
            transactionDao.updateTransactionsCategory(categoryId, newCategoryId)
            // Then delete the category
            categoryDao.deleteCategoryById(categoryId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTransactionCount(categoryId: String): Int {
        return categoryDao.getTransactionCount(categoryId)
    }
}