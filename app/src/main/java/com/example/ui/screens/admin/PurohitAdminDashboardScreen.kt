package com.example.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.PurohitService
import com.example.data.model.toDomain
import com.example.data.model.toDto
import com.example.ui.components.AdminTrendAnalyticsSection
import com.example.ui.viewmodel.PurohitAdminViewModel

enum class AdminTab {
    ANALYTICS, RITUALS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurohitAdminDashboardScreen(
    purohitName: String = "Pandit Ramesh Shastri",
    registrationId: String = "TP-PRAYAG-2024-88",
    services: List<PurohitService> = listOf(
        PurohitService(
            ritualName = "Triveni Sangam Snan & Vedic Sankalp",
            fixedDakshina = 501.0,
            description = "Sacred dip with Vedic mantras, clan lineage recitation, and Sankalpa.",
            materialsIncluded = true
        ),
        PurohitService(
            ritualName = "Pind Daan & Pitra Tarpan Puja",
            fixedDakshina = 2100.0,
            description = "Complete ancestral rites according to Garuda Purana at Sangam / Daraganj.",
            materialsIncluded = true
        ),
        PurohitService(
            ritualName = "Rudrabhishek & Special Ganga Aarti",
            fixedDakshina = 1100.0,
            description = "Shiva lingam abhishek with holy Ganga water, panchamrit and bilva patra.",
            materialsIncluded = false
        )
    ),
    viewModel: PurohitAdminViewModel = viewModel(),
    onAddService: (PurohitService) -> Unit = {},
    onEditService: (PurohitService) -> Unit = {},
    onDeleteService: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    val activePurohitName = uiState.profile?.fullName?.ifBlank { purohitName } ?: purohitName
    val activeRegistrationId = uiState.profile?.registrationId?.ifBlank { registrationId } ?: registrationId

    val serviceList = remember(uiState.services, services) {
        if (uiState.services.isNotEmpty()) {
            uiState.services.map { it.toDomain() }
        } else {
            services
        }
    }

    var selectedAdminTab by remember { mutableStateOf(AdminTab.ANALYTICS) }
    var showAddDialog by remember { mutableStateOf(false) }
    var serviceToEdit by remember { mutableStateOf<PurohitService?>(null) }
    var serviceToDelete by remember { mutableStateOf<PurohitService?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Purohit Admin Dashboard") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("admin_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (selectedAdminTab == AdminTab.RITUALS) {
                        IconButton(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.testTag("admin_add_action")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Ritual")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedAdminTab == AdminTab.RITUALS) {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Add Ritual / Puja") },
                    modifier = Modifier.testTag("admin_add_fab")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Profile Verification Header Card
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = activePurohitName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Reg No: $activeRegistrationId (Tirth Purohit Sabha)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            uiState.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Tabs: Analytics vs Rituals
            TabRow(
                selectedTabIndex = selectedAdminTab.ordinal,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedAdminTab == AdminTab.ANALYTICS,
                    onClick = { selectedAdminTab = AdminTab.ANALYTICS },
                    text = { Text("Trends & Analytics") },
                    icon = { Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_admin_analytics")
                )
                Tab(
                    selected = selectedAdminTab == AdminTab.RITUALS,
                    onClick = { selectedAdminTab = AdminTab.RITUALS },
                    text = { Text("Rituals & Rates (${serviceList.size})") },
                    icon = { Icon(Icons.Default.SelfImprovement, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_admin_rituals")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedAdminTab) {
                AdminTab.ANALYTICS -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            AdminTrendAnalyticsSection(
                                roleTitle = "Purohit"
                            )
                        }
                    }
                }

                AdminTab.RITUALS -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "My Offered Rituals & Transparent Rates",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${serviceList.size} Rituals",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (serviceList.isEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No rituals published yet",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Click 'Add Ritual / Puja' to publish standard dakshina rates for pilgrims.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(serviceList, key = { it.id }) { service ->
                                OutlinedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("ritual_card_${service.id}")
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = service.ritualName,
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "₹${service.fixedDakshina.toInt()}",
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        if (service.description.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = service.description,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            SuggestionChip(
                                                onClick = {},
                                                label = {
                                                    Text(if (service.materialsIncluded) "Samagri Included" else "Samagri Extra")
                                                }
                                            )

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                OutlinedButton(
                                                    onClick = { serviceToEdit = service },
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                    modifier = Modifier.testTag("edit_service_${service.id}")
                                                ) {
                                                    Icon(
                                                        Icons.Default.Edit,
                                                        contentDescription = "Edit",
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Edit")
                                                }

                                                OutlinedButton(
                                                    onClick = { serviceToDelete = service },
                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                        contentColor = MaterialTheme.colorScheme.error
                                                    ),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                    modifier = Modifier.testTag("delete_service_${service.id}")
                                                ) {
                                                    Icon(
                                                        Icons.Default.Delete,
                                                        contentDescription = "Delete",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Delete")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Service Dialog
        if (showAddDialog) {
            AddRitualDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { newService ->
                    viewModel.addService(
                        name = newService.ritualName,
                        dakshina = newService.fixedDakshina,
                        description = newService.description,
                        materialsIncluded = newService.materialsIncluded
                    )
                    onAddService(newService)
                    showAddDialog = false
                }
            )
        }

        // Edit Service Dialog
        serviceToEdit?.let { currentService ->
            EditRitualDialog(
                service = currentService,
                onDismiss = { serviceToEdit = null },
                onConfirm = { updatedService ->
                    val purohitId = uiState.profile?.id ?: "purohit-demo-01"
                    viewModel.updateService(updatedService.toDto(purohitId))
                    onEditService(updatedService)
                    serviceToEdit = null
                }
            )
        }

        // Delete Confirmation Dialog
        serviceToDelete?.let { service ->
            DeleteRitualConfirmationDialog(
                service = service,
                onDismiss = { serviceToDelete = null },
                onConfirm = {
                    viewModel.deleteService(service.id)
                    onDeleteService(service.id)
                    serviceToDelete = null
                }
            )
        }
    }
}

@Composable
fun AddRitualDialog(
    onDismiss: () -> Unit,
    onConfirm: (PurohitService) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dakshina by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var materialsIncluded by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Offer New Ritual") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Ritual Name (e.g. Sankalpa)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_ritual_name_input")
                )
                OutlinedTextField(
                    value = dakshina,
                    onValueChange = { dakshina = it },
                    label = { Text("Standard Dakshina (₹)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_ritual_dakshina_input")
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description & Instructions") },
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_ritual_desc_input")
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = materialsIncluded,
                        onCheckedChange = { materialsIncluded = it },
                        modifier = Modifier.testTag("add_ritual_materials_checkbox")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Samagri / Puja Materials Included",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && dakshina.isNotBlank()) {
                        onConfirm(
                            PurohitService(
                                ritualName = name.trim(),
                                fixedDakshina = dakshina.toDoubleOrNull() ?: 0.0,
                                description = desc.trim(),
                                materialsIncluded = materialsIncluded
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("publish_ritual_button")
            ) {
                Text("Publish Ritual")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_add_ritual_button")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditRitualDialog(
    service: PurohitService,
    onDismiss: () -> Unit,
    onConfirm: (PurohitService) -> Unit
) {
    var name by remember { mutableStateOf(service.ritualName) }
    var dakshina by remember { mutableStateOf(service.fixedDakshina.toInt().toString()) }
    var desc by remember { mutableStateOf(service.description) }
    var materialsIncluded by remember { mutableStateOf(service.materialsIncluded) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Ritual Details") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Ritual Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_ritual_name_input")
                )
                OutlinedTextField(
                    value = dakshina,
                    onValueChange = { dakshina = it },
                    label = { Text("Standard Dakshina (₹)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_ritual_dakshina_input")
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description & Instructions") },
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_ritual_desc_input")
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = materialsIncluded,
                        onCheckedChange = { materialsIncluded = it },
                        modifier = Modifier.testTag("edit_ritual_materials_checkbox")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Samagri / Puja Materials Included",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && dakshina.isNotBlank()) {
                        onConfirm(
                            service.copy(
                                ritualName = name.trim(),
                                fixedDakshina = dakshina.toDoubleOrNull() ?: service.fixedDakshina,
                                description = desc.trim(),
                                materialsIncluded = materialsIncluded
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("save_ritual_button")
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_edit_ritual_button")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteRitualConfirmationDialog(
    service: PurohitService,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.WarningAmber,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text("Delete Ritual Service?")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Are you sure you want to permanently remove this ritual?",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• ${service.ritualName} (₹${service.fixedDakshina.toInt()})",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Pilgrims will no longer be able to book this service under your profile.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.testTag("confirm_delete_ritual_button")
            ) {
                Text("Delete Service")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_delete_ritual_button")
            ) {
                Text("Keep Service")
            }
        }
    )
}
