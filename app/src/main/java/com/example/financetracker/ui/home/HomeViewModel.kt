package com.example.financetracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.data.entities.CategoryEntity
import com.example.financetracker.data.entities.TransactionEntity
import com.example.financetracker.data.entities.TransactionType
import com.example.financetracker.data.repository.AuthRepository
import com.example.financetracker.data.repository.CategoryRepository
import com.example.financetracker.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val userId = authRepository.getCurrentUserId() ?: ""

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            combine(
                transactionRepository.getAllTransactions(userId),
                transactionRepository.getTotalIncome(userId),
                transactionRepository.getTotalExpense(userId),
                categoryRepository.getAllCategories(userId)
            ) { transactions, income, expense, categories ->
                val recentTransactions = transactions.take(5)
                val balance = (income ?: 0.0) - (expense ?: 0.0)

                // Calculate spending by category for pie chart
                val expenseTransactions = transactions.filter { it.type == TransactionType.EXPENSE }
                val categorySpending = mutableMapOf<Long, Double>()

                expenseTransactions.forEach { transaction ->
                    val current = categorySpending[transaction.categoryId] ?: 0.0
                    categorySpending[transaction.categoryId] = current + transaction.amount
                }

                val categoryData = categorySpending.map { (categoryId, total) ->
                    val category = categories.find { it.id == categoryId }
                    CategorySpending(
                        categoryId = categoryId.toString(),
                        categoryName = category?.name ?: "Unknown",
                        amount = total,
                        color = category?.color ?: "#808080"
                    )
                }.sortedByDescending { it.amount }

                HomeUiState(
                    balance = balance,
                    totalIncome = income ?: 0.0,
                    totalExpense = expense ?: 0.0,
                    recentTransactions = recentTransactions,
                    categorySpending = categoryData,
                    categories = categories,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}

data class HomeUiState(
    val balance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val categorySpending: List<CategorySpending> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val isLoading: Boolean = false
)

data class CategorySpending(
    val categoryId: String,
    val categoryName: String,
    val amount: Double,
    val color: String
)