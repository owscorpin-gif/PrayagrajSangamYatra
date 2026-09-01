package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AuthRepository
import com.example.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthState {
    data object Idle : AuthState
    data object Loading : AuthState
    data object OtpSent : AuthState
    data class Authenticated(val phone: String) : AuthState
    data class Error(val message: String) : AuthState
}

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        // Check if user is already logged in
        val currentPhone = repository.getActiveUserPhone()
        if (currentPhone != null) {
            _state.value = AuthState.Authenticated(currentPhone)
        }
    }

    fun sendOtp(phone: String) {
        val cleanDigits = phone.filter { it.isDigit() }
        if (cleanDigits.length < 10) {
            _state.value = AuthState.Error("Please enter a valid 10-digit mobile number")
            return
        }

        viewModelScope.launch {
            _state.value = AuthState.Loading
            // Formatting for India (+91)
            val formattedPhone = if (phone.startsWith("+")) phone else "+91$cleanDigits"
            val result = repository.sendOtp(formattedPhone)
            result.fold(
                onSuccess = {
                    _state.value = AuthState.OtpSent
                },
                onFailure = { error ->
                    _state.value = AuthState.Error(error.localizedMessage ?: "Failed to send OTP code")
                }
            )
        }
    }

    fun verifyOtp(phone: String, code: String) {
        val cleanCode = code.trim()
        if (cleanCode.length != 6) {
            _state.value = AuthState.Error("Please enter the complete 6-digit OTP code")
            return
        }

        viewModelScope.launch {
            _state.value = AuthState.Loading
            val cleanDigits = phone.filter { it.isDigit() }
            val formattedPhone = if (phone.startsWith("+")) phone else "+91$cleanDigits"
            val result = repository.verifyOtp(formattedPhone, cleanCode)
            result.fold(
                onSuccess = {
                    _state.value = AuthState.Authenticated(formattedPhone)
                },
                onFailure = { error ->
                    _state.value = AuthState.Error(error.localizedMessage ?: "Invalid verification code")
                }
            )
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
            _state.value = AuthState.Idle
        }
    }

    fun resetToIdle() {
        _state.value = AuthState.Idle
    }
}

@Composable
fun PhoneAuthScreen(
    onLoginSuccess: () -> Unit = {},
    viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onDismissOrBack: (() -> Unit)? = null
) {
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    val authState by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current

    // Automatically trigger navigation callback when logged in
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PolishBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (onDismissOrBack != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = onDismissOrBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PolishTextPrimary
                    )
                }
            }
        }

        // Header Sacred Badge
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(PolishPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ॐ",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = PolishPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Prayagraj Sangam Yatra",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = PolishTextPrimary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Sacred Pilgrimage & Verified Panda Portal",
            fontSize = 13.sp,
            color = PolishTextSecondary,
            modifier = Modifier.padding(top = 4.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Main Auth Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = PolishCardSurface,
            border = BorderStroke(1.dp, PolishBorder)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (val state = authState) {
                    is AuthState.Idle, is AuthState.Error, is AuthState.Loading -> {
                        Text(
                            text = "Phone Number Login",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishTextPrimary
                        )

                        Text(
                            text = "Enter your 10-digit mobile number to receive a secure Supabase SMS OTP.",
                            fontSize = 12.sp,
                            color = PolishTextSecondary,
                            textAlign = TextAlign.Center
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = {
                                if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                                    phone = it
                                }
                            },
                            label = { Text("Mobile Number (10 Digits)") },
                            prefix = { Text("+91 ", fontWeight = FontWeight.Bold, color = PolishPrimary) },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = "Phone Icon", tint = PolishPrimary)
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    viewModel.sendOtp(phone)
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phone_number_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.sendOtp(phone)
                            },
                            enabled = phone.length == 10 && state !is AuthState.Loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("get_otp_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary)
                        ) {
                            if (state is AuthState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Get OTP Code", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    is AuthState.OtpSent -> {
                        Text(
                            text = "Verify Mobile OTP",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishTextPrimary
                        )

                        Text(
                            text = "A 6-digit SMS verification code was sent to +91 $phone",
                            fontSize = 12.sp,
                            color = PolishTextSecondary,
                            textAlign = TextAlign.Center
                        )

                        OutlinedTextField(
                            value = code,
                            onValueChange = {
                                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                    code = it
                                }
                            },
                            label = { Text("Enter 6-Digit OTP") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = "Security Code", tint = PolishPrimary)
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    viewModel.verifyOtp(phone, code)
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("otp_code_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.verifyOtp(phone, code)
                            },
                            enabled = code.length == 6,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("verify_otp_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary)
                        ) {
                            Text("Verify & Login", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }

                        TextButton(
                            onClick = { viewModel.resetToIdle() }
                        ) {
                            Text("Change Mobile Number", fontSize = 12.sp, color = PolishTextSecondary)
                        }
                    }

                    is AuthState.Authenticated -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Authenticated",
                            tint = PolishGreen,
                            modifier = Modifier.size(56.dp)
                        )

                        Text(
                            text = "Login Successful!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishGreen
                        )

                        Text(
                            text = "Logged in as ${state.phone}. Welcome to the Prayagraj Yatra pilgrimage portal.",
                            fontSize = 13.sp,
                            color = PolishTextSecondary,
                            textAlign = TextAlign.Center
                        )

                        OutlinedButton(
                            onClick = { viewModel.signOut() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Sign Out")
                        }
                    }
                }

                if (authState is AuthState.Error) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = (authState as AuthState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
