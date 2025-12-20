package com.example.financetracker.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financetracker.data.entities.CategoryEntity
import com.example.financetracker.data.entities.TransactionType
import com.example.financetracker.data.repository.AuthRepository
import com.example.financetracker.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class SignUpViewModel(
    private val authRepository: AuthRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword)
    }

    fun signUp(onSuccess: () -> Unit) {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword

        // Validation
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = _uiState.value.copy(error = "Please enter a valid email")
            return
        }
        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(error = "Password must be at least 6 characters")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = _uiState.value.copy(error = "Passwords do not match")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            authRepository.signUp(email, password).fold(
                onSuccess = { user ->
                    createDefaultCategories(user.uid)
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Sign up failed"
                    )
                }
            )
        }
    }

    private suspend fun createDefaultCategories(userId: String) {
        listOf(
            CategoryEntity(userId = userId, name = "Food & Dining", type = TransactionType.EXPENSE, color = "#FF5733"),
            CategoryEntity(userId = userId, name = "Transport", type = TransactionType.EXPENSE, color = "#33C3FF"),
            CategoryEntity(userId = userId, name = "Shopping", type = TransactionType.EXPENSE, color = "#9D33FF"),
            CategoryEntity(userId = userId, name = "Bills", type = TransactionType.EXPENSE, color = "#F5FF33"),
            CategoryEntity(userId = userId, name = "Salary", type = TransactionType.INCOME, color = "#4CAF50"),
            CategoryEntity(userId = userId, name = "Other", type = TransactionType.EXPENSE, color = "#808080")
        ).forEach { categoryRepository.insertCategory(it) }
    }
}

data class SignUpUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)