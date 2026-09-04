package com.prayagraj.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.prayagraj.app.data.model.FleetVehicleDto
import com.prayagraj.app.data.model.RouteFareDto
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TransportMonthlyRevenueProjectionCard(
    vehicle: FleetVehicleDto?,
    fares: List<RouteFareDto>,
    modifier: Modifier = Modifier
) {
    // Assumptions for daily operations:
    // Capacity of the vehicle per trip (defaults to vehicle.capacity or 12 persons for typical boat/van)
    val passengerCapacityPerTrip = vehicle?.capacity ?: 12
    
    // Average trips per operating day (slider / adjustable state)
    var tripsPerDay by remember { mutableFloatStateOf(6f) }
    
    // Average occupancy rate assumption (e.g., 85% normal mela occupancy)
    var occupancyRatePercent by remember { mutableFloatStateOf(85f) }

    // Weighted or average standard fare across configured routes
    val avgStandardFare = remember(fares) {
        if (fares.isNotEmpty()) {
            fares.map { it.standardFare }.average()
        } else {
            120.0 // Default baseline Kumbh boat/e-rickshaw standard fare (₹120)
        }
    }

    val avgCappedFare = remember(fares) {
        if (fares.isNotEmpty()) {
            fares.map { it.govCappedMaxFare }.average()
        } else {
            180.0
        }
    }

    // Assumed daily passengers = tripsPerDay * passengerCapacityPerTrip * (occupancyRatePercent / 100)
    val dailyPassengers = remember(tripsPerDay, passengerCapacityPerTrip, occupancyRatePercent) {
        tripsPerDay * passengerCapacityPerTrip * (occupancyRatePercent / 100f)
    }

    // Daily earnings based on standard fares
    val dailyRevenue = remember(dailyPassengers, avgStandardFare) {
        dailyPassengers * avgStandardFare
    }

    // Monthly estimated earnings (30 operating days)
    val monthlyProjectedRevenue = remember(dailyRevenue) {
        dailyRevenue * 30.0
    }

    // 100% capacity standard potential
    val monthlyFullCapacityRevenue = remember(tripsPerDay, passengerCapacityPerTrip, avgStandardFare) {
        (tripsPerDay * passengerCapacityPerTrip * avgStandardFare) * 30.0
    }

    // Peak Shahi Snan day potential (using official max government cap)
    val monthlyPeakCapPotential = remember(tripsPerDay, passengerCapacityPerTrip, avgCappedFare) {
        (tripsPerDay * passengerCapacityPerTrip * avgCappedFare) * 30.0
    }

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("transport_monthly_revenue_projection_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PolishPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = PolishPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Monthly Revenue Projection",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = vehicle?.title ?: "Active Transport Asset",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PolishPrimary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "30 Days Cycle",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = PolishPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Headline Numbers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = currencyFormatter.format(monthlyProjectedRevenue),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = PolishPrimary
                        )
                    )
                    Text(
                        text = "At ${(occupancyRatePercent).toInt()}% load (${dailyPassengers.toInt()} passengers/day)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Est. Daily Earnings",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${currencyFormatter.format(dailyRevenue)}/day",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(12.dp))

            // Parameter Controls (Assumed Capacity & Trips)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Trips: ${tripsPerDay.toInt()} round trips",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                )
                Text(
                    text = "Cap: $passengerCapacityPerTrip per trip",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Slider(
                value = tripsPerDay,
                onValueChange = { tripsPerDay = it },
                valueRange = 2f..16f,
                steps = 13,
                modifier = Modifier.fillMaxWidth()
            )

            // Scenarios comparison (85% vs 100% vs Peak Cap)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Avg Std Fare",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "₹${avgStandardFare.toInt()}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PolishTeal
                            )
                        )
                        Text(
                            text = "${fares.size} active routes",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "100% Full Load",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currencyFormatter.format(monthlyFullCapacityRevenue),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PolishGreen
                            )
                        )
                        Text(
                            text = "Max standard load",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Peak Gov Cap",
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
                            text = "Capped tariff limit",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}
