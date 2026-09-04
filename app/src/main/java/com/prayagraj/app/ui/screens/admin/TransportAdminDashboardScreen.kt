package com.prayagraj.app.ui.screens.admin

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prayagraj.app.data.model.FleetVehicleDto
import com.prayagraj.app.data.model.RouteFareDto
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransportAdminDashboardScreen(
    vehicles: List<FleetVehicleDto>,
    selectedVehicleFares: List<RouteFareDto>,
    onAddRouteFare: (RouteFareDto) -> Unit,
    onBack: () -> Unit
) {
    var showAddFareDialog by remember { mutableStateOf(false) }
    var currentVehicleId by remember { mutableStateOf(vehicles.firstOrNull()?.id ?: "") }
    val currentSelectedVehicle = remember(currentVehicleId, vehicles) {
        vehicles.find { it.id == currentVehicleId } ?: vehicles.firstOrNull()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Boat & Vehicle Admin") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("transport_admin_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddFareDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Fixed Fare Route") },
                modifier = Modifier.testTag("add_fare_route_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Rate Governance Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rate_governance_card")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.DirectionsBoat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Regulated Fare Rate Card",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "To prevent overcharging pilgrims during mela days, fares must not exceed government rate caps.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Total Monthly Revenue Projection Across All Routes
            RevenueProjectionCard(
                routes = selectedVehicleFares,
                vehicleCapacity = currentSelectedVehicle?.capacity ?: 10,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text("Registered Boats & Vehicles", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            // Registered Vehicle Cards
            LazyColumn(
                modifier = Modifier.height(160.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(vehicles) { vehicle ->
                    OutlinedCard(
                        onClick = { currentVehicleId = vehicle.id ?: "" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(vehicle.title, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "Reg No: ${vehicle.registrationNumber} • Cap: ${vehicle.capacity} Persons",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            AssistChip(
                                onClick = {},
                                label = { Text(if (vehicle.isVerified) "Approved" else "Pending") }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Fixed Fare Route Setup", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            // Route Cards List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = selectedVehicleFares,
                    key = { it.id ?: "${it.origin}_${it.destination}_${it.standardFare}" }
                ) { fare ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItemPlacement()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(fare.origin, style = MaterialTheme.typography.titleMedium)
                                    Icon(
                                        Icons.Default.East,
                                        contentDescription = null,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )
                                    Text(fare.destination, style = MaterialTheme.typography.titleMedium)
                                }
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(if (fare.isSharedService) "Shared Ride" else "Private Charter") }
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Offered Fare", style = MaterialTheme.typography.labelSmall)
                                    Text("₹${fare.standardFare.toInt()} / passenger", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                                }
                                Column {
                                    Text("Official Cap Limit", style = MaterialTheme.typography.labelSmall)
                                    Text("₹${fare.govCappedMaxFare.toInt()} Max", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddFareDialog) {
            AddRouteFareDialog(
                vehicleId = currentVehicleId,
                onDismiss = { showAddFareDialog = false },
                onConfirm = { newFare ->
                    onAddRouteFare(newFare)
                    showAddFareDialog = false
                }
            )
        }
    }
}

@Composable
fun AddRouteFareDialog(
    vehicleId: String,
    onDismiss: () -> Unit,
    onConfirm: (RouteFareDto) -> Unit
) {
    var origin by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var standardFare by remember { mutableStateOf("") }
    var maxFareCap by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Route Fare") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = origin,
                    onValueChange = { origin = it },
                    label = { Text("Origin Ghat / Landmark") }
                )
                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text("Destination Ghat / Landmark") }
                )
                OutlinedTextField(
                    value = standardFare,
                    onValueChange = { standardFare = it },
                    label = { Text("Fixed Fare Rate (₹)") }
                )
                OutlinedTextField(
                    value = maxFareCap,
                    onValueChange = { maxFareCap = it },
                    label = { Text("Max Government Tariff Cap (₹)") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (origin.isNotEmpty() && destination.isNotEmpty() && standardFare.isNotEmpty()) {
                        onConfirm(
                            RouteFareDto(
                                vehicleId = vehicleId,
                                origin = origin,
                                destination = destination,
                                standardFare = standardFare.toDoubleOrNull() ?: 0.0,
                                govCappedMaxFare = maxFareCap.toDoubleOrNull() ?: 0.0
                            )
                        )
                    }
                }
            ) {
                Text("Save Route Fare")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * RevenueProjectionCard:
 * Calculates total estimated monthly revenue by summing:
 * (standardFare * dailyPassengerCapacity * 30 days) for all routes in the current list.
 */
@Composable
fun RevenueProjectionCard(
    routes: List<RouteFareDto>,
    vehicleCapacity: Int,
    modifier: Modifier = Modifier,
    tripsPerDay: Int = 4
) {
    // Calculate daily passenger capacity per route
    val dailyPassengerCapacity = vehicleCapacity * tripsPerDay

    // Total monthly projected revenue = sum of (standardFare * dailyPassengerCapacity * 30 days) across all routes
    val totalMonthlyRevenue = routes.sumOf { route ->
        route.standardFare * dailyPassengerCapacity * 30.0
    }

    // Peak Government Capped revenue benchmark
    val peakCapMonthlyRevenue = routes.sumOf { route ->
        route.govCappedMaxFare * dailyPassengerCapacity * 30.0
    }

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }
    }

    ElevatedCard(
        modifier = modifier.testTag("revenue_projection_card"),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Estimated Monthly Revenue",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Formula: Σ (Standard Fare × Daily Passenger Cap × 30 Days)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${routes.size} Routes",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Projected Amount
            Text(
                text = currencyFormatter.format(totalMonthlyRevenue),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Detailed Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Daily Passenger Cap",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$dailyPassengerCapacity / route ($tripsPerDay trips)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Peak Cap Potential",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currencyFormatter.format(peakCapMonthlyRevenue),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

