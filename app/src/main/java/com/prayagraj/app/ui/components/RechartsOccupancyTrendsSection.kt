package com.prayagraj.app.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class RechartsDailyOccupancy(
    val dayNumber: Int,
    val date: String,
    val dayName: String,
    val occupancyPercent: Int,
    val bookedRooms: Int,
    val totalRooms: Int,
    val avgTariff: Int,
    val isShahiSnan: Boolean = false,
    val snanName: String = "",
    val bookingTrend: String = "Normal"
)

/**
 * Interface exposed to JavaScript within the Recharts WebView
 */
class RechartsWebInterface(
    private val onPointHovered: (RechartsDailyOccupancy?) -> Unit
) {
    private val json = Json { ignoreUnknownKeys = true }

    @JavascriptInterface
    fun onDataPointSelected(jsonString: String) {
        try {
            if (jsonString.isBlank() || jsonString == "null") {
                onPointHovered(null)
            } else {
                val point = json.decodeFromString<RechartsDailyOccupancy>(jsonString)
                onPointHovered(point)
            }
        } catch (_: Exception) {
            onPointHovered(null)
        }
    }
}

/**
 * Generates the self-contained HTML payload embedding React, ReactDOM, and Recharts (UMD)
 * with responsive Area/Line hybrid charts, active tooltip inspection, and 90% surge reference line.
 */
fun buildRechartsHtml(dataPoints: List<RechartsDailyOccupancy>): String {
    val jsonString = Json.encodeToString(dataPoints)
    return """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
  <title>Kumbh Occupancy Recharts</title>
  <!-- Load React 18 & ReactDOM -->
  <script src="https://unpkg.com/react@18.3.1/umd/react.production.min.js" crossorigin></script>
  <script src="https://unpkg.com/react-dom@18.3.1/umd/react-dom.production.min.js" crossorigin></script>
  <!-- Load Prop-types required by Recharts UMD -->
  <script src="https://unpkg.com/prop-types@15.8.1/prop-types.min.js"></script>
  <!-- Load Recharts UMD -->
  <script src="https://unpkg.com/recharts@2.12.7/umd/Recharts.min.js"></script>
  <style>
    * {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
      -webkit-tap-highlight-color: transparent;
      user-select: none;
    }
    body, html {
      width: 100%;
      height: 100%;
      background: transparent;
      overflow: hidden;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
    }
    #root {
      width: 100%;
      height: 100%;
      padding: 4px 6px 0 0;
    }
    .custom-tooltip {
      background: rgba(26, 28, 30, 0.95);
      backdrop-filter: blur(8px);
      color: #ffffff;
      padding: 8px 12px;
      border-radius: 10px;
      border: 1px solid rgba(255, 153, 51, 0.4);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.25);
      font-size: 11px;
    }
    .tooltip-title {
      font-weight: 700;
      color: #ffb74d;
      font-size: 12px;
      margin-bottom: 2px;
    }
    .tooltip-val {
      font-weight: 800;
      font-size: 14px;
      color: #4dd0e1;
    }
    .tooltip-snan {
      color: #ffcc80;
      font-weight: 600;
      margin-top: 2px;
    }
  </style>
</head>
<body>
  <div id="root"></div>

  <script>
    (function() {
      const data = $jsonString;
      const {
        ResponsiveContainer,
        AreaChart,
        Area,
        Line,
        XAxis,
        YAxis,
        CartesianGrid,
        Tooltip,
        ReferenceLine,
        Dot
      } = window.Recharts;

      const CustomTooltipContent = ({ active, payload }) => {
        if (active && payload && payload.length) {
          const item = payload[0].payload;
          if (window.AndroidBridge && window.AndroidBridge.onDataPointSelected) {
            window.AndroidBridge.onDataPointSelected(JSON.stringify(item));
          }
          return React.createElement(
            'div',
            { className: 'custom-tooltip' },
            React.createElement('div', { className: 'tooltip-title' }, item.date + ' (Day ' + item.dayNumber + ')'),
            React.createElement('div', null, 
              React.createElement('span', { className: 'tooltip-val' }, item.occupancyPercent + '% Occupancy')
            ),
            React.createElement('div', { style: { color: '#cfd8dc', fontSize: '10px' } }, item.bookedRooms + ' / ' + item.totalRooms + ' Rooms Booked'),
            React.createElement('div', { style: { color: '#a5d6a7', fontSize: '10px' } }, 'Avg: ₹' + item.avgTariff + '/night'),
            item.snanName ? React.createElement('div', { className: 'tooltip-snan' }, '★ ' + item.snanName) : null
          );
        }
        return null;
      };

      const CustomizedShahiDot = (props) => {
        const { cx, cy, payload } = props;
        if (!payload.isShahiSnan) return null;
        return React.createElement(
          'g',
          null,
          React.createElement('circle', {
            cx: cx,
            cy: cy,
            r: 7,
            fill: 'rgba(255, 112, 67, 0.4)',
            stroke: 'none'
          }),
          React.createElement('circle', {
            cx: cx,
            cy: cy,
            r: 3.5,
            fill: '#d84315',
            stroke: '#ffffff',
            strokeWidth: 1.5
          })
        );
      };

      function OccupancyChartApp() {
        return React.createElement(
          ResponsiveContainer,
          { width: '100%', height: '100%' },
          React.createElement(
            AreaChart,
            {
              data: data,
              margin: { top: 12, right: 14, left: -22, bottom: 4 }
            },
            React.createElement(
              'defs',
              null,
              React.createElement(
                'linearGradient',
                { id: 'colorOccupancy', x1: '0', y1: '0', x2: '0', y2: '1' },
                React.createElement('stop', { offset: '0%', stopColor: '#c25e00', stopOpacity: 0.45 }),
                React.createElement('stop', { offset: '95%', stopColor: '#c25e00', stopOpacity: 0.02 })
              )
            ),
            React.createElement(CartesianGrid, {
              strokeDasharray: '3 3',
              stroke: '#e0e0e0',
              vertical: false
            }),
            React.createElement(XAxis, {
              dataKey: 'date',
              tick: { fontSize: 10, fill: '#757575' },
              interval: 4,
              axisLine: { stroke: '#cccccc' },
              tickLine: false
            }),
            React.createElement(YAxis, {
              domain: [0, 100],
              ticks: [0, 25, 50, 75, 90, 100],
              tick: { fontSize: 9, fill: '#757575' },
              axisLine: false,
              tickLine: false,
              tickFormatter: (val) => val + '%'
            }),
            React.createElement(Tooltip, {
              content: React.createElement(CustomTooltipContent)
            }),
            React.createElement(ReferenceLine, {
              y: 90,
              stroke: '#d32f2f',
              strokeDasharray: '4 4',
              strokeWidth: 1.5,
              label: {
                value: '90% Cap',
                position: 'right',
                fill: '#d32f2f',
                fontSize: 9,
                fontWeight: 'bold'
              }
            }),
            React.createElement(Area, {
              type: 'monotone',
              dataKey: 'occupancyPercent',
              stroke: '#b84e00',
              strokeWidth: 2.5,
              fillOpacity: 1,
              fill: 'url(#colorOccupancy)',
              dot: React.createElement(CustomizedShahiDot),
              activeDot: {
                r: 6,
                fill: '#b84e00',
                stroke: '#ffffff',
                strokeWidth: 2
              }
            })
          )
        );
      }

      ReactDOM.render(React.createElement(OccupancyChartApp), document.getElementById('root'));
    })();
  </script>
</body>
</html>
""".trimIndent()
}

/**
 * Composable function integrating Recharts via Android WebView to visualize daily
 * room occupancy trends over the next 30 days for the HotelAdminDashboardScreen.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RechartsOccupancyTrendsSection(
    totalRoomsCapacity: Int = 92,
    modifier: Modifier = Modifier
) {
    var selectedRange by remember { mutableIntStateOf(30) } // Next 30 Days default
    var activeHoveredPoint by remember { mutableStateOf<RechartsDailyOccupancy?>(null) }
    var isWebViewLoaded by remember { mutableStateOf(false) }

    // Next 30 Days of daily room occupancy data points for Kumbh Mela
    val thirtyDaysData = remember(totalRoomsCapacity) {
        val total = if (totalRoomsCapacity > 0) totalRoomsCapacity else 92
        listOf(
            RechartsDailyOccupancy(1, "13 Jan", "Mon", 74, (total * 0.74).toInt(), total, 850, true, "Paush Purnima Eve", "Rising"),
            RechartsDailyOccupancy(2, "14 Jan", "Tue", 96, (total * 0.96).toInt(), total, 1250, true, "Makar Sankranti (Shahi Snan)", "Surge Peak"),
            RechartsDailyOccupancy(3, "15 Jan", "Wed", 84, (total * 0.84).toInt(), total, 920, false, "", "High"),
            RechartsDailyOccupancy(4, "16 Jan", "Thu", 78, (total * 0.78).toInt(), total, 820, false, "", "Steady"),
            RechartsDailyOccupancy(5, "17 Jan", "Fri", 75, (total * 0.75).toInt(), total, 800, false, "", "Steady"),
            RechartsDailyOccupancy(6, "18 Jan", "Sat", 85, (total * 0.85).toInt(), total, 900, false, "", "Weekend Spike"),
            RechartsDailyOccupancy(7, "19 Jan", "Sun", 82, (total * 0.82).toInt(), total, 860, false, "", "High"),
            RechartsDailyOccupancy(8, "20 Jan", "Mon", 76, (total * 0.76).toInt(), total, 810, false, "", "Steady"),
            RechartsDailyOccupancy(9, "21 Jan", "Tue", 78, (total * 0.78).toInt(), total, 830, false, "", "Steady"),
            RechartsDailyOccupancy(10, "22 Jan", "Wed", 81, (total * 0.81).toInt(), total, 850, false, "", "Moderate"),
            RechartsDailyOccupancy(11, "23 Jan", "Thu", 84, (total * 0.84).toInt(), total, 880, false, "", "Moderate"),
            RechartsDailyOccupancy(12, "24 Jan", "Fri", 83, (total * 0.83).toInt(), total, 870, false, "", "Moderate"),
            RechartsDailyOccupancy(13, "25 Jan", "Sat", 89, (total * 0.89).toInt(), total, 1000, true, "Paush Purnima Finale", "High Surge"),
            RechartsDailyOccupancy(14, "26 Jan", "Sun", 93, (total * 0.93).toInt(), total, 1150, false, "Republic Day Long Weekend", "Surge Peak"),
            RechartsDailyOccupancy(15, "27 Jan", "Mon", 95, (total * 0.95).toInt(), total, 1200, false, "Pre-Mauni Amavasya Influx", "Surge Peak"),
            RechartsDailyOccupancy(16, "28 Jan", "Tue", 98, (total * 0.98).toInt(), total, 1300, true, "Mauni Amavasya Eve Rush", "Extreme Surge"),
            RechartsDailyOccupancy(17, "29 Jan", "Wed", 100, total, total, 1400, true, "Mauni Amavasya (Mahasnanam)", "Full House (100%)"),
            RechartsDailyOccupancy(18, "30 Jan", "Thu", 91, (total * 0.91).toInt(), total, 1100, false, "Post-Mauni Departure Flow", "High"),
            RechartsDailyOccupancy(19, "31 Jan", "Fri", 80, (total * 0.80).toInt(), total, 880, false, "", "Moderate"),
            RechartsDailyOccupancy(20, "01 Feb", "Sat", 86, (total * 0.86).toInt(), total, 920, false, "Feb First Weekend", "High"),
            RechartsDailyOccupancy(21, "02 Feb", "Sun", 90, (total * 0.90).toInt(), total, 1050, false, "Basant Panchami Eve", "Surge Peak"),
            RechartsDailyOccupancy(22, "03 Feb", "Mon", 97, (total * 0.97).toInt(), total, 1260, true, "Basant Panchami (Shahi Snan)", "Extreme Surge"),
            RechartsDailyOccupancy(23, "04 Feb", "Tue", 85, (total * 0.85).toInt(), total, 910, false, "", "Moderate"),
            RechartsDailyOccupancy(24, "05 Feb", "Wed", 79, (total * 0.79).toInt(), total, 840, false, "", "Steady"),
            RechartsDailyOccupancy(25, "06 Feb", "Thu", 80, (total * 0.80).toInt(), total, 850, false, "", "Steady"),
            RechartsDailyOccupancy(26, "07 Feb", "Fri", 88, (total * 0.88).toInt(), total, 980, true, "Achala Saptami Snan", "High Surge"),
            RechartsDailyOccupancy(27, "08 Feb", "Sat", 89, (total * 0.89).toInt(), total, 990, false, "Weekend Spike", "High Surge"),
            RechartsDailyOccupancy(28, "09 Feb", "Sun", 82, (total * 0.82).toInt(), total, 880, false, "", "Moderate"),
            RechartsDailyOccupancy(29, "10 Feb", "Mon", 85, (total * 0.85).toInt(), total, 900, false, "Jaya Ekadashi Rituals", "High"),
            RechartsDailyOccupancy(30, "11 Feb", "Tue", 92, (total * 0.92).toInt(), total, 1100, true, "Maghi Purnima Eve", "Surge Peak")
        )
    }

    // Default inspector point to Mauni Amavasya
    LaunchedEffect(thirtyDaysData) {
        activeHoveredPoint = thirtyDaysData.firstOrNull { it.occupancyPercent == 100 } ?: thirtyDaysData[16]
    }

    val avgOccupancy = remember(thirtyDaysData) {
        if (thirtyDaysData.isNotEmpty()) thirtyDaysData.map { it.occupancyPercent }.average().toInt() else 0
    }
    val surgeDaysCount = remember(thirtyDaysData) {
        thirtyDaysData.count { it.occupancyPercent >= 90 }
    }
    val peakDay = remember(thirtyDaysData) {
        thirtyDaysData.maxByOrNull { it.occupancyPercent }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("recharts_occupancy_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Recharts & Live Trend badges
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
                            text = "30-Day Room Occupancy Forecast",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = "Interactive Recharts Component • Kumbh Mela",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PolishPrimaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
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
                            text = "$avgOccupancy% 30D Avg",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = PolishOnPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Active Inspector Callout Card (receives real-time hover callbacks from Recharts JS bridge)
            AnimatedVisibility(
                visible = activeHoveredPoint != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                activeHoveredPoint?.let { point ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
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
                                        text = "${point.date} (${point.dayName}) - Day ${point.dayNumber}",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    if (point.isShahiSnan) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Saffron500
                                        ) {
                                            Text(
                                                text = "★ Shahi Snan",
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                if (point.snanName.isNotBlank()) {
                                    Text(
                                        text = point.snanName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (point.isShahiSnan) Saffron800 else PolishTeal
                                    )
                                }
                                Text(
                                    text = "${point.bookedRooms} of ${point.totalRooms} rooms booked (${point.bookingTrend})",
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
                                    text = "Avg: ₹${point.avgTariff}/night",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Recharts Web-Rendered Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Transparent)
            ) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            setBackgroundColor(0) // Transparent background
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                cacheMode = WebSettings.LOAD_DEFAULT
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                setSupportZoom(false)
                            }
                            addJavascriptInterface(
                                RechartsWebInterface { point ->
                                    if (point != null) {
                                        activeHoveredPoint = point
                                    }
                                },
                                "AndroidBridge"
                            )
                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                }
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isWebViewLoaded = true
                                }
                            }
                            webChromeClient = WebChromeClient()
                            val html = buildRechartsHtml(thirtyDaysData)
                            loadDataWithBaseURL("https://unpkg.com", html, "text/html", "UTF-8", null)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (!isWebViewLoaded) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = PolishPrimary,
                            strokeWidth = 2.5.dp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Legend Row
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
                    Text("Recharts Area & Spline", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(12.dp, 2.dp)
                            .background(PolishRed)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("90% Surge Cap", style = MaterialTheme.typography.labelSmall, color = PolishRed)
                }

                Text(
                    text = "Hover / tap chart points",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 30-Day Milestone Snan Quick Jump
            Text(
                text = "Key Royal Bathing (Shahi Snan) Dates:",
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
                    Pair("Maghi Purnima", "11 Feb")
                ).forEach { (name, date) ->
                    OutlinedButton(
                        onClick = {
                            val match = thirtyDaysData.firstOrNull { it.date.startsWith(date) }
                            if (match != null) {
                                activeHoveredPoint = match
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
                    title = "Peak Capacity Date",
                    value = peakDay?.let { "${it.date} (${it.occupancyPercent}%)" } ?: "29 Jan",
                    subtitle = peakDay?.snanName ?: "Mauni Amavasya",
                    icon = Icons.Default.ElectricBolt,
                    containerColor = Saffron100,
                    modifier = Modifier.weight(1f)
                )
                SummaryKpiCard(
                    title = "Surge (>90%) Days",
                    value = "$surgeDaysCount of 30 Days",
                    subtitle = "Gov Tariff Cap Active",
                    icon = Icons.Default.Shield,
                    containerColor = PolishSurface,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
