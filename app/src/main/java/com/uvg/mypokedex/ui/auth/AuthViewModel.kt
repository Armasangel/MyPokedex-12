package com.uvg.mypokedex.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uvg.mypokedex.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _isAuthenticated = MutableStateFlow<Boolean?>(null)
    val isAuthenticated: StateFlow<Boolean?> = _isAuthenticated

    init {
        checkUserAuthentication()
    }

    private fun checkUserAuthentication() {
        _isAuthenticated.value = authRepository.getCurrentUserId() != null
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            val userId = authRepository.signInAnonymously()
            _isAuthenticated.value = userId != null
        }
    }
}