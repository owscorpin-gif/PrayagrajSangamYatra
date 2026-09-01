package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.theme.*

/**
 * Top-Level Pilgrim Safety & Anti-Scam Dashboard Component
 */
@Composable
fun PilgrimSafetyDashboard(
    onNavigateToOfficialBoats: () -> Unit = {},
    onNavigateToVerifiedPurohits: () -> Unit = {},
    onNavigateToAiAdvisor: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var expandedScamId by remember { mutableStateOf<String?>("scam-unauthorized-priests") }
    var showReportScamDialog by remember { mutableStateOf(false) }
    var checkedItems by remember { mutableStateOf(setOf("check-purohit-id", "check-life-jacket")) }
    var activeTab by remember { mutableIntStateOf(0) } // 0: Scam Advisories, 1: Helplines, 2: Tariff Checker, 3: Safety Checklist

    val filteredScams = remember(selectedCategoryFilter) {
        if (selectedCategoryFilter == "ALL") {
            COMMON_SCAM_ALERTS
        } else {
            COMMON_SCAM_ALERTS.filter { it.categoryTitle.contains(selectedCategoryFilter, ignoreCase = true) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pilgrim_safety_dashboard")
    ) {
        // 1. High-Impact Emergency Hero Banner
        EmergencyHelplineHeroBanner(
            onCallHelpline = { number -> dialPhoneNumber(context, number) },
            onOpenReportModal = { showReportScamDialog = true }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Navigation Tabs for Safety Sections
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = PolishSurface,
            border = BorderStroke(1.dp, PolishBorder)
        ) {
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = PolishSurface,
                contentColor = PolishPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = PolishPrimary
                    )
                }
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = {
                        Text(
                            text = "⚠️ Scam Alerts",
                            fontSize = 11.sp,
                            fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = {
                        Text(
                            text = "📞 Helplines",
                            fontSize = 11.sp,
                            fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = {
                        Text(
                            text = "🏷️ Legal Rates",
                            fontSize = 11.sp,
                            fontWeight = if (activeTab == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    text = {
                        Text(
                            text = "✅ Checklist",
                            fontSize = 11.sp,
                            fontWeight = if (activeTab == 3) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Tab Content
        when (activeTab) {
            0 -> {
                // Section: Common Scam Advisories
                ScamAdvisoriesSection(
                    scams = filteredScams,
                    selectedFilter = selectedCategoryFilter,
                    onSelectFilter = { selectedCategoryFilter = it },
                    expandedScamId = expandedScamId,
                    onToggleExpand = { id ->
                        expandedScamId = if (expandedScamId == id) null else id
                    },
                    onNavigateToOfficialBoats = onNavigateToOfficialBoats,
                    onNavigateToVerifiedPurohits = onNavigateToVerifiedPurohits,
                    onReportClick = { showReportScamDialog = true }
                )
            }
            1 -> {
                // Section: Emergency Contact Directory
                HelplineDirectorySection(
                    onCallHelpline = { dialPhoneNumber(context, it) }
                )
            }
            2 -> {
                // Section: Official Government Approved Tariff Card
                OfficialTariffCheckerSection(
                    onBookBoatClick = onNavigateToOfficialBoats,
                    onBookPurohitClick = onNavigateToVerifiedPurohits
                )
            }
            3 -> {
                // Section: Interactive Pre-Snan Safety Checklist
                SafetyChecklistSection(
                    checkedItemIds = checkedItems,
                    onToggleItem = { id ->
                        checkedItems = if (checkedItems.contains(id)) {
                            checkedItems - id
                        } else {
                            checkedItems + id
                        }
                    }
                )
            }
        }
    }

    // Modal: Incident SOS & Scam Grievance Reporter
    if (showReportScamDialog) {
        ReportScamModalDialog(
            onDismiss = { showReportScamDialog = false },
            onSubmitReport = { location, category, details, phone ->
                showReportScamDialog = false
                Toast.makeText(
                    context,
                    "Grievance filed successfully. Connecting to Kumbh Helpline 1920...",
                    Toast.LENGTH_LONG
                ).show()
                dialPhoneNumber(context, "1920")
            }
        )
    }
}

/**
 * 1. Emergency Helpline Hero Banner with 1920 & SOS Quick Dial
 */
@Composable
fun EmergencyHelplineHeroBanner(
    onCallHelpline: (String) -> Unit,
    onOpenReportModal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("emergency_helpline_hero_banner"),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF7F1D1D), // Deep rich crimson red for maximum vigilance & safety contrast
        border = BorderStroke(1.dp, Color(0xFFB91C1C)),
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF991B1B),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🛡️", fontSize = 20.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Pilgrim Safety Cell",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFDC2626)
                            ) {
                                Text(
                                    text = "24x7 ACTIVE",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Prayagraj Administration Anti-Touting Vigilance",
                            fontSize = 11.sp,
                            color = Color(0xFFFECACA)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Prominent Kumbh 1920 Helpline Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF991B1B),
                border = BorderStroke(1.dp, Color(0xFFEF4444)),
                modifier = Modifier.fillMaxWidth()
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
                            text = "Kumbh Mela Toll-Free Helpline",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFEE2E2)
                        )
                        Text(
                            text = "Dial 1920 (All Indian Languages)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "Instant dispute resolution, lost & found, and rescue.",
                            fontSize = 10.sp,
                            color = Color(0xFFFECACA)
                        )
                    }

                    Button(
                        onClick = { onCallHelpline("1920") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("call_1920_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call 1920",
                            tint = Color(0xFF991B1B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Call 1920",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Direct Dial Action Strip (112, 1077, 1090, Report)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickDialChip(
                    label = "Police 112",
                    icon = "👮",
                    onClick = { onCallHelpline("112") },
                    modifier = Modifier.weight(1f)
                )
                QuickDialChip(
                    label = "Water Police 1077",
                    icon = "🛟",
                    onClick = { onCallHelpline("1077") },
                    modifier = Modifier.weight(1.2f)
                )
                QuickDialChip(
                    label = "Report Scam",
                    icon = "🚨",
                    onClick = onOpenReportModal,
                    modifier = Modifier.weight(1.1f),
                    isHighlighted = true
                )
            }
        }
    }
}

@Composable
private fun QuickDialChip(
    label: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isHighlighted) Color(0xFFFEF2F2) else Color(0xFF991B1B),
        border = BorderStroke(1.dp, if (isHighlighted) Color(0xFFEF4444) else Color(0xFFB91C1C)),
        modifier = modifier
            .height(36.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = icon, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isHighlighted) Color(0xFF991B1B) else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 2. Scam Advisories Section (Priests, Boats, VIP Passes, Souvenirs)
 */
@Composable
fun ScamAdvisoriesSection(
    scams: List<ScamAlert>,
    selectedFilter: String,
    onSelectFilter: (String) -> Unit,
    expandedScamId: String?,
    onToggleExpand: (String) -> Unit,
    onNavigateToOfficialBoats: () -> Unit,
    onNavigateToVerifiedPurohits: () -> Unit,
    onReportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filters = listOf(
        "ALL" to "All Alerts (${COMMON_SCAM_ALERTS.size})",
        "Priests" to "🕉️ Unauthorized Priests",
        "Boats" to "🛶 Boat Overcharging",
        "Temples" to "🎟️ Fake Passes",
        "Prashad" to "📿 Prashad Traps",
        "Transport" to "🛺 Transport"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
        ) {
            items(filters) { (key, label) ->
                val isSelected = selectedFilter == key
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectFilter(key) },
                    label = { Text(text = label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PolishPrimary,
                        selectedLabelColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) PolishPrimary else PolishBorder
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Scam Alert Cards List
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            scams.forEach { scam ->
                val isExpanded = expandedScamId == scam.id
                ScamCard(
                    scam = scam,
                    isExpanded = isExpanded,
                    onToggleExpand = { onToggleExpand(scam.id) },
                    onNavigateToOfficialBoats = onNavigateToOfficialBoats,
                    onNavigateToVerifiedPurohits = onNavigateToVerifiedPurohits,
                    onReportClick = onReportClick
                )
            }
        }
    }
}

/**
 * Detailed Scam Card with Red Flags and Action Steps
 */
@Composable
fun ScamCard(
    scam: ScamAlert,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onNavigateToOfficialBoats: () -> Unit,
    onNavigateToVerifiedPurohits: () -> Unit,
    onReportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (scam.riskLevel == ScamRiskLevel.CRITICAL) Saffron300 else PolishBorder,
                RoundedCornerShape(16.dp)
            )
            .animateContentSize()
            .testTag("scam_card_${scam.id}"),
        shape = RoundedCornerShape(16.dp),
        color = PolishCardSurface,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = when (scam.riskLevel) {
                            ScamRiskLevel.CRITICAL -> Saffron50
                            ScamRiskLevel.HIGH -> Saffron50
                            ScamRiskLevel.MODERATE -> Ocean50
                        },
                        border = BorderStroke(
                            1.dp,
                            when (scam.riskLevel) {
                                ScamRiskLevel.CRITICAL -> Saffron200
                                ScamRiskLevel.HIGH -> Saffron200
                                ScamRiskLevel.MODERATE -> Ocean200
                            }
                        ),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = scam.iconEmoji, fontSize = 20.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = scam.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PolishTextPrimary
                            )
                        }
                        Text(
                            text = scam.hindiTitle,
                            fontSize = 11.sp,
                            color = PolishTextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (scam.riskLevel) {
                        ScamRiskLevel.CRITICAL -> Color(0xFFFEE2E2)
                        ScamRiskLevel.HIGH -> Saffron100
                        ScamRiskLevel.MODERATE -> Ocean100
                    }
                ) {
                    Text(
                        text = when (scam.riskLevel) {
                            ScamRiskLevel.CRITICAL -> "CRITICAL RISK"
                            ScamRiskLevel.HIGH -> "HIGH VIGILANCE"
                            ScamRiskLevel.MODERATE -> "MODERATE RISK"
                        },
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (scam.riskLevel) {
                            ScamRiskLevel.CRITICAL -> Color(0xFFB91C1C)
                            ScamRiskLevel.HIGH -> Saffron900
                            ScamRiskLevel.MODERATE -> Ocean800
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Warning summary
            Text(
                text = scam.warningSummary,
                fontSize = 12.sp,
                color = PolishTextPrimary,
                lineHeight = 16.sp
            )

            // Expandable details block
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Divider(color = PolishBorder.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // How the scam operates
                    Text(
                        text = "🎭 Modus Operandi (Scam Technique):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishPrimary
                    )
                    Text(
                        text = scam.scamTechniqueDetails,
                        fontSize = 11.sp,
                        color = PolishTextSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                    )

                    // Red Flags Section
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFFBEB),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "🚩 Red Flags to Watch Out For:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            scam.redFlags.forEach { flag ->
                                Row(
                                    modifier = Modifier.padding(vertical = 1.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("• ", fontSize = 10.sp, color = Color(0xFFB45309), fontWeight = FontWeight.Bold)
                                    Text(text = flag, fontSize = 10.sp, color = Color(0xFF78350F))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Official Government Stance
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Emerald50,
                        border = BorderStroke(1.dp, Emerald200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Gavel,
                                    contentDescription = null,
                                    tint = Emerald700,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Government Approved Standard:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald800
                                )
                            }
                            Text(
                                text = scam.officialRule,
                                fontSize = 10.sp,
                                color = Emerald900,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Safe Action Tips
                    Text(
                        text = "🛡️ Recommended Safe Actions:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextPrimary
                    )
                    scam.preventionTips.forEach { tip ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("✓ ", fontSize = 11.sp, color = Emerald700, fontWeight = FontWeight.Bold)
                            Text(text = tip, fontSize = 11.sp, color = PolishTextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (scam.id == "scam-unauthorized-priests") {
                            Button(
                                onClick = onNavigateToVerifiedPurohits,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Find Verified Purohit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else if (scam.id == "scam-boat-overcharging") {
                            Button(
                                onClick = onNavigateToOfficialBoats,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Official Boat Rates", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = onReportClick,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFDC2626)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(0.9f)
                        ) {
                            Icon(imageVector = Icons.Default.Report, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Report Scam", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Expand / Collapse Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "Show Less" else "Tap for Red Flags & Prevention Guidelines",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = PolishPrimary
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = PolishPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * 3. Official Helpline Directory Section
 */
@Composable
fun HelplineDirectorySection(
    onCallHelpline: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Official 24x7 Pilgrim Assistance Helplines",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = PolishTextPrimary
        )

        OFFICIAL_EMERGENCY_CONTACTS.forEach { contact ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PolishCardSurface,
                border = BorderStroke(1.dp, PolishBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("helpline_card_${contact.id}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = when (contact.category) {
                                HelplineCategory.KUMBH_CENTRAL -> Color(0xFFFEE2E2)
                                HelplineCategory.POLICE -> PolishPrimaryContainer
                                HelplineCategory.RIVER_RESCUE -> Ocean100
                                HelplineCategory.MEDICAL -> Emerald50
                                HelplineCategory.WOMEN_SAFETY -> Saffron100
                                HelplineCategory.TOURIST_ASSISTANCE -> Ocean50
                            },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = contact.iconEmoji, fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = contact.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PolishTextPrimary
                            )
                            Text(
                                text = contact.formattedDisplayNumber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PolishPrimary
                            )
                            Text(
                                text = contact.description,
                                fontSize = 10.sp,
                                color = PolishTextSecondary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onCallHelpline(contact.number) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (contact.category == HelplineCategory.KUMBH_CENTRAL) Color(0xFFDC2626) else PolishPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Call", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * 4. Official Government Approved Tariff Card Section
 */
@Composable
fun OfficialTariffCheckerSection(
    onBookBoatClick: () -> Unit,
    onBookPurohitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Emerald50,
            border = BorderStroke(1.dp, Emerald200),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Emerald700,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "District Administration Price Guarantee",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Emerald800
                    )
                    Text(
                        text = "Anyone demanding prices higher than these caps can be reported to 1920 immediately.",
                        fontSize = 10.sp,
                        color = Emerald900
                    )
                }
            }
        }

        // Rates Table Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = PolishCardSurface,
            border = BorderStroke(1.dp, PolishBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Official Maximum Approved Rates Reference",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PolishTextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                OFFICIAL_APPROVED_TARIFF_REFERENCE.forEachIndexed { index, item ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.serviceName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PolishTextPrimary
                                )
                                Text(
                                    text = item.regulatoryNote,
                                    fontSize = 10.sp,
                                    color = PolishTextSecondary
                                )
                            }
                            Text(
                                text = item.approvedCap,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PolishPrimary
                            )
                        }
                        if (index < OFFICIAL_APPROVED_TARIFF_REFERENCE.size - 1) {
                            Divider(color = PolishBorder.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        // Quick Booking Shortcuts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onBookBoatClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                modifier = Modifier.weight(1f)
            ) {
                Text("🛶 Official Boat Booking", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onBookPurohitClick,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, PolishPrimary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PolishPrimary),
                modifier = Modifier.weight(1f)
            ) {
                Text("🕉️ Verified Purohits", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * 5. Interactive Pre-Snan Safety Checklist Section
 */
@Composable
fun SafetyChecklistSection(
    checkedItemIds: Set<String>,
    onToggleItem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalItems = PILGRIM_SAFETY_CHECKLIST.size
    val completedCount = checkedItemIds.size
    val progress = if (totalItems > 0) completedCount.toFloat() / totalItems else 0f

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Progress Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = PolishSurface,
            border = BorderStroke(1.dp, PolishBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pilgrim Safety Preparedness",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextPrimary
                    )
                    Text(
                        text = "$completedCount of $totalItems Checked",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (completedCount == totalItems) Emerald700 else PolishPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (completedCount == totalItems) Emerald600 else PolishPrimary,
                    trackColor = PolishBorder
                )

                if (completedCount == totalItems) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "🌟 Excellent! You are fully prepared against scams and overcharging.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Emerald700
                    )
                }
            }
        }

        // Checklist Items
        PILGRIM_SAFETY_CHECKLIST.forEach { item ->
            val isChecked = checkedItemIds.contains(item.id)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isChecked) PolishPrimaryContainer.copy(alpha = 0.5f) else PolishCardSurface,
                border = BorderStroke(1.dp, if (isChecked) PolishPrimary.copy(alpha = 0.5f) else PolishBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleItem(item.id) }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { onToggleItem(item.id) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = PolishPrimary,
                            uncheckedColor = PolishTextTertiary
                        ),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isChecked) PolishTextPrimary else PolishTextPrimary
                            )
                            if (item.isCritical) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(3.dp),
                                    color = Color(0xFFFEE2E2)
                                ) {
                                    Text(
                                        text = "CRITICAL",
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB91C1C),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = item.description,
                            fontSize = 10.sp,
                            color = PolishTextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 6. Report Scam & Incident SOS Dialog
 */
@Composable
fun ReportScamModalDialog(
    onDismiss: () -> Unit,
    onSubmitReport: (location: String, category: String, details: String, phone: String) -> Unit
) {
    var location by remember { mutableStateOf("Sangam Ghat / Kila Ghat") }
    var category by remember { mutableStateOf("Overcharging Boatman") }
    var details by remember { mutableStateOf("") }
    var offenderId by remember { mutableStateOf("") }

    val categories = listOf("Overcharging Boatman", "Unauthorized Priest / Fake Sankalp", "Fake VIP Pass Touts", "Prashad / Gemstone Trap", "Transport Overcharging", "Other Harassment")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PolishSurface,
            border = BorderStroke(1.dp, PolishBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("report_scam_dialog")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🚨", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lodge Scam Grievance",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishTextPrimary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = PolishTextTertiary)
                    }
                }

                Text(
                    text = "Direct submission to Prayagraj Mela Police & Anti-Touting Squad (1920)",
                    fontSize = 11.sp,
                    color = PolishTextSecondary,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                // Location Field
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Incident Location / Ghat", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PolishPrimary)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Offender details
                OutlinedTextField(
                    value = offenderId,
                    onValueChange = { offenderId = it },
                    label = { Text("Boat # / Priest Badge / Person Details (if known)", fontSize = 11.sp) },
                    placeholder = { Text("e.g. Boat painted #142 near Kila Ghat", fontSize = 10.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PolishPrimary)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Grievance Details
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Describe the extortion / overcharge", fontSize = 11.sp) },
                    placeholder = { Text("e.g. Demanded ₹3000 instead of official ₹100 rate...", fontSize = 10.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PolishPrimary)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { onSubmitReport(location, category, details, offenderId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("submit_scam_report_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Submit & Call Kumbh Helpline (1920)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Dedicated Full Screen wrapper for Pilgrim Safety Dashboard
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PilgrimSafetyScreen(
    onBack: () -> Unit,
    onNavigateToOfficialBoats: () -> Unit = {},
    onNavigateToVerifiedPurohits: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PolishBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Pilgrim Safety & Anti-Scam",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = PolishTextPrimary
                            )
                        }
                        Text(
                            text = "Helplines • Scam Alerts • Fair Tariff Rules",
                            fontSize = 11.sp,
                            color = PolishTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PolishTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PolishSurface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                PilgrimSafetyDashboard(
                    onNavigateToOfficialBoats = onNavigateToOfficialBoats,
                    onNavigateToVerifiedPurohits = onNavigateToVerifiedPurohits
                )
            }
        }
    }
}

private fun dialPhoneNumber(context: Context, phoneNumber: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Dialing $phoneNumber", Toast.LENGTH_SHORT).show()
    }
}
