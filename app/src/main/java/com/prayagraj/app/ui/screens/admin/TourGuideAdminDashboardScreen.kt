package com.prayagraj.app.ui.screens.admin

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
import androidx.compose.ui.unit.dp
import com.prayagraj.app.data.model.TourGuideProfileDto
import com.prayagraj.app.data.model.TourPackageDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TourGuideAdminDashboardScreen(
    guideProfile: TourGuideProfileDto?,
    packages: List<TourPackageDto>,
    onAddPackage: (TourPackageDto) -> Unit,
    onToggleActive: (packageId: String, currentStatus: Boolean) -> Unit,
    onBack: () -> Unit
) {
    var showAddPackageDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tour & Heritage Guide Admin") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("tour_admin_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddPackageDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Create Tour Package") },
                modifier = Modifier.testTag("create_tour_package_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Guide Badge & Credentials Header
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("guide_credentials_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = guideProfile?.badgeLevel ?: "Official Tour Guide",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "License No: ${guideProfile?.licenseNumber ?: "Pending"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        AssistChip(
                            onClick = {},
                            label = { Text(if (guideProfile?.isVerified == true) "Verified" else "Under Review") },
                            leadingIcon = {
                                Icon(
                                    if (guideProfile?.isVerified == true) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                                    contentDescription = null
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = true,
                            onClick = {},
                            label = { Text("${guideProfile?.experienceYears ?: 1}+ Yrs Experience") }
                        )
                        guideProfile?.languagesSpoken?.forEach { lang ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(lang, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Customized Tour Packages", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            // Packages List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("tour_packages_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(packages) { pkg ->
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tour_package_card_${pkg.id ?: "new"}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = pkg.packageTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Switch(
                                    checked = pkg.isActive,
                                    onCheckedChange = { onToggleActive(pkg.id ?: "", pkg.isActive) },
                                    modifier = Modifier.testTag("package_toggle_switch_${pkg.id ?: ""}")
                                )
                            }

                            Text(
                                text = "Duration: ${pkg.durationHours} Hours • Max Group: ${pkg.maxGroupSize} People",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Itinerary Highlights:", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = pkg.itineraryHighlights,
                                style = MaterialTheme.typography.bodySmall
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "₹${pkg.pricePerPerson.toInt()} / Person",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${pkg.includedServices.size} Inclusions",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showAddPackageDialog) {
            AddTourPackageDialog(
                guideProfileId = guideProfile?.id ?: "",
                onDismiss = { showAddPackageDialog = false },
                onConfirm = { newPkg ->
                    onAddPackage(newPkg)
                    showAddPackageDialog = false
                }
            )
        }
    }
}

@Composable
fun AddTourPackageDialog(
    guideProfileId: String,
    onDismiss: () -> Unit,
    onConfirm: (TourPackageDto) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var groupSize by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var highlights by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Tour Package") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Package Title (e.g. Heritage Circuit)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_tour_title_input")
                )
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration (Hours)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_tour_duration_input")
                )
                OutlinedTextField(
                    value = groupSize,
                    onValueChange = { groupSize = it },
                    label = { Text("Max Group Capacity") },
                    modifier = Modifier.fillMaxWidth().testTag("add_tour_capacity_input")
                )
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price Per Person (₹)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_tour_price_input")
                )
                OutlinedTextField(
                    value = highlights,
                    onValueChange = { highlights = it },
                    label = { Text("Itinerary Highlights & Key Stops") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("add_tour_highlights_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotEmpty() && price.isNotEmpty()) {
                        onConfirm(
                            TourPackageDto(
                                guideProfileId = guideProfileId,
                                packageTitle = title,
                                durationHours = duration.toIntOrNull() ?: 3,
                                maxGroupSize = groupSize.toIntOrNull() ?: 5,
                                pricePerPerson = price.toDoubleOrNull() ?: 0.0,
                                itineraryHighlights = highlights,
                                includedServices = listOf("Guided Experience", "Site Information")
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("save_package_button")
            ) {
                Text("Save Package")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_package_button")
            ) {
                Text("Cancel")
            }
        }
    )
}
