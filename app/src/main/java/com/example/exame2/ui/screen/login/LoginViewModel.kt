package com.example.exame2.ui.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exame2.core.state.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _loginState = MutableStateFlow<UiState<String>?>(null)
    val loginState: StateFlow<UiState<String>?> = _loginState.asStateFlow()

    private val validCredentials = mapOf(
        "admin" to "admin123",
        "operador" to "op2026"
    )

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            delay(800)
            if (validCredentials[username.trim()] == password.trim()) {
                _loginState.value = UiState.Success(username.trim())
            } else {
                _loginState.value = UiState.Error("Invalid credentials. Use admin/admin123 or operador/op2026")
            }
        }
    }

    fun clearState() {
        _loginState.value = null
    }
}
