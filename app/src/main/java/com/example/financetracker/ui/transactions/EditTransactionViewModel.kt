package com.example.financetracker.ui.transactions

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

class EditTransactionViewModel(
    private val transactionId: String,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val userId = authRepository.getCurrentUserId() ?: ""

    private val _uiState = MutableStateFlow(EditTransactionUiState())
    val uiState: StateFlow<EditTransactionUiState> = _uiState.asStateFlow()

    init {
        loadTransaction()
        loadCategories()
    }

    private fun loadTransaction() {
        viewModelScope.launch {
            val transaction = transactionRepository.getTransactionById(transactionId)
            transaction?.let {
                _uiState.value = _uiState.value.copy(
                    amount = it.amount.toString(),
                    type = it.type,
                    selectedCategoryId = it.categoryId.toString(),
                    date = it.date,
                    notes = it.notes.toString(),
                    isLoading = false
                )
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategories(userId).collect { categories ->
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

    fun onCategoryChange(categoryId: String) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
    }

    fun onDateChange(date: Long) {
        _uiState.value = _uiState.value.copy(date = date)
    }

    fun onNotesChange(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun updateTransaction(onSuccess: () -> Unit) {
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
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)

            val transaction = TransactionEntity(
                id = transactionId.toLong(),
                userId = userId,
                amount = amount,
                type = _uiState.value.type,
                categoryId = categoryId.toLong(),
                date = _uiState.value.date,
                notes = _uiState.value.notes
            )

            try {
                transactionRepository.updateTransaction(transaction)
                _uiState.value = _uiState.value.copy(isSaving = false)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Failed to update transaction"
                )
            }
        }
    }

    fun deleteTransaction(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                transactionRepository.deleteTransaction(transactionId)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Failed to delete transaction"
                )
            }
        }
    }
}

data class EditTransactionUiState(
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val selectedCategoryId: String? = null,
    val date: Long = System.currentTimeMillis(),
    val notes: String = "",
    val categories: List<CategoryEntity> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null
)