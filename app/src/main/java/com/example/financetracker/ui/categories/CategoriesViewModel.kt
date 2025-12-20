package com.example.financetracker.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.data.entities.CategoryEntity
import com.example.financetracker.data.entities.TransactionType
import com.example.financetracker.data.repository.AuthRepository
import com.example.financetracker.data.repository.CategoryRepository
import com.example.financetracker.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val categoryRepository: CategoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val userId = authRepository.getCurrentUserId() ?: ""

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategories(userId).collect { categories ->
                _uiState.value = _uiState.value.copy(
                    categories = categories,
                    isLoading = false
                )
            }
        }
    }

    fun showAddDialog(type: TransactionType) {
        _uiState.value = _uiState.value.copy(
            showAddDialog = true,
            dialogType = type,
            dialogName = "",
            dialogColor = Constants.CATEGORY_COLORS.first()
        )
    }

    fun hideAddDialog() {
        _uiState.value = _uiState.value.copy(showAddDialog = false)
    }

    fun onDialogNameChange(name: String) {
        _uiState.value = _uiState.value.copy(dialogName = name)
    }

    fun onDialogColorChange(color: String) {
        _uiState.value = _uiState.value.copy(dialogColor = color)
    }

    fun addCategory() {
        val name = _uiState.value.dialogName.trim()
        if (name.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Category name cannot be empty")
            return
        }

        viewModelScope.launch {
            val category = CategoryEntity(
                userId = userId,
                name = name,
                type = _uiState.value.dialogType,
                color = _uiState.value.dialogColor
            )
            categoryRepository.insertCategory(category)
            hideAddDialog()
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true)

            val count = categoryRepository.getTransactionCount(categoryId)
            if (count > 0) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    error = "Cannot delete category with $count transactions. Please reassign them first."
                )
                return@launch
            }

            categoryRepository.deleteCategory(categoryId)
            _uiState.value = _uiState.value.copy(isDeleting = false)
        }
    }
}

data class CategoriesUiState(
    val categories: List<CategoryEntity> = emptyList(),
    val showAddDialog: Boolean = false,
    val dialogType: TransactionType = TransactionType.EXPENSE,
    val dialogName: String = "",
    val dialogColor: String = Constants.CATEGORY_COLORS.first(),
    val isLoading: Boolean = true,
    val isDeleting: Boolean = false,
    val error: String? = null
)