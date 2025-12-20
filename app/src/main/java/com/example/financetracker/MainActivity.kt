package com.example.financetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.financetracker.data.local.database.AppDatabase
import com.example.financetracker.data.repository.AuthRepository
import com.example.financetracker.data.repository.CategoryRepository
import com.example.financetracker.data.repository.TransactionRepository
import com.example.financetracker.navigation.AppNavigation
import com.example.financetracker.ui.theme.FinanceTrackerTheme

class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database
        database = AppDatabase.getDatabase(applicationContext)

        // Initialize repositories
        transactionRepository = TransactionRepository(database.transactionDao())
        categoryRepository = CategoryRepository(
            database.categoryDao(),
            database.transactionDao()
        )
        authRepository = AuthRepository()

        setContent {
            FinanceTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        transactionRepository = transactionRepository,
                        categoryRepository = categoryRepository,
                        authRepository = authRepository
                    )
                }
            }
        }
    }
}