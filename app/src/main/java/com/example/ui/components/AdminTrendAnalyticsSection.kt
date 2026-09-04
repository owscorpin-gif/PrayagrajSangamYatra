package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class DailyTrendPoint(
    val dayLabel: String,
    val bookingCount: Int,
    val dakshinaAmount: Double,
    val dateStr: String
)

data class ServiceCategoryStat(
    val categoryName: String,
    val percentage: Float,
    val count: Int,
    val color: Color
)

enum class AnalyticsTimeframe(val label: String) {
    SEVEN_DAYS("Last 7 Days"),
    THIRTY_DAYS("Last 30 Days"),
    KUMBH_PEAK("Kumbh Snan Peaks")
}

@Composable
fun AdminTrendAnalyticsSection(
    modifier: Modifier = Modifier,
    roleTitle: String = "Purohit"
) {
    var selectedTimeframe by remember { mutableStateOf(AnalyticsTimeframe.SEVEN_DAYS) }
    var selectedBarIndex by remember { mutableStateOf<Int?>(6) } // Default to latest day

    val trendData = remember(selectedTimeframe) {
        when (selectedTimeframe) {
            AnalyticsTimeframe.SEVEN_DAYS -> listOf(
                DailyTrendPoint("Mon", 12, 6200.0, "24 Aug"),
                DailyTrendPoint("Tue", 15, 8400.0, "25 Aug"),
                DailyTrendPoint("Wed", 18, 11500.0, "26 Aug"),
                DailyTrendPoint("Thu", 22, 14200.0, "27 Aug"),
                DailyTrendPoint("Fri", 28, 19800.0, "28 Aug"),
                DailyTrendPoint("Sat", 45, 34200.0, "29 Aug (Somvati)"),
                DailyTrendPoint("Sun", 38, 28500.0, "30 Aug (Snan)")
            )
            AnalyticsTimeframe.THIRTY_DAYS -> listOf(
                DailyTrendPoint("W1", 85, 58000.0, "1-7 Aug"),
                DailyTrendPoint("W2", 110, 79500.0, "8-14 Aug"),
                DailyTrendPoint("W3", 145, 112000.0, "15-21 Aug"),
                DailyTrendPoint("W4", 178, 142500.0, "22-28 Aug")
            )
            AnalyticsTimeframe.KUMBH_PEAK -> listOf(
                DailyTrendPoint("Makar", 95, 82000.0, "14 Jan (Makar Sankranti)"),
                DailyTrendPoint("Paush", 118, 98000.0, "25 Jan (Paush Purnima)"),
                DailyTrendPoint("Mauni", 280, 245000.0, "09 Feb (Mauni Amavasya Peak)"),
                DailyTrendPoint("Basant", 160, 138000.0, "14 Feb (Basant Panchami)"),
                DailyTrendPoint("Maghi", 195, 172000.0, "24 Feb (Maghi Purnima)"),
                DailyTrendPoint("Maha", 210, 189000.0, "08 Mar (Maha Shivaratri)")
            )
        }
    }

    val categoryStats = remember {
        listOf(
            ServiceCategoryStat("Sangam Snan & Sankalp", 0.42f, 185, PolishPrimary),
            ServiceCategoryStat("Pind Daan & Tarpan", 0.28f, 122, PolishTeal),
            ServiceCategoryStat("Rudrabhishek Puja", 0.18f, 79, Saffron500),
            ServiceCategoryStat("Vedic Chhatra / Boat Rites", 0.12f, 54, Color(0xFF6750A4))
        )
    }

    val totalBookings = trendData.sumOf { it.bookingCount }
    val totalRevenue = trendData.sumOf { it.dakshinaAmount }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admin_trend_analytics_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = PolishCardSurface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with title and timeframe chip selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = PolishPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Booking & Revenue Trends",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Real-time demand & verified pilgrim traffic",
                        style = MaterialTheme.typography.bodySmall,
                        color = PolishTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Timeframe Segmented Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnalyticsTimeframe.values().forEach { timeframe ->
                    val isSelected = selectedTimeframe == timeframe
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedTimeframe = timeframe
                            selectedBarIndex = trendData.lastIndex.coerceAtLeast(0)
                        },
                        label = { Text(timeframe.label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PolishPrimaryContainer,
                            selectedLabelColor = PolishPrimary
                        ),
                        modifier = Modifier.testTag("timeframe_chip_${timeframe.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // KPI Metric Summary Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiMetricCard(
                    title = "Total Bookings",
                    value = "$totalBookings",
                    subtitle = "+24% this period",
                    icon = Icons.Default.EventAvailable,
                    accentColor = PolishPrimary,
                    modifier = Modifier.weight(1f)
                )

                KpiMetricCard(
                    title = "Total Dakshina",
                    value = "₹${totalRevenue.toInt()}",
                    subtitle = "100% Direct to Panda",
                    icon = Icons.Default.AccountBalanceWallet,
                    accentColor = PolishTeal,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Active Selected Point Callout Banner
            selectedBarIndex?.let { index ->
                if (index in trendData.indices) {
                    val point = trendData[index]
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PolishPrimaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = point.dateStr,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PolishTextPrimary
                                )
                                Text(
                                    text = "${point.bookingCount} Pilgrims confirmed",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PolishPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = "₹${point.dakshinaAmount.toInt()}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = PolishPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Bar & Line Chart Canvas
            Text(
                text = "Daily Booking Volume & Peak Surges",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = PolishTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            BookingBarChart(
                points = trendData,
                selectedIndex = selectedBarIndex,
                onSelectBar = { selectedBarIndex = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .testTag("booking_bar_chart")
            )

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider(color = PolishBorder)

            Spacer(modifier = Modifier.height(16.dp))

            // Service Category Distribution Breakdown
            Text(
                text = "Popular Rituals & Request Share",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = PolishTextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            categoryStats.forEach { stat ->
                ServiceProgressRow(stat = stat)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun KpiMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = PolishBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = PolishTextSecondary
                )
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = PolishTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = PolishTeal,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun BookingBarChart(
    points: List<DailyTrendPoint>,
    selectedIndex: Int?,
    onSelectBar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    val maxVal = (points.maxOfOrNull { it.bookingCount } ?: 1).coerceAtLeast(10)

    val primaryBarColor = PolishPrimary
    val selectedBarColor = Saffron500
    val unselectedBarColor = PolishPrimaryContainer.copy(alpha = 0.7f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        points.forEachIndexed { index, point ->
            val isSelected = selectedIndex == index
            val heightRatio = (point.bookingCount.toFloat() / maxVal).coerceIn(0.1f, 1f)

            val animatedHeight by animateFloatAsState(
                targetValue = heightRatio,
                animationSpec = tween(durationMillis = 600),
                label = "bar_height_$index"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onSelectBar(index) }
                    .padding(horizontal = 4.dp)
            ) {
                // Top Count Badge if selected
                if (isSelected) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = selectedBarColor,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = "${point.bookingCount}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Text(
                        text = "${point.bookingCount}",
                        fontSize = 10.sp,
                        color = PolishTextSecondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // Bar Shape
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(animatedHeight * 0.72f)
                        .background(
                            brush = if (isSelected) {
                                Brush.verticalGradient(
                                    colors = listOf(Saffron500, PolishPrimary)
                                )
                            } else {
                                Brush.verticalGradient(
                                    colors = listOf(
                                        primaryBarColor.copy(alpha = 0.85f),
                                        unselectedBarColor
                                    )
                                )
                            },
                            shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 2.dp, bottomEnd = 2.dp)
                        )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // X-Axis Day Label
                Text(
                    text = point.dayLabel,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) PolishPrimary else PolishTextSecondary
                )
            }
        }
    }
}

@Composable
fun ServiceProgressRow(
    stat: ServiceCategoryStat
) {
    val animatedProgress by animateFloatAsState(
        targetValue = stat.percentage,
        animationSpec = tween(durationMillis = 800),
        label = "progress_${stat.categoryName}"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(stat.color, shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stat.categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = PolishTextPrimary
                )
            }
            Text(
                text = "${(stat.percentage * 100).toInt()}% (${stat.count})",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = PolishTextSecondary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = stat.color,
            trackColor = PolishBackground
        )
    }
}
