package com.tmark.client.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tmark.client.data.api.ApiResult
import com.tmark.client.data.model.Profile
import com.tmark.client.data.repository.AuthRepository
import com.tmark.client.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val loading: Boolean = false,
    val profile: Profile? = null,
    val error: String? = null,
    val saving: Boolean = false,
    val saveSuccess: Boolean = false,
    val loggedOut: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepo: ProfileRepository,
    private val authRepo: AuthRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(ProfileUiState())
    val ui: StateFlow<ProfileUiState> = _ui

    init { load() }

    fun load() {
        viewModelScope.launch {
            _ui.value = ProfileUiState(loading = true)
            when (val r = profileRepo.getProfile()) {
                is ApiResult.Success   -> _ui.value = ProfileUiState(profile = r.data.profile)
                is ApiResult.Error     -> _ui.value = ProfileUiState(error = r.message)
                is ApiResult.Exception -> _ui.value = ProfileUiState(error = "Network error")
            }
        }
    }

    fun update(email: String?, address: String?) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(saving = true)
            when (val r = profileRepo.updateProfile(email, address)) {
                is ApiResult.Success   -> _ui.value = _ui.value.copy(saving = false, profile = r.data.profile, saveSuccess = true)
                is ApiResult.Error     -> _ui.value = _ui.value.copy(saving = false, error = r.message)
                is ApiResult.Exception -> _ui.value = _ui.value.copy(saving = false, error = "Network error")
            }
        }
    }

    fun clearSaveSuccess() { _ui.value = _ui.value.copy(saveSuccess = false, error = null) }

    fun logout() {
        viewModelScope.launch {
            authRepo.logout()
            _ui.value = _ui.value.copy(loggedOut = true)
        }
    }
}
