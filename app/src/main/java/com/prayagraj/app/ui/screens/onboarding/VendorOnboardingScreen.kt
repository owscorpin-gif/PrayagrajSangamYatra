package com.prayagraj.app.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prayagraj.app.data.auth.VendorType
import com.prayagraj.app.ui.viewmodel.OnboardingUiState
import com.prayagraj.app.ui.viewmodel.VendorOnboardingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorOnboardingScreen(
    viewModel: VendorOnboardingViewModel = viewModel(),
    onOnboardingComplete: (VendorType) -> Unit
) {
    var selectedRole by remember { mutableStateOf<VendorType?>(null) }
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is OnboardingUiState.Success) {
            onOnboardingComplete((uiState as OnboardingUiState.Success).vendorType)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vendor Portal Registration") },
                modifier = Modifier.testTag("vendor_onboarding_topbar")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .testTag("vendor_onboarding_column")
        ) {
            Text(
                text = "Select Your Service Category",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Choose the primary service you will provide to pilgrims and visitors.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2-Column / Vertical Role Selection Cards
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                vendorRoles.forEach { role ->
                    val isSelected = selectedRole == role.type
                    OutlinedCard(
                        onClick = { selectedRole = role.type },
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vendor_role_card_${role.type.name}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = role.icon,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = role.title,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = role.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedRole = role.type },
                                modifier = Modifier.testTag("vendor_role_radio_${role.type.name}")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Profile Details Form (Appears when a role is selected)
            AnimatedVisibility(visible = selectedRole != null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Vendor Details",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vendor_full_name_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Mobile / WhatsApp Number *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vendor_phone_number_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        label = { Text("Agency / Firm Name (Optional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vendor_business_name_input"),
                        singleLine = true
                    )

                    if (uiState is OnboardingUiState.Error) {
                        Text(
                            text = (uiState as OnboardingUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.testTag("vendor_onboarding_error_text")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            selectedRole?.let { role ->
                                viewModel.registerVendorProfile(
                                    fullName = fullName,
                                    phoneNumber = phoneNumber,
                                    businessName = businessName,
                                    selectedVendorType = role
                                )
                            }
                        },
                        enabled = fullName.isNotBlank() && phoneNumber.isNotBlank() && uiState !is OnboardingUiState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("complete_registration_button")
                    ) {
                        if (uiState is OnboardingUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Complete Registration")
                        }
                    }
                }
            }
        }
    }
}
