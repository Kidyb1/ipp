package com.example.zz.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth? by lazy {
        try {
            com.google.firebase.FirebaseApp.getInstance()
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    fun login(email: String, password: String) {
        if (auth == null) {
            _authState.value = AuthState.Error("Błąd: Firebase nie jest zainicjalizowany. Upewnij się, że dodałeś plik google-services.json do folderu app/.")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth!!.signInWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Błąd logowania")
            }
        }
    }

    fun register(email: String, password: String) {
        if (auth == null) {
            _authState.value = AuthState.Error("Błąd: Firebase nie jest zainicjalizowany. Upewnij się, że dodałeś plik google-services.json do folderu app/.")
            return
        }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth!!.createUserWithEmailAndPassword(email, password).await()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.localizedMessage ?: "Błąd rejestracji")
            }
        }
    }
}
