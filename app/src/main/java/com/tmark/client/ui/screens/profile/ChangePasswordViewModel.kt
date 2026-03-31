package com.tmark.client.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tmark.client.data.api.ApiResult
import com.tmark.client.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChangePasswordUiState(
    val hasPassword: Boolean = true,
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val saving: Boolean = false,
    val success: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(ChangePasswordUiState())
    val ui: StateFlow<ChangePasswordUiState> = _ui

    fun init(hasPassword: Boolean) {
        _ui.value = _ui.value.copy(hasPassword = hasPassword)
    }

    fun onCurrentPassword(v: String) { _ui.value = _ui.value.copy(currentPassword = v, error = null) }
    fun onNewPassword(v: String)     { _ui.value = _ui.value.copy(newPassword = v, error = null) }
    fun onConfirmPassword(v: String) { _ui.value = _ui.value.copy(confirmPassword = v, error = null) }

    fun submit() {
        val s = _ui.value
        if (s.hasPassword && s.currentPassword.isBlank()) {
            _ui.value = s.copy(error = "Enter your current password"); return
        }
        if (s.newPassword.length < 8) {
            _ui.value = s.copy(error = "New password must be at least 8 characters"); return
        }
        if (s.newPassword != s.confirmPassword) {
            _ui.value = s.copy(error = "Passwords do not match"); return
        }
        viewModelScope.launch {
            _ui.value = s.copy(saving = true, error = null)
            val current = if (s.hasPassword) s.currentPassword else null
            when (val r = authRepo.changePassword(current, s.newPassword)) {
                is ApiResult.Success   -> _ui.value = _ui.value.copy(saving = false, success = true)
                is ApiResult.Error     -> _ui.value = _ui.value.copy(saving = false, error = r.message)
                is ApiResult.Exception -> _ui.value = _ui.value.copy(saving = false, error = "Network error")
            }
        }
    }
}
