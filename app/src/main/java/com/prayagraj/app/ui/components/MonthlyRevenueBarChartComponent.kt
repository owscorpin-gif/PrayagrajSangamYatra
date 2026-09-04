package com.prayagraj.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.prayagraj.app.data.model.RoomInventoryDto
import java.text.NumberFormat
import java.util.Locale

data class RoomCategoryRevenueBar(
    val categoryName: String,
    val totalRooms: Int,
    val availableRooms: Int,
    val baseTariff: Double,
    val peakTariff: Double,
    val monthlyBaseRevenue100: Double, // totalRooms * baseTariff * 30
    val monthlyProjectedRevenue85: Double, // monthlyBaseRevenue100 * 0.85
    val monthlyCurrentBookedRevenue: Double, // bookedRooms * baseTariff * 30
    val monthlyPeakCapRevenue: Double, // totalRooms * peakTariff * 30
    val colorGradient: List<Color>
)

enum class RevenueProjectionMode(val label: String, val description: String) {
    STANDARD_100("100% Capacity", "Full 30-day capacity at base tariff"),
    MELA_85("85% Mela Avg", "Expected average pilgrim occupancy"),
    CURRENT_BOOKINGS("Current Booked", "Real-time active room bookings"),
    PEAK_GOV_CAP("Peak Gov Cap", "Max allowable tariff under UP Tourism cap")
}

@Composable
fun MonthlyRevenueBarChartComponent(
    rooms: List<RoomInventoryDto>,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf(RevenueProjectionMode.STANDARD_100) }
    var selectedCategoryIndex by remember { mutableStateOf<Int?>(null) }

    // Fallback room data if empty
    val effectiveRooms = remember(rooms) {
        if (rooms.isNotEmpty()) rooms else listOf(
            RoomInventoryDto(
                propertyId = "demo-prop",
                roomCategory = "Standard Non-AC Room",
                totalRooms = 35,
                availableRooms = 8,
                baseTariff = 950.0,
                peakMelaTariff = 1500.0,
                amenities = listOf("Attached Bath", "24/7 Water", "Geyser")
            ),
            RoomInventoryDto(
                propertyId = "demo-prop",
                roomCategory = "Deluxe AC Family Suite",
                totalRooms = 25,
                availableRooms = 2,
                baseTariff = 2200.0,
                peakMelaTariff = 3200.0,
                amenities = listOf("AC", "Geyser", "Wi-Fi", "River View")
            ),
            RoomInventoryDto(
                propertyId = "demo-prop",
                roomCategory = "Dormitory Pilgrim Hall (10 Bed)",
                totalRooms = 20,
                availableRooms = 5,
                baseTariff = 450.0,
                peakMelaTariff = 750.0,
                amenities = listOf("Locker", "Common Bath", "Hot Water")
            ),
            RoomInventoryDto(
                propertyId = "demo-prop",
                roomCategory = "Super Deluxe Sangam Facing",
                totalRooms = 12,
                availableRooms = 0,
                baseTariff = 3800.0,
                peakMelaTariff = 5000.0,
                amenities = listOf("AC", "Direct Sangam View", "Breakfast", "Priority Ghat Pass")
            )
        )
    }

    val colorPalettes = listOf(
        listOf(PolishPrimary, Saffron600),
        listOf(PolishTeal, PolishGreen),
        listOf(Saffron500, PolishAmber),
        listOf(PolishPrimaryDark, Saffron700),
        listOf(Emerald600, PolishTeal)
    )

    val barData = remember(effectiveRooms) {
        effectiveRooms.mapIndexed { idx, room ->
            val booked = (room.totalRooms - room.availableRooms).coerceAtLeast(0)
            val base100 = room.totalRooms * room.baseTariff * 30.0
            RoomCategoryRevenueBar(
                categoryName = room.roomCategory,
                totalRooms = room.totalRooms,
                availableRooms = room.availableRooms,
                baseTariff = room.baseTariff,
                peakTariff = room.peakMelaTariff,
                monthlyBaseRevenue100 = base100,
                monthlyProjectedRevenue85 = base100 * 0.85,
                monthlyCurrentBookedRevenue = booked * room.baseTariff * 30.0,
                monthlyPeakCapRevenue = room.totalRooms * room.peakMelaTariff * 30.0,
                colorGradient = colorPalettes[idx % colorPalettes.size]
            )
        }
    }

    // Determine current value for each category based on selected mode
    fun getValueForMode(item: RoomCategoryRevenueBar, mode: RevenueProjectionMode): Double {
        return when (mode) {
            RevenueProjectionMode.STANDARD_100 -> item.monthlyBaseRevenue100
            RevenueProjectionMode.MELA_85 -> item.monthlyProjectedRevenue85
            RevenueProjectionMode.CURRENT_BOOKINGS -> item.monthlyCurrentBookedRevenue
            RevenueProjectionMode.PEAK_GOV_CAP -> item.monthlyPeakCapRevenue
        }
    }

    val totalMonthlyProjected = remember(barData, selectedMode) {
        barData.sumOf { getValueForMode(it, selectedMode) }
    }

    val maxCategoryRevenue = remember(barData, selectedMode) {
        val max = barData.maxOfOrNull { getValueForMode(it, selectedMode) } ?: 1.0
        if (max > 0) max else 1.0
    }

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }
    }

    val selectedBarItem = remember(selectedCategoryIndex, barData) {
        selectedCategoryIndex?.let { if (it in barData.indices) barData[it] else null }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_revenue_bar_chart_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(PolishPrimary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Projected Monthly Revenue",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = "Breakdown across room categories • 30-Day Cycle",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PolishPrimaryContainer
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = currencyFormatter.format(totalMonthlyProjected),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = PolishOnPrimaryContainer
                            )
                        )
                        Text(
                            text = "Total Projected",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = PolishOnPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Scenario Mode Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                RevenueProjectionMode.entries.forEach { mode ->
                    val isSelected = selectedMode == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedMode = mode
                        },
                        label = {
                            Text(
                                text = mode.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            )
                        },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = selectedMode.description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Inspector Card when tapping a bar
            selectedBarItem?.let { item ->
                val categoryVal = getValueForMode(item, selectedMode)
                val sharePercent = if (totalMonthlyProjected > 0) ((categoryVal / totalMonthlyProjected) * 100).toInt() else 0

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.categoryName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${item.totalRooms} Rooms (₹${item.baseTariff.toInt()} std / ₹${item.peakTariff.toInt()} peak)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = currencyFormatter.format(categoryVal),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PolishPrimary
                                )
                            )
                            Text(
                                text = "$sharePercent% of total revenue",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // Compose Shape-based Bar Chart Visualization
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("revenue_bars_container"),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                barData.forEachIndexed { idx, bar ->
                    val value = getValueForMode(bar, selectedMode)
                    val shareRatio = (value / maxCategoryRevenue).toFloat().coerceIn(0.04f, 1.0f)
                    val isSelected = selectedCategoryIndex == idx

                    // Animated progress fill using spring animation
                    val animatedRatio by animateFloatAsState(
                        targetValue = shareRatio,
                        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessLow),
                        label = "bar_ratio_$idx"
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                selectedCategoryIndex = if (selectedCategoryIndex == idx) null else idx
                            }
                            .then(
                                if (isSelected) {
                                    Modifier.border(1.5.dp, PolishPrimary, RoundedCornerShape(10.dp))
                                } else Modifier
                            )
                            .padding(4.dp)
                    ) {
                        // Bar Label Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Brush.horizontalGradient(bar.colorGradient))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = bar.categoryName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currencyFormatter.format(value),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) PolishPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Visual Progress Bar Container using simple Compose Shapes
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(22.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            // Animated Filled Bar with Gradient
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedRatio)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Brush.horizontalGradient(bar.colorGradient))
                            ) {
                                // Subtle inner highlight shine shape
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.dp)
                                        .align(Alignment.TopCenter)
                                        .background(Color.White.copy(alpha = 0.35f))
                                )
                            }

                            // Percentage inside or next to the bar
                            val percentage = if (totalMonthlyProjected > 0) ((value / totalMonthlyProjected) * 100).toInt() else 0
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${bar.totalRooms} rooms",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (animatedRatio > 0.35f) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Text(
                                    text = "$percentage%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (animatedRatio > 0.85f) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(12.dp))

            // Footer Summary Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.TouchApp,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Tap any bar to inspect category breakdown",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "${barData.size} Categories",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
