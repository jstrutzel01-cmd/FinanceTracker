package com.example.financetracker.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.data.entities.CategoryEntity
import com.example.financetracker.data.entities.TransactionEntity
import com.example.financetracker.data.entities.TransactionType
import com.example.financetracker.data.repository.AuthRepository
import com.example.financetracker.data.repository.CategoryRepository
import com.example.financetracker.data.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TransactionsListViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val userId = authRepository.getCurrentUserId() ?: ""

    private val _uiState = MutableStateFlow(TransactionsListUiState())
    val uiState: StateFlow<TransactionsListUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                transactionRepository.getAllTransactions(userId),
                categoryRepository.getAllCategories(userId)
            ) { transactions, categories ->
                _uiState.value.copy(
                    allTransactions = transactions,
                    filteredTransactions = applyFilters(transactions),
                    categories = categories,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setFilterType(type: TransactionType?) {
        _uiState.value = _uiState.value.copy(filterType = type)
        applyFiltersAndUpdate()
    }

    fun setFilterCategory(categoryId: String?) {
        _uiState.value = _uiState.value.copy(filterCategory = categoryId)
        applyFiltersAndUpdate()
    }

    fun setSortOrder(ascending: Boolean) {
        _uiState.value = _uiState.value.copy(sortAscending = ascending)
        applyFiltersAndUpdate()
    }

    private fun applyFiltersAndUpdate() {
        val filtered = applyFilters(_uiState.value.allTransactions)
        _uiState.value = _uiState.value.copy(filteredTransactions = filtered)
    }

    private fun applyFilters(transactions: List<TransactionEntity>): List<TransactionEntity> {
        var result = transactions

        _uiState.value.filterType?.let { type ->
            result = result.filter { it.type == type }
        }

        _uiState.value.filterCategory?.let { categoryId ->
            result = result.filter { it.categoryId == categoryId.toLong() }
        }

        result = if (_uiState.value.sortAscending) {
            result.sortedBy { it.date }
        } else {
            result.sortedByDescending { it.date }
        }

        return result
    }
}

data class TransactionsListUiState(
    val allTransactions: List<TransactionEntity> = emptyList(),
    val filteredTransactions: List<TransactionEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val filterType: TransactionType? = null,
    val filterCategory: String? = null,
    val sortAscending: Boolean = false,
    val isLoading: Boolean = true
)