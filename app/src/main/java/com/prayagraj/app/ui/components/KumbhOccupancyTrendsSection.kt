package com.prayagraj.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class DailyOccupancyPoint(
    val dayNumber: Int,
    val dateLabel: String,
    val snanEvent: String? = null,
    val occupancyPercent: Int, // 0 to 100
    val bookedRooms: Int,
    val totalCapacity: Int,
    val avgTariff: Double,
    val isPeakShahiSnan: Boolean = false
)

enum class OccupancyViewMode(val label: String) {
    FULL_MELA("Full 45 Days"),
    SHAHI_SNAN_PEAKS("Shahi Snan Peaks"),
    HIGH_SURGE("Surge Days (>85%)")
}

@Composable
fun KumbhOccupancyTrendsSection(
    totalRoomsCapacity: Int = 92,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(OccupancyViewMode.FULL_MELA) }
    var hoveredIndex by remember { mutableStateOf<Int?>(14) } // Default to Mauni Amavasya peak

    // 45 Days of Kumbh Mela daily room occupancy dataset
    val fullMelaData = remember(totalRoomsCapacity) {
        val total = if (totalRoomsCapacity > 0) totalRoomsCapacity else 92
        listOf(
            DailyOccupancyPoint(1, "13 Jan", "Paush Purnima (Snan Eve)", 74, (total * 0.74).toInt(), total, 850.0, true),
            DailyOccupancyPoint(2, "14 Jan", "Makar Sankranti (Shahi Snan)", 95, (total * 0.95).toInt(), total, 1200.0, true),
            DailyOccupancyPoint(3, "15 Jan", "Kalpvas Opening Phase", 82, (total * 0.82).toInt(), total, 900.0),
            DailyOccupancyPoint(4, "16 Jan", "Mela Day 4", 76, (total * 0.76).toInt(), total, 800.0),
            DailyOccupancyPoint(5, "17 Jan", "Mela Day 5", 72, (total * 0.72).toInt(), total, 780.0),
            DailyOccupancyPoint(6, "18 Jan", "Weekend Pilgrim Influx", 84, (total * 0.84).toInt(), total, 880.0),
            DailyOccupancyPoint(7, "19 Jan", "Mela Day 7", 78, (total * 0.78).toInt(), total, 820.0),
            DailyOccupancyPoint(8, "20 Jan", "Mela Day 8", 75, (total * 0.75).toInt(), total, 800.0),
            DailyOccupancyPoint(9, "21 Jan", "Mela Day 9", 77, (total * 0.77).toInt(), total, 810.0),
            DailyOccupancyPoint(10, "22 Jan", "Mela Day 10", 80, (total * 0.80).toInt(), total, 840.0),
            DailyOccupancyPoint(11, "23 Jan", "Netaji Jayanti Rush", 83, (total * 0.83).toInt(), total, 860.0),
            DailyOccupancyPoint(12, "24 Jan", "Mela Day 12", 81, (total * 0.81).toInt(), total, 850.0),
            DailyOccupancyPoint(13, "25 Jan", "Paush Purnima Culmination", 88, (total * 0.88).toInt(), total, 980.0, true),
            DailyOccupancyPoint(14, "26 Jan", "Republic Day Long Weekend", 92, (total * 0.92).toInt(), total, 1100.0),
            DailyOccupancyPoint(15, "27 Jan", "Pre-Mauni Amavasya Setup", 94, (total * 0.94).toInt(), total, 1180.0),
            DailyOccupancyPoint(16, "28 Jan", "Mauni Amavasya Eve Rush", 97, (total * 0.97).toInt(), total, 1250.0, true),
            DailyOccupancyPoint(17, "29 Jan", "Mauni Amavasya (Mahasnanam)", 99, (total * 0.99).toInt(), total, 1350.0, true),
            DailyOccupancyPoint(18, "30 Jan", "Post-Mauni Departure", 89, (total * 0.89).toInt(), total, 1050.0),
            DailyOccupancyPoint(19, "31 Jan", "Mela Day 19", 78, (total * 0.78).toInt(), total, 850.0),
            DailyOccupancyPoint(20, "01 Feb", "Early Feb Weekend", 85, (total * 0.85).toInt(), total, 900.0),
            DailyOccupancyPoint(21, "02 Feb", "Basant Panchami Eve", 89, (total * 0.89).toInt(), total, 980.0),
            DailyOccupancyPoint(22, "03 Feb", "Basant Panchami (Shahi Snan)", 96, (total * 0.96).toInt(), total, 1220.0, true),
            DailyOccupancyPoint(23, "04 Feb", "Mela Day 23", 83, (total * 0.83).toInt(), total, 900.0),
            DailyOccupancyPoint(24, "05 Feb", "Mela Day 24", 77, (total * 0.77).toInt(), total, 820.0),
            DailyOccupancyPoint(25, "06 Feb", "Mela Day 25", 79, (total * 0.79).toInt(), total, 840.0),
            DailyOccupancyPoint(26, "07 Feb", "Achala Saptami Snan", 87, (total * 0.87).toInt(), total, 950.0, true),
            DailyOccupancyPoint(27, "08 Feb", "Weekend Pilgrim Spike", 88, (total * 0.88).toInt(), total, 960.0),
            DailyOccupancyPoint(28, "09 Feb", "Mela Day 28", 80, (total * 0.80).toInt(), total, 850.0),
            DailyOccupancyPoint(29, "10 Feb", "Jaya Ekadashi Rituals", 84, (total * 0.84).toInt(), total, 890.0),
            DailyOccupancyPoint(30, "11 Feb", "Maghi Purnima Eve", 91, (total * 0.91).toInt(), total, 1080.0),
            DailyOccupancyPoint(31, "12 Feb", "Maghi Purnima (Kalpvas Finale)", 98, (total * 0.98).toInt(), total, 1300.0, true),
            DailyOccupancyPoint(32, "13 Feb", "Kalpvas Departure Flow", 85, (total * 0.85).toInt(), total, 920.0),
            DailyOccupancyPoint(33, "14 Feb", "Mid-Feb Weekend Flow", 82, (total * 0.82).toInt(), total, 880.0),
            DailyOccupancyPoint(34, "15 Feb", "Mela Day 34", 75, (total * 0.75).toInt(), total, 800.0),
            DailyOccupancyPoint(35, "16 Feb", "Mela Day 35", 73, (total * 0.73).toInt(), total, 780.0),
            DailyOccupancyPoint(36, "17 Feb", "Mela Day 36", 76, (total * 0.76).toInt(), total, 810.0),
            DailyOccupancyPoint(37, "18 Feb", "Mela Day 37", 78, (total * 0.78).toInt(), total, 820.0),
            DailyOccupancyPoint(38, "19 Feb", "Mela Day 38", 80, (total * 0.80).toInt(), total, 850.0),
            DailyOccupancyPoint(39, "20 Feb", "Mela Day 39", 82, (total * 0.82).toInt(), total, 870.0),
            DailyOccupancyPoint(40, "21 Feb", "Pre-Shivratri Weekend", 86, (total * 0.86).toInt(), total, 920.0),
            DailyOccupancyPoint(41, "22 Feb", "Mela Day 41", 84, (total * 0.84).toInt(), total, 900.0),
            DailyOccupancyPoint(42, "23 Feb", "Mela Day 42", 85, (total * 0.85).toInt(), total, 910.0),
            DailyOccupancyPoint(43, "24 Feb", "Maha Shivratri Eve", 92, (total * 0.92).toInt(), total, 1120.0),
            DailyOccupancyPoint(44, "25 Feb", "Maha Shivratri (Closing Snan)", 97, (total * 0.97).toInt(), total, 1280.0, true),
            DailyOccupancyPoint(45, "26 Feb", "Kumbh Mela Concluding Day", 70, (total * 0.70).toInt(), total, 750.0)
        )
    }

    val displayData = remember(selectedFilter, fullMelaData) {
        when (selectedFilter) {
            OccupancyViewMode.FULL_MELA -> fullMelaData
            OccupancyViewMode.SHAHI_SNAN_PEAKS -> fullMelaData.filter { it.isPeakShahiSnan }
            OccupancyViewMode.HIGH_SURGE -> fullMelaData.filter { it.occupancyPercent >= 85 }
        }
    }

    // Ensure hovered index stays in bounds
    val activePoint = remember(hoveredIndex, displayData) {
        val idx = hoveredIndex ?: 0
        if (idx in displayData.indices) displayData[idx] else displayData.firstOrNull()
    }

    val avgOccupancy = remember(displayData) {
        if (displayData.isNotEmpty()) displayData.map { it.occupancyPercent }.average().toInt() else 0
    }
    val peakPoint = remember(displayData) {
        displayData.maxByOrNull { it.occupancyPercent }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("kumbh_occupancy_chart_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with badge
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
                            text = "Daily Room Occupancy Trends",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = "Kumbh Mela 45-Day Sacred Influx Cycle",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PolishPrimaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = PolishOnPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$avgOccupancy% Avg",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = PolishOnPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Tabs (D3 style view controls)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OccupancyViewMode.entries.forEach { mode ->
                    val isSelected = selectedFilter == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedFilter = mode
                            hoveredIndex = 0
                        },
                        label = { Text(mode.label, style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = if (isSelected) {
                            {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Tooltip Callout Card (D3 Floating Inspector Style)
            AnimatedVisibility(
                visible = activePoint != null,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200))
            ) {
                activePoint?.let { point ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${point.dateLabel} (Day ${point.dayNumber})",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    if (point.snanEvent != null) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (point.isPeakShahiSnan) Saffron500 else PolishTeal
                                        ) {
                                            Text(
                                                text = if (point.isPeakShahiSnan) "★ Shahi Snan" else "Ritual",
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                if (point.snanEvent != null) {
                                    Text(
                                        text = point.snanEvent,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (point.isPeakShahiSnan) Saffron800 else PolishTeal
                                    )
                                }
                                Text(
                                    text = "${point.bookedRooms} of ${point.totalCapacity} rooms occupied",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                val occColor = when {
                                    point.occupancyPercent >= 95 -> PolishRed
                                    point.occupancyPercent >= 85 -> PolishAmber
                                    else -> PolishGreen
                                }
                                Text(
                                    text = "${point.occupancyPercent}%",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = occColor
                                    )
                                )
                                Text(
                                    text = "₹${point.avgTariff.toInt()}/night",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // D3 Spline Area & Line Canvas Chart
            D3StyleOccupancyCanvas(
                points = displayData,
                selectedIndex = hoveredIndex,
                onSelectIndex = { hoveredIndex = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Timeline Legend & Threshold Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp, 3.dp)
                            .background(PolishPrimary)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Occupancy %", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(12.dp, 2.dp)
                            .background(PolishRed)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("90% Surge Threshold", style = MaterialTheme.typography.labelSmall, color = PolishRed)
                }

                Text(
                    text = "Touch/drag chart to inspect",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Key Milestone Quick Jump Buttons
            Text(
                text = "Major Holy Snan Days (Tap to inspect):",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    Pair("Makar Sankranti", "14 Jan"),
                    Pair("Mauni Amavasya", "29 Jan"),
                    Pair("Basant Panchami", "03 Feb"),
                    Pair("Maghi Purnima", "12 Feb"),
                    Pair("Maha Shivratri", "25 Feb")
                ).forEach { (name, date) ->
                    OutlinedButton(
                        onClick = {
                            val targetIdx = displayData.indexOfFirst { it.dateLabel.startsWith(date) }
                            if (targetIdx != -1) {
                                hoveredIndex = targetIdx
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = date,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                            Text(
                                text = name.split(" ").first(),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // KPI Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryKpiCard(
                    title = "Peak Demand Date",
                    value = peakPoint?.let { "${it.dateLabel} (${it.occupancyPercent}%)" } ?: "29 Jan",
                    subtitle = peakPoint?.snanEvent ?: "Mauni Amavasya",
                    icon = Icons.Default.ElectricBolt,
                    containerColor = Saffron100,
                    modifier = Modifier.weight(1f)
                )
                SummaryKpiCard(
                    title = "High Demand Days",
                    value = "${fullMelaData.count { it.occupancyPercent >= 85 }} of 45",
                    subtitle = "Tariff cap active",
                    icon = Icons.Default.Shield,
                    containerColor = PolishSurface,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun SummaryKpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = containerColor
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = PolishPrimary)
                Spacer(modifier = Modifier.width(4.dp))
                Text(title, style = MaterialTheme.typography.labelSmall, color = PolishTextSecondary)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = PolishTextTertiary)
        }
    }
}

@Composable
fun D3StyleOccupancyCanvas(
    points: List<DailyOccupancyPoint>,
    selectedIndex: Int?,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    val strokeColor = PolishPrimary
    val areaStartColor = PolishPrimary.copy(alpha = 0.35f)
    val areaEndColor = PolishPrimary.copy(alpha = 0.02f)
    val gridLineColor = PolishBorderDarker.copy(alpha = 0.4f)
    val surgeLineColor = PolishRed.copy(alpha = 0.7f)

    Canvas(
        modifier = modifier
            .testTag("d3_occupancy_chart_canvas")
            .pointerInput(points) {
                detectTapGestures { offset ->
                    val spacing = size.width / (points.size - 1).coerceAtLeast(1)
                    val index = (offset.x / spacing).toInt().coerceIn(0, points.size - 1)
                    onSelectIndex(index)
                }
            }
            .pointerInput(points) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val spacing = size.width / (points.size - 1).coerceAtLeast(1)
                    val index = (change.position.x / spacing).toInt().coerceIn(0, points.size - 1)
                    onSelectIndex(index)
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val bottomPadding = 24.dp.toPx()
        val topPadding = 16.dp.toPx()
        val chartHeight = height - bottomPadding - topPadding

        // 1. Draw horizontal grid lines (0%, 25%, 50%, 75%, 100%)
        val gridLevels = listOf(0.25f, 0.5f, 0.75f, 1.0f)
        gridLevels.forEach { level ->
            val y = topPadding + chartHeight * (1f - level)
            drawLine(
                color = gridLineColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )
        }

        // 2. Draw 90% Surge Alert Line
        val surgeY = topPadding + chartHeight * (1f - 0.90f)
        drawLine(
            color = surgeLineColor,
            start = Offset(0f, surgeY),
            end = Offset(width, surgeY),
            strokeWidth = 1.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
        )

        // 3. Calculate (X, Y) coordinates for each point
        val spacing = width / (points.size - 1).coerceAtLeast(1)
        val coords = points.mapIndexed { idx, point ->
            val x = idx * spacing
            val normalizedVal = (point.occupancyPercent / 100f).coerceIn(0f, 1f)
            val y = topPadding + chartHeight * (1f - normalizedVal)
            Offset(x, y)
        }

        // 4. Build smooth cubic Bezier path (D3 Monotone Spline approximation)
        val splinePath = Path().apply {
            if (coords.isNotEmpty()) {
                moveTo(coords[0].x, coords[0].y)
                for (i in 0 until coords.size - 1) {
                    val p0 = coords[i]
                    val p1 = coords[i + 1]
                    val controlX = (p0.x + p1.x) / 2f
                    cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                }
            }
        }

        // 5. Fill Area Gradient below curve (d3.area())
        val areaPath = Path().apply {
            addPath(splinePath)
            lineTo(coords.last().x, topPadding + chartHeight)
            lineTo(coords.first().x, topPadding + chartHeight)
            close()
        }

        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
                colors = listOf(areaStartColor, areaEndColor),
                startY = topPadding,
                endY = topPadding + chartHeight
            )
        )

        // 6. Draw Stroke Curve (d3.line())
        drawPath(
            path = splinePath,
            color = strokeColor,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        )

        // 7. Shahi Snan Milestone markers (pulsing glowing pins on peak days)
        points.forEachIndexed { idx, pt ->
            if (pt.isPeakShahiSnan) {
                val coord = coords[idx]
                // Halo circle
                drawCircle(
                    color = Saffron400.copy(alpha = 0.35f),
                    radius = 8.dp.toPx(),
                    center = coord
                )
                // Solid center pin
                drawCircle(
                    color = Saffron600,
                    radius = 4.dp.toPx(),
                    center = coord
                )
            }
        }

        // 8. Draw active hovered vertical scrubber line & beacon (D3 Voronoi / Tooltip)
        if (selectedIndex != null && selectedIndex in coords.indices) {
            val selectedCoord = coords[selectedIndex]

            // Vertical indicator guide line
            drawLine(
                color = PolishPrimary,
                start = Offset(selectedCoord.x, topPadding),
                end = Offset(selectedCoord.x, topPadding + chartHeight),
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
            )

            // Outer glowing beacon
            drawCircle(
                color = PolishPrimary.copy(alpha = 0.25f),
                radius = 12.dp.toPx(),
                center = selectedCoord
            )
            // Inner circle
            drawCircle(
                color = Color.White,
                radius = 6.dp.toPx(),
                center = selectedCoord
            )
            drawCircle(
                color = PolishPrimary,
                radius = 4.dp.toPx(),
                center = selectedCoord
            )
        }

        // 9. X-axis baseline
        drawLine(
            color = PolishBorderDarker,
            start = Offset(0f, topPadding + chartHeight),
            end = Offset(width, topPadding + chartHeight),
            strokeWidth = 1.5f
        )
    }
}
