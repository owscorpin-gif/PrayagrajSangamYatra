package com.prayagraj.app.ui.screens.admin

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.data.model.PurohitService as ExamplePurohitService
import com.example.ui.screens.admin.PurohitAdminDashboardScreen as ExamplePurohitAdminDashboardScreen
import com.prayagraj.app.data.model.PurohitService

@Composable
fun PurohitAdminDashboardScreen(
    purohitName: String = "Pandit Ramesh Shastri",
    registrationId: String = "TP-PRAYAG-2024-88",
    services: List<PurohitService> = emptyList(),
    onAddService: (PurohitService) -> Unit = {},
    onEditService: (PurohitService) -> Unit = {},
    onDeleteService: (String) -> Unit = {},
    onBack: () -> Unit = {},
    onLogout: () -> Unit = onBack
) {
    ExamplePurohitAdminDashboardScreen(
        purohitName = purohitName,
        registrationId = registrationId,
        services = services.map {
            ExamplePurohitService(
                id = it.id,
                ritualName = it.ritualName,
                fixedDakshina = it.fixedDakshina,
                description = it.description,
                materialsIncluded = it.materialsIncluded,
                durationMinutes = it.durationMinutes
            )
        },
        onAddService = { s ->
            onAddService(
                PurohitService(
                    id = s.id,
                    ritualName = s.ritualName,
                    fixedDakshina = s.fixedDakshina,
                    description = s.description,
                    materialsIncluded = s.materialsIncluded,
                    durationMinutes = s.durationMinutes
                )
            )
        },
        onEditService = { s ->
            onEditService(
                PurohitService(
                    id = s.id,
                    ritualName = s.ritualName,
                    fixedDakshina = s.fixedDakshina,
                    description = s.description,
                    materialsIncluded = s.materialsIncluded,
                    durationMinutes = s.durationMinutes
                )
            )
        },
        onDeleteService = onDeleteService,
        onBack = onBack
    )
}
