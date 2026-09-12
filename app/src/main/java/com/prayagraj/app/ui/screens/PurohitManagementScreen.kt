package com.prayagraj.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.prayagraj.app.data.model.PurohitServiceDto
import com.prayagraj.app.data.model.PriestDetails
import com.prayagraj.app.ui.components.ProfilePhotoCaptureBox
import com.prayagraj.app.ui.viewmodel.PurohitManagementUiState
import com.prayagraj.app.ui.viewmodel.PurohitManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurohitManagementScreen(
    viewModel: PurohitManagementViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Tirth Purohit & Rituals") }) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is PurohitManagementUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is PurohitManagementUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadPurohitData() }) {
                            Text("Retry")
                        }
                    }
                }
                is PurohitManagementUiState.Success -> {
                    if (state.priestDetails == null || state.priestDetails.fullName.isBlank()) {
                        RegisterPriestDetailsForm(
                            onRegister = { name, specialization, exp, languages, phone, regId, photoUri ->
                                viewModel.registerPriestDetails(name, specialization, exp, languages, phone, regId, photoUri)
                            }
                        )
                    } else {
                        PurohitDashboard(
                            priest = state.priestDetails,
                            services = state.services,
                            onPhotoUpdated = { photoUri ->
                                viewModel.updatePriestPhoto(photoUri)
                            },
                            onServiceCreated = {
                                viewModel.loadPurohitData()
                            },
                            onAddService = { name, dakshina, desc, duration, materials ->
                                viewModel.addService(name, dakshina, desc, duration, materials)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegisterPriestDetailsForm(
    onRegister: (
        name: String,
        specialization: String,
        experienceYears: Int,
        languages: List<String>,
        phoneNumber: String,
        registrationId: String,
        photoUri: String?
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("Vedic Karmakand") }
    var experienceYears by remember { mutableStateOf("10") }
    val selectedLanguages = remember { mutableStateListOf("Hindi", "Sanskrit") }
    var phoneNumber by remember { mutableStateOf("") }
    var registrationId by remember { mutableStateOf("") }
    var capturedPhotoUri by remember { mutableStateOf<String?>(null) }

    val specializations = listOf(
        "Vedic Karmakand",
        "Pitru Tarpan & Pind Daan",
        "Rudrabhishek & Hawan",
        "Jyotish & Kundali",
        "Sangam Snan Sankalp"
    )

    val availableLanguages = listOf("Hindi", "Sanskrit", "Bhojpuri", "English", "Bengali", "Gujarati", "Marathi")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Register Priest Profile", style = MaterialTheme.typography.titleLarge)
        Text(
            "Enter your Vedic lineage details, ritual specialization, experience, and spoken languages to accept pilgrim bookings at Prayagraj.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Priest Profile Photo Capture
        ProfilePhotoCaptureBox(
            photoUriString = capturedPhotoUri,
            title = "Priest Profile Photo",
            subtitle = "Take a photo of yourself in traditional attire using your device camera",
            isCircular = true,
            onPhotoCaptured = { uri ->
                capturedPhotoUri = uri.toString()
            },
            onPhotoRemoved = {
                capturedPhotoUri = null
            }
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Priest / Acharya Full Name *") },
            placeholder = { Text("e.g. Pt. Ramakant Mishra Shastri") },
            modifier = Modifier.fillMaxWidth()
        )

        // Specialization Chips
        Text("Ritual Specialization *", style = MaterialTheme.typography.labelLarge)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            specializations.forEach { spec ->
                FilterChip(
                    selected = specialization == spec,
                    onClick = { specialization = spec },
                    label = { Text(spec) }
                )
            }
        }

        // Experience in Years
        OutlinedTextField(
            value = experienceYears,
            onValueChange = { experienceYears = it },
            label = { Text("Experience in Ritual Practice (Years) *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        // Languages Spoken
        Text("Languages Spoken *", style = MaterialTheme.typography.labelLarge)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            availableLanguages.forEach { lang ->
                val isSelected = selectedLanguages.contains(lang)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) {
                            if (selectedLanguages.size > 1) selectedLanguages.remove(lang)
                        } else {
                            selectedLanguages.add(lang)
                        }
                    },
                    label = { Text(lang) }
                )
            }
        }

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Contact Phone Number *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = registrationId,
            onValueChange = { registrationId = it },
            label = { Text("Tirth Purohit Sabha / Govt Reg. ID (Optional)") },
            placeholder = { Text("e.g. PRY-KMB-2025-0142") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                onRegister(
                    name,
                    specialization,
                    experienceYears.toIntOrNull() ?: 5,
                    selectedLanguages.toList(),
                    phoneNumber,
                    registrationId,
                    capturedPhotoUri
                )
            },
            enabled = name.isNotBlank() && phoneNumber.isNotBlank() && selectedLanguages.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Register Priest Profile")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurohitDashboard(
    priest: PriestDetails,
    services: List<PurohitServiceDto>,
    onPhotoUpdated: ((String) -> Unit)? = null,
    onServiceCreated: (() -> Unit)? = null,
    onAddService: (name: String, dakshina: Double, desc: String, duration: Int, materials: Boolean) -> Unit
) {
    var showAddServiceDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Priest Profile Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (!priest.photoUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = priest.photoUri,
                                    contentDescription = "Priest Photo",
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            } else {
                                Icon(
                                    Icons.Default.SelfImprovement,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Column {
                                Text(priest.fullName, style = MaterialTheme.typography.titleMedium)
                                if (priest.isVerified) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Verified,
                                            contentDescription = "Verified",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "Verified Tirth Purohit",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Live camera photo update option inside dashboard
                    if (onPhotoUpdated != null) {
                        ProfilePhotoCaptureBox(
                            photoUriString = priest.photoUri,
                            title = "Update Profile Photo",
                            subtitle = "Take a fresh photo with camera",
                            isCircular = false,
                            onPhotoCaptured = { uri ->
                                onPhotoUpdated(uri.toString())
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${priest.specialization} • ${priest.experienceYears}+ years exp",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Language,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Languages: ${priest.languages.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (priest.phoneNumber.isNotBlank()) {
                        Text(
                            "Contact: ${priest.phoneNumber}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (priest.registrationId.isNotBlank()) {
                        Text(
                            "Reg ID: ${priest.registrationId}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Ritual Services Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Listed Ritual Services", style = MaterialTheme.typography.titleMedium)
                Button(onClick = { showAddServiceDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Ritual")
                }
            }
        }

        // Ritual Service Items
        items(services) { service ->
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(service.poojaName, style = MaterialTheme.typography.titleMedium)
                            Text(
                                service.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Text(
                            "₹${service.baseDakshina}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (!service.description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(service.description, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            "Duration: ${(service.durationHours * 60).toInt()} mins",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "Samagri: ${if (service.samagriIncluded) "Included" else "+₹${service.samagriExtraCost}"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }

    if (showAddServiceDialog) {
        ModalBottomSheet(
            onDismissRequest = { showAddServiceDialog = false }
        ) {
            PurohitServiceForm(
                onSuccess = {
                    showAddServiceDialog = false
                    onServiceCreated?.invoke()
                }
            )
        }
    }
}

@Composable
fun AddRitualServiceDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, dakshina: Double, desc: String, duration: Int, materials: Boolean) -> Unit
) {
    var ritualName by remember { mutableStateOf("") }
    var dakshina by remember { mutableStateOf("501") }
    var description by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("45") }
    var materialsIncluded by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Ritual Offering") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = ritualName,
                    onValueChange = { ritualName = it },
                    label = { Text("Ritual / Puja Name *") },
                    placeholder = { Text("e.g. Pitru Tarpan & Pind Daan") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dakshina,
                    onValueChange = { dakshina = it },
                    label = { Text("Fixed Dakshina (₹) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Ritual Description & Vidhi") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Estimated Duration (Minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = materialsIncluded,
                        onCheckedChange = { materialsIncluded = it }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Puja Samagri (Materials) Included")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        ritualName,
                        dakshina.toDoubleOrNull() ?: 501.0,
                        description,
                        duration.toIntOrNull() ?: 45,
                        materialsIncluded
                    )
                },
                enabled = ritualName.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
