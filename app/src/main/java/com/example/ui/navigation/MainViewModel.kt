package com.example.ui.navigation

import androidx.lifecycle.ViewModel
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed interface LaunchState {
    data object Loading : LaunchState
    data class DestinationReady(val startRoute: String) : LaunchState
}

class MainViewModel(private val authRepository: AuthRepository = AuthRepository()) : ViewModel() {

    private val _launchState = MutableStateFlow<LaunchState>(LaunchState.Loading)
    val launchState: StateFlow<LaunchState> = _launchState

    init {
        checkSession()
    }

    private fun checkSession() {
        val startDestination = if (authRepository.isUserLoggedIn()) {
            Screen.Places.route
        } else {
            Screen.Auth.route
        }
        _launchState.value = LaunchState.DestinationReady(startDestination)
    }
}
