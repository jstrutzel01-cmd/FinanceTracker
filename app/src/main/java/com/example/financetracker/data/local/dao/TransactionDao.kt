package com.example.financetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.financetracker.data.entities.TransactionEntity
import com.example.financetracker.data.entities.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getAllTransactions(userId: String) : Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE userId = :userId AND categoryId = :categoryId ORDER BY date DESC")
    fun getTransactionsByCategory(userId: String, categoryId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND type = :type ORDER BY date DESC")
    fun getTransactionsByType(userId: String, type: TransactionType): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(userId: String, startDate: Long, endDate: Long) : Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER By date DESC LIMIT :limit")
    fun getRecentTransactions(userId: String, limit: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTransaction(transaction: Transaction)

    @Update
    fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("UPDATE transactions SET categoryId = :newCategoryId WHERE categoryId = :oldCategoryId")
    suspend fun updateTransactionsCategory(oldCategoryId: String, newCategoryId: String)

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = :type")
    fun getTotalByType(userId: String)

    @Query("""
        SELECT categoryId, SUM(amount) as total 
        FROM transactions 
        WHERE userId = :userId AND type = 'EXPENSE'
        GROUP BY categoryId
        ORDER BY total DESC
    """)
    fun getSpendingByCategory(userId: String): Flow<Map<String, Double>>
}

