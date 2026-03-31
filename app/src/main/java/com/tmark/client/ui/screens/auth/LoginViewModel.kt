package com.tmark.client.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tmark.client.data.api.ApiResult
import com.tmark.client.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginStep {
    object Phone    : LoginStep()
    object Register : LoginStep()
    object Otp      : LoginStep()
    object Email    : LoginStep()
}

data class LoginUiState(
    val step: LoginStep = LoginStep.Phone,
    // Phone / OTP fields
    val phone: String = "",
    val otp: String = "",
    // Registration fields
    val regName: String = "",
    val regEmail: String = "",
    val regPassword: String = "",
    val regConfirmPassword: String = "",
    // Email login fields
    val emailInput: String = "",
    val emailPassword: String = "",
    // Common
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(LoginUiState())
    val ui: StateFlow<LoginUiState> = _ui

    // ── Phone validation ────────────────────────────────────────────────────
    // Accepts Bangladeshi mobile numbers in any common format:
    //   Local:         01XXXXXXXXX   (11 digits, operator digit 3–9)
    //   With country:  +8801XXXXXXXXX or 8801XXXXXXXXX
    //   Spaces/dashes between groups are stripped before checking
    private fun validatePhone(raw: String): String? {
        val digits = raw.replace(Regex("[\\s\\-().+]"), "")
        val local: String = when {
            digits.startsWith("880") && digits.length == 13 -> "0${digits.removePrefix("880")}"
            digits.startsWith("88")  && digits.length == 13 -> "0${digits.removePrefix("88")}"
            digits.startsWith("0")   && digits.length == 11 -> digits
            digits.startsWith("1")   && digits.length == 10 -> "0$digits"
            else -> return "Enter a valid Bangladeshi phone number"
        }
        // Local number must match 01[3-9]XXXXXXXX
        return if (Regex("^01[3-9]\\d{8}$").matches(local)) null
        else "Enter a valid Bangladeshi phone number"
    }

    // Phone step
    fun onPhoneChange(v: String) { _ui.value = _ui.value.copy(phone = v, error = null) }
    fun onOtpChange(v: String)   { _ui.value = _ui.value.copy(otp = v, error = null) }

    fun sendOtp() {
        val phone = _ui.value.phone.trim()
        val phoneError = validatePhone(phone)
        if (phoneError != null) { _ui.value = _ui.value.copy(error = phoneError); return }
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = null)
            when (val r = authRepo.sendOtp(phone)) {
                is ApiResult.Success -> {
                    if (r.data.newUser) {
                        _ui.value = _ui.value.copy(loading = false, step = LoginStep.Register)
                    } else {
                        _ui.value = _ui.value.copy(loading = false, step = LoginStep.Otp)
                    }
                }
                is ApiResult.Error -> {
                    // 404 "Account not found" means phone isn't registered — open registration
                    if (r.code == 404 || r.message.contains("not found", ignoreCase = true)) {
                        _ui.value = _ui.value.copy(loading = false, step = LoginStep.Register)
                    } else {
                        _ui.value = _ui.value.copy(loading = false, error = r.message)
                    }
                }
                is ApiResult.Exception -> _ui.value = _ui.value.copy(loading = false, error = "Network error")
            }
        }
    }

    fun verifyOtp() {
        val otp = _ui.value.otp.trim()
        if (otp.length < 4) { _ui.value = _ui.value.copy(error = "Enter the OTP code"); return }
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = null)
            when (val r = authRepo.verifyOtp(_ui.value.phone.trim(), otp)) {
                is ApiResult.Success   -> _ui.value = _ui.value.copy(loading = false, success = true)
                is ApiResult.Error     -> _ui.value = _ui.value.copy(loading = false, error = r.message)
                is ApiResult.Exception -> _ui.value = _ui.value.copy(loading = false, error = "Network error")
            }
        }
    }

    fun backToPhone() { _ui.value = _ui.value.copy(step = LoginStep.Phone, otp = "", error = null) }

    // Auto-fill OTP from SMS Retriever
    fun autoFillOtp(code: String) {
        _ui.value = _ui.value.copy(otp = code)
        verifyOtp()
    }

    // Registration step
    fun onRegName(v: String)            { _ui.value = _ui.value.copy(regName = v, error = null) }
    fun onRegEmail(v: String)           { _ui.value = _ui.value.copy(regEmail = v, error = null) }
    fun onRegPassword(v: String)        { _ui.value = _ui.value.copy(regPassword = v, error = null) }
    fun onRegConfirmPassword(v: String) { _ui.value = _ui.value.copy(regConfirmPassword = v, error = null) }

    fun register() {
        val s = _ui.value
        if (s.regName.isBlank())     { _ui.value = s.copy(error = "Name is required"); return }
        if (s.regPassword.length < 8){ _ui.value = s.copy(error = "Password must be at least 8 characters"); return }
        if (s.regPassword != s.regConfirmPassword) { _ui.value = s.copy(error = "Passwords do not match"); return }
        val email = s.regEmail.trim().ifBlank { null }
        viewModelScope.launch {
            _ui.value = s.copy(loading = true, error = null)
            when (val r = authRepo.register(s.phone.trim(), s.regName.trim(), email, s.regPassword)) {
                is ApiResult.Success   -> _ui.value = _ui.value.copy(loading = false, step = LoginStep.Otp)
                is ApiResult.Error     -> _ui.value = _ui.value.copy(loading = false, error = r.message)
                is ApiResult.Exception -> _ui.value = _ui.value.copy(loading = false, error = "Network error")
            }
        }
    }

    // Email login step
    fun onEmailInput(v: String)    { _ui.value = _ui.value.copy(emailInput = v, error = null) }
    fun onEmailPassword(v: String) { _ui.value = _ui.value.copy(emailPassword = v, error = null) }

    fun switchToEmailLogin() { _ui.value = _ui.value.copy(step = LoginStep.Email, error = null) }
    fun switchToPhoneLogin() { _ui.value = _ui.value.copy(step = LoginStep.Phone, error = null) }

    fun loginWithEmail() {
        val s = _ui.value
        if (s.emailInput.isBlank())    { _ui.value = s.copy(error = "Enter your email"); return }
        if (s.emailPassword.isBlank()) { _ui.value = s.copy(error = "Enter your password"); return }
        viewModelScope.launch {
            _ui.value = s.copy(loading = true, error = null)
            when (val r = authRepo.loginEmail(s.emailInput.trim(), s.emailPassword)) {
                is ApiResult.Success   -> _ui.value = _ui.value.copy(loading = false, success = true)
                is ApiResult.Error     -> _ui.value = _ui.value.copy(loading = false, error = r.message)
                is ApiResult.Exception -> _ui.value = _ui.value.copy(loading = false, error = "Network error")
            }
        }
    }
}
