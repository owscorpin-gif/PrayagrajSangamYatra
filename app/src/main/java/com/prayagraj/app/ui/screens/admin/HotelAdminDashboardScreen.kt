package com.prayagraj.app.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale
import com.prayagraj.app.data.model.AccommodationDto
import com.prayagraj.app.data.model.RoomInventoryDto
import com.prayagraj.app.ui.components.KumbhOccupancyTrendsSection
import com.prayagraj.app.ui.components.MonthlyRevenueBarChartComponent
import com.prayagraj.app.ui.components.RechartsOccupancyTrendsSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelAdminDashboardScreen(
    property: AccommodationDto?,
    rooms: List<RoomInventoryDto>,
    onAddRoom: (RoomInventoryDto) -> Unit,
    onBack: () -> Unit
) {
    var showAddRoomDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Occupancy Trends, 1: Rooms & Tariffs
    var chartEngine by remember { mutableIntStateOf(0) } // 0: Recharts (Web), 1: D3 Native (Canvas)

    val totalPropertyRooms = remember(rooms) {
        val sum = rooms.sumOf { it.totalRooms }
        if (sum > 0) sum else 92
    }

    val totalAvailableRooms = remember(rooms) {
        rooms.sumOf { it.availableRooms }
    }

    // Daily revenue calculation at 100% capacity using standard base tariffs
    val dailyBaseCapacityRevenue = remember(rooms) {
        rooms.sumOf { it.totalRooms * it.baseTariff }
    }

    // Estimated monthly revenue (30 days) across different occupancy projections
    val monthlyRevenue100 = remember(dailyBaseCapacityRevenue) { dailyBaseCapacityRevenue * 30.0 }
    val monthlyRevenue85 = remember(dailyBaseCapacityRevenue) { dailyBaseCapacityRevenue * 30.0 * 0.85 } // 85% expected mela average
    val monthlyRevenueCurrent = remember(rooms) {
        val bookedRoomsDailyRevenue = rooms.sumOf { (it.totalRooms - it.availableRooms) * it.baseTariff }
        bookedRoomsDailyRevenue * 30.0
    }

    // Peak mela potential revenue calculation using government capped peak rates
    val monthlyPeakCapPotential = remember(rooms) {
        rooms.sumOf { it.totalRooms * it.peakMelaTariff } * 30.0
    }

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hotel & Dharamshala Admin") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddRoomDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Room Category") },
                modifier = Modifier.testTag("add_room_category_fab")
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Property Header Card
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = property?.propertyName ?: "Sri Prayag Teerth Yatri Niwas",
                            style = MaterialTheme.typography.titleLarge
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text(if (property?.isVerified == true) "Verified" else "Unverified") },
                            leadingIcon = {
                                Icon(
                                    if (property?.isVerified == true) Icons.Default.Verified else Icons.Default.Warning,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                    Text(
                        text = "Type: ${property?.propertyType ?: "Dharamshala"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "License/Reg ID: ${property?.registrationLicenseId ?: "UP-TOU-PRY-2024-551"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Navigation: Occupancy Trends vs Room Inventory
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Occupancy Trends") },
                    icon = { Icon(Icons.Default.ShowChart, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Rooms & Tariffs (${rooms.size})") },
                    icon = { Icon(Icons.Default.MeetingRoom, contentDescription = null) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> {
                    // TAB 0: Daily Occupancy Trends Visualization (Recharts 30-Day Forecast & D3 Native 45-Day Canvas)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Engine Switcher: Recharts (Web) vs D3 (Native)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = chartEngine == 0,
                                onClick = { chartEngine = 0 },
                                label = { Text("Recharts (30 Days)", style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.QueryStats,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = chartEngine == 1,
                                onClick = { chartEngine = 1 },
                                label = { Text("D3 Canvas (45 Days)", style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Timeline,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (chartEngine == 0) {
                            RechartsOccupancyTrendsSection(
                                totalRoomsCapacity = totalPropertyRooms,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            KumbhOccupancyTrendsSection(
                                totalRoomsCapacity = totalPropertyRooms,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Revenue projection summary beneath chart
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("trends_monthly_revenue_summary_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.MonetizationOn,
                                            contentDescription = null,
                                            tint = PolishPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Estimated Monthly Revenue",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Based on standard base tariff • 30 days projection",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = currencyFormatter.format(monthlyRevenue100),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = PolishPrimary
                                        )
                                    )
                                    Text(
                                        text = "${currencyFormatter.format(dailyBaseCapacityRevenue)}/day",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                1 -> {
                    // TAB 1: Room Inventory & Government Tariff Cap Compliance
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Estimated Monthly Revenue Summary Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("monthly_revenue_summary_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.AccountBalanceWallet,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Estimated Monthly Revenue",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "30 Days Standard",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Text(
                                            text = currencyFormatter.format(monthlyRevenue100),
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                        Text(
                                            text = "At 100% capacity (${totalPropertyRooms} rooms across ${rooms.size} categories)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Daily Rate",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${currencyFormatter.format(dailyBaseCapacityRevenue)}/day",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(12.dp))

                                // Occupancy Projection Scenarios
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surface
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "At 85% Mela Avg",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = currencyFormatter.format(monthlyRevenue85),
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = PolishAmber
                                                )
                                            )
                                            Text(
                                                text = "Expected normal",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }

                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surface
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "Current Bookings",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = currencyFormatter.format(monthlyRevenueCurrent),
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = PolishGreen
                                                )
                                            )
                                            Text(
                                                text = "${totalPropertyRooms - totalAvailableRooms}/$totalPropertyRooms booked",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }

                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surface
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "Peak Cap Potential",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = currencyFormatter.format(monthlyPeakCapPotential),
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Saffron700
                                                )
                                            )
                                            Text(
                                                text = "Under Gov Caps",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Projected Monthly Revenue Bar Chart Component
                        MonthlyRevenueBarChartComponent(
                            rooms = rooms,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Official Anti-Scam Advisory Card
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Tariff Cap Advisory: All room tariffs must comply with UP Tourism peak pricing limits. Direct UPI scam links are strictly prohibited.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Room Categories & Tariff Cards",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(rooms) { room ->
                                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = room.roomCategory,
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                text = "${room.availableRooms}/${room.totalRooms} Available",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        HorizontalDivider()
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("Standard Base Tariff", style = MaterialTheme.typography.labelMedium)
                                                Text("₹${room.baseTariff.toInt()} / night", style = MaterialTheme.typography.bodyMedium)
                                            }
                                            Column {
                                                Text("Peak Mela Tariff Cap", style = MaterialTheme.typography.labelMedium)
                                                Text(
                                                    "₹${room.peakMelaTariff.toInt()} / night",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            room.amenities.forEach { amenity ->
                                                SuggestionChip(
                                                    onClick = {},
                                                    label = { Text(amenity, style = MaterialTheme.typography.labelSmall) }
                                                )
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

        if (showAddRoomDialog) {
            AddRoomDialog(
                propertyId = property?.id ?: "",
                onDismiss = { showAddRoomDialog = false },
                onConfirm = { newRoom ->
                    onAddRoom(newRoom)
                    showAddRoomDialog = false
                }
            )
        }
    }
}

@Composable
fun AddRoomDialog(
    propertyId: String,
    onDismiss: () -> Unit,
    onConfirm: (RoomInventoryDto) -> Unit
) {
    var category by remember { mutableStateOf("") }
    var totalCount by remember { mutableStateOf("") }
    var basePrice by remember { mutableStateOf("") }
    var peakPrice by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Room Category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (e.g. AC Double Room)") }
                )
                OutlinedTextField(
                    value = totalCount,
                    onValueChange = { totalCount = it },
                    label = { Text("Total Number of Rooms") }
                )
                OutlinedTextField(
                    value = basePrice,
                    onValueChange = { basePrice = it },
                    label = { Text("Standard Base Tariff (₹)") }
                )
                OutlinedTextField(
                    value = peakPrice,
                    onValueChange = { peakPrice = it },
                    label = { Text("Peak Mela Tariff Cap (₹)") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (category.isNotEmpty() && basePrice.isNotEmpty()) {
                        val count = totalCount.toIntOrNull() ?: 1
                        onConfirm(
                            RoomInventoryDto(
                                propertyId = propertyId,
                                roomCategory = category,
                                totalRooms = count,
                                availableRooms = count,
                                baseTariff = basePrice.toDoubleOrNull() ?: 0.0,
                                peakMelaTariff = peakPrice.toDoubleOrNull() ?: 0.0
                            )
                        )
                    }
                }
            ) {
                Text("Save Room Category")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
