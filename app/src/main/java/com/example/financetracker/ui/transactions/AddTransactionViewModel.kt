package com.example.financetracker.ui.transactions

import android.util.Log
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
import kotlinx.coroutines.launch

class AddTransactionViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val userId = authRepository.getCurrentUserId() ?: ""

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    init {
        Log.d("AddTransactionVM", "=== INIT ===")
        Log.d("AddTransactionVM", "UserId: '$userId'")
        loadCategories()

    }

    private fun loadCategories() {
        Log.d("AddTransactionVM", "Loading categories...")
        viewModelScope.launch {
            categoryRepository.getAllCategories(userId).collect { categories ->
                Log.d("AddTransactionVM", "Received ${categories.size} categories")
                _uiState.value = _uiState.value.copy(categories = categories)
            }
        }
    }

    fun onAmountChange(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
    }

    fun onTypeChange(type: TransactionType) {
        _uiState.value = _uiState.value.copy(type = type, selectedCategoryId = null)
    }

    fun onCategoryChange(categoryId: Long) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
    }

    fun onDateChange(date: Long) {
        _uiState.value = _uiState.value.copy(date = date)
    }

    fun onNotesChange(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun saveTransaction(onSuccess: () -> Unit) {
        val amount = _uiState.value.amount.toDoubleOrNull()
        val categoryId = _uiState.value.selectedCategoryId

        if (amount == null || amount <= 0) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid amount")
            return
        }

        if (categoryId == null) {
            _uiState.value = _uiState.value.copy(error = "Please select a category")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val transaction = TransactionEntity(
                userId = userId,
                amount = amount,
                type = _uiState.value.type,
                categoryId = categoryId,
                date = _uiState.value.date,
                notes = _uiState.value.notes
            )

            try {
                transactionRepository.insertTransaction(transaction)
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save transaction"
                )
            }
        }
    }
}

data class AddTransactionUiState(
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val selectedCategoryId: Long? = null,
    val date: Long = System.currentTimeMillis(),
    val notes: String = "",
    val categories: List<CategoryEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)