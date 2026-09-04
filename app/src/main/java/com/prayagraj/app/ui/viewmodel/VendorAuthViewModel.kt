package com.prayagraj.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prayagraj.app.data.auth.VendorAuthState
import com.prayagraj.app.data.repository.VendorAuthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VendorAuthViewModel(
    private val authRepository: VendorAuthRepository = VendorAuthRepository()
) : ViewModel() {

    val authState: StateFlow<VendorAuthState> = authRepository.observeVendorAuthState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = VendorAuthState.Loading
        )

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
