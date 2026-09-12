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
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.prayagraj.app.data.model.AccommodationDto
import com.prayagraj.app.data.model.RoomInventoryDto
import com.prayagraj.app.ui.components.ProfilePhotoCaptureBox
import com.prayagraj.app.ui.viewmodel.AccommodationManagementViewModel
import com.prayagraj.app.ui.viewmodel.PropertyUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccommodationManagementScreen(
    viewModel: AccommodationManagementViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Accommodation & Inventory") }) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is PropertyUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is PropertyUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadPropertyData() }) {
                            Text("Retry")
                        }
                    }
                }
                is PropertyUiState.Success -> {
                    if (state.property == null) {
                        CreatePropertyForm(
                            onCreateProperty = { name, type, address, contact, license, imageUrl ->
                                viewModel.createProperty(name, type, address, contact, license, imageUrl)
                            }
                        )
                    } else {
                        PropertyDashboard(
                            property = state.property,
                            rooms = state.rooms,
                            onAddRoom = { cat, total, base, peak, amenities ->
                                viewModel.addRoom(state.property.id!!, cat, total, base, peak, amenities)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreatePropertyForm(
    onCreateProperty: (name: String, type: String, address: String, contact: String, license: String, imageUrl: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Dharamshala") }
    var address by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var license by remember { mutableStateOf("") }
    var capturedPhotoUri by remember { mutableStateOf<String?>(null) }

    val propertyTypes = listOf("Dharamshala", "Budget Hotel", "Tent City", "Guest House")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Register Accommodation Property", style = MaterialTheme.typography.titleLarge)

        // Camera photo capture for Property
        ProfilePhotoCaptureBox(
            photoUriString = capturedPhotoUri,
            title = "Property Photo (Camera)",
            subtitle = "Take a photo of the entrance, facade, or rooms using your device camera",
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
            label = { Text("Property Name *") },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Property Category", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            propertyTypes.forEach { item ->
                FilterChip(
                    selected = type == item,
                    onClick = { type = item },
                    label = { Text(item) }
                )
            }
        }

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Full Address *") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = contact,
            onValueChange = { contact = it },
            label = { Text("Contact Number *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = license,
            onValueChange = { license = it },
            label = { Text("Govt License / Registration ID (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { onCreateProperty(name, type, address, contact, license, capturedPhotoUri) },
            enabled = name.isNotBlank() && address.isNotBlank() && contact.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Save Property Profile")
        }
    }
}

@Composable
fun PropertyDashboard(
    property: AccommodationDto,
    rooms: List<RoomInventoryDto>,
    onAddRoom: (category: String, total: Int, base: Double, peak: Double, amenities: List<String>) -> Unit
) {
    var showAddRoomDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (!property.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = property.imageUrl,
                            contentDescription = "Property Photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Hotel, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(property.propertyName, style = MaterialTheme.typography.titleMedium)
                        if (property.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.Verified, contentDescription = "Verified", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Text("Type: ${property.propertyType}", style = MaterialTheme.typography.bodyMedium)
                    Text("Address: ${property.address}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Room Inventory", style = MaterialTheme.typography.titleMedium)
                Button(onClick = { showAddRoomDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Room Category")
                }
            }
        }

        items(rooms) { room ->
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(room.roomCategory, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Total Rooms: ${room.totalRooms} | Available: ${room.availableRooms}")
                    Text("Base Tariff: ₹${room.baseTariff} | Peak Tariff: ₹${room.peakMelaTariff}")
                    Text("Amenities: ${room.amenities.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }

    if (showAddRoomDialog) {
        AddRoomDialog(
            onDismiss = { showAddRoomDialog = false },
            onConfirm = { cat, total, base, peak, amenities ->
                onAddRoom(cat, total, base, peak, amenities)
                showAddRoomDialog = false
            }
        )
    }
}

@Composable
fun AddRoomDialog(
    onDismiss: () -> Unit,
    onConfirm: (category: String, total: Int, base: Double, peak: Double, amenities: List<String>) -> Unit
) {
    var category by remember { mutableStateOf("") }
    var totalRooms by remember { mutableStateOf("1") }
    var baseTariff by remember { mutableStateOf("1000") }
    var peakTariff by remember { mutableStateOf("2500") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Room Category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category Name (e.g. Deluxe Non-AC)") }
                )
                OutlinedTextField(
                    value = totalRooms,
                    onValueChange = { totalRooms = it },
                    label = { Text("Total Room Count") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = baseTariff,
                    onValueChange = { baseTariff = it },
                    label = { Text("Standard Daily Tariff (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = peakTariff,
                    onValueChange = { peakTariff = it },
                    label = { Text("Peak Festival Tariff (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        category,
                        totalRooms.toIntOrNull() ?: 1,
                        baseTariff.toDoubleOrNull() ?: 0.0,
                        peakTariff.toDoubleOrNull() ?: 0.0,
                        listOf("Clean Linen", "Hot Water")
                    )
                },
                enabled = category.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
