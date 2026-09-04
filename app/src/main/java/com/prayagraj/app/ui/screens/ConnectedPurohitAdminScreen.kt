package com.prayagraj.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prayagraj.app.ui.screens.admin.PurohitAdminDashboardScreen
import com.prayagraj.app.ui.viewmodel.PurohitAdminViewModel

@Composable
fun ConnectedPurohitAdminScreen(
    viewModel: PurohitAdminViewModel = viewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        state.errorMessage != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Error: ${state.errorMessage}", color = MaterialTheme.colorScheme.error)
            }
        }
        else -> {
            PurohitAdminDashboardScreen(
                purohitName = state.profile?.fullName ?: "Pandit Ji",
                registrationId = state.profile?.registrationId ?: "Verification Pending",
                services = state.services.map { dto ->
                    com.prayagraj.app.data.model.PurohitService(
                        id = dto.id ?: "",
                        purohitId = dto.purohitId,
                        ritualName = dto.ritualName,
                        description = dto.description,
                        fixedDakshina = dto.fixedDakshina,
                        durationMinutes = dto.durationMinutes,
                        materialsIncluded = dto.materialsIncluded
                    )
                },
                onAddService = { newService ->
                    viewModel.addService(
                        name = newService.ritualName,
                        dakshina = newService.fixedDakshina,
                        description = newService.description
                    )
                },
                onBack = onBack
            )
        }
    }
}
