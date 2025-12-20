package com.example.financetracker.ui.profile

import androidx.lifecycle.ViewModel
import com.example.financetracker.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserInfo()
    }

    private fun loadUserInfo() {
        val email = authRepository.getCurrentUserEmail() ?: "No email"
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun signOut(onSignOutComplete: () -> Unit) {
        authRepository.signOut()
        onSignOutComplete()
    }
}

data class ProfileUiState(
    val email: String = ""
)