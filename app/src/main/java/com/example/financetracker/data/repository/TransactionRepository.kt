package com.example.financetracker.data.repository

import com.example.financetracker.data.entities.TransactionEntity
import com.example.financetracker.data.entities.TransactionType
import com.example.financetracker.data.local.dao.CategorySpending
import com.example.financetracker.data.local.dao.TransactionDao
import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val transactionDao: TransactionDao
) {

    fun getAllTransactions(userId: String): Flow<List<TransactionEntity>> {
        return transactionDao.getAllTransactions(userId)
    }

    suspend fun getTransactionById(transactionId: String): TransactionEntity? {
        return transactionDao.getTransactionById(transactionId)
    }

    fun getTransactionByIdFlow(transactionId: String): Flow<TransactionEntity?> {
        return transactionDao.getTransactionByIdFlow(transactionId)
    }

    fun getTransactionsByCategory(userId: String, categoryId: String): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByCategory(userId, categoryId)
    }

    fun getTransactionsByType(userId: String, type: TransactionType): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByType(userId, type)
    }

    fun getTransactionsByDateRange(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByDateRange(userId, startDate, endDate)
    }

    fun getRecentTransactions(userId: String, limit: Int = 10): Flow<List<TransactionEntity>> {
        return transactionDao.getRecentTransactions(userId, limit)
    }

    suspend fun insertTransaction(transaction: TransactionEntity) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transactionId: String) {
        transactionDao.deleteTransactionById(transactionId)
    }


    fun getTotalIncome(userId: String): Flow<Double?> {
        return transactionDao.getTotalByType(userId, TransactionType.INCOME)
    }

    fun getTotalExpense(userId: String): Flow<Double?> {
        return transactionDao.getTotalByType(userId, TransactionType.EXPENSE)
    }

    fun getSpendingByCategory(userId: String): Flow<List<CategorySpending>> {
        return transactionDao.getSpendingByCategory(userId)
    }
}