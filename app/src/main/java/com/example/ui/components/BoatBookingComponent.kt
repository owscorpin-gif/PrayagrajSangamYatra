package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.theme.*

/**
 * Official Boat Counter Location Details
 */
data class OfficialBoatCounter(
    val id: String,
    val ghatName: String,
    val hindiName: String,
    val counterNumber: String,
    val locationDescription: String,
    val operatingHours: String,
    val tokenSystem: String,
    val helpline: String
)

val OFFICIAL_BOAT_COUNTERS = listOf(
    OfficialBoatCounter(
        id = "ctr-kila",
        ghatName = "Kila Ghat Official Counter",
        hindiName = "किला घाट प्रशासनिक नौका काउंटर",
        counterNumber = "Counter #1 & #2 (Near Akbar Fort East Gate)",
        locationDescription = "Adjacent to Police Control Room, Kila Bund",
        operatingHours = "04:30 AM – 08:30 PM",
        tokenSystem = "Digital QR Slip / Cash / UPI Allowed",
        helpline = "1920 (Kumbh Mela Helpline)"
    ),
    OfficialBoatCounter(
        id = "ctr-saraswati",
        ghatName = "Saraswati Ghat Official Counter",
        hindiName = "सरस्वती घाट नगर निगम नौका केंद्र",
        counterNumber = "Counter #3 (Near Ganga Aarti Steps)",
        locationDescription = "Near Mankameshwar Link Stairs & Park Gate",
        operatingHours = "05:00 AM – 09:00 PM",
        tokenSystem = "Administration Standard Rate Slip",
        helpline = "0532-2500000"
    ),
    OfficialBoatCounter(
        id = "ctr-arail",
        ghatName = "Arail Ghat South Bank Counter",
        hindiName = "अरैल दक्षिणी तट नौका केंद्र",
        counterNumber = "Counter #4 (Near Tent City Stand)",
        locationDescription = "Someshwar Mahadev Temple Entrance Road",
        operatingHours = "05:00 AM – 07:30 PM",
        tokenSystem = "Govt Token Dispatch",
        helpline = "1920"
    ),
    OfficialBoatCounter(
        id = "ctr-daraganj",
        ghatName = "Dashashwamedh Ghat Counter",
        hindiName = "दशाश्वमेध घाट नौका स्टैंड",
        counterNumber = "Counter #5 (Daraganj Ganga Bank)",
        locationDescription = "Near Daraganj Police Post",
        operatingHours = "05:00 AM – 08:00 PM",
        tokenSystem = "Pilgrim Standard Slip",
        helpline = "0532-2500001"
    )
)

/**
 * Official Approved Boat Booking Card with Tier Toggle (Shared vs Private)
 * and 'Book Now' integration for Official Administration Counters.
 */
@Composable
fun BoatBookingTierComponent(
    onBookCounter: (BoatFareMatrix, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var selectedTier by remember { mutableStateOf(BoatType.SHARED_ROWBOAT) } // SHARED_ROWBOAT or PRIVATE_ROWBOAT
    var selectedGhatCounter by remember { mutableStateOf(OFFICIAL_BOAT_COUNTERS.first()) }
    var showBookingConfirmationDialog by remember { mutableStateOf(false) }
    var confirmedTokenNumber by remember { mutableStateOf("") }
    var passengerCount by remember { mutableIntStateOf(2) }

    val currentFare = remember(selectedTier) {
        SAMPLE_BOAT_FARES.find { it.boatType == selectedTier } ?: SAMPLE_BOAT_FARES.first()
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("boat_booking_component"),
        shape = RoundedCornerShape(20.dp),
        color = PolishCardSurface,
        border = BorderStroke(1.dp, PolishBorder),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Anti-Exploitation Verified Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Ocean50,
                        border = BorderStroke(1.dp, Ocean200),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🛶", fontSize = 18.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Official Boat Booking",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishTextPrimary
                        )
                        Text(
                            text = "District Administration Approved Rates",
                            fontSize = 11.sp,
                            color = PolishTextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Emerald50,
                    border = BorderStroke(1.dp, Emerald200)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = Emerald700,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "OFFICIAL TARIFF",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald700
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fare Tier Selector Tabs (Shared vs Private vs Express Motorboat)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PolishBackground,
                border = BorderStroke(1.dp, PolishBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Shared Rowboat Tab
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedTier == BoatType.SHARED_ROWBOAT) PolishPrimary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedTier = BoatType.SHARED_ROWBOAT
                                if (passengerCount > 12) passengerCount = 2
                            }
                            .testTag("tier_tab_shared")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Shared Boat",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTier == BoatType.SHARED_ROWBOAT) Color.White else PolishTextPrimary
                            )
                            Text(
                                text = "₹50–₹100 / person",
                                fontSize = 10.sp,
                                color = if (selectedTier == BoatType.SHARED_ROWBOAT) Saffron100 else PolishTextSecondary
                            )
                        }
                    }

                    // Private Rowboat Tab
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedTier == BoatType.PRIVATE_ROWBOAT) PolishPrimary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedTier = BoatType.PRIVATE_ROWBOAT
                                if (passengerCount > 6) passengerCount = 4
                            }
                            .testTag("tier_tab_private")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Private Boat",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTier == BoatType.PRIVATE_ROWBOAT) Color.White else PolishTextPrimary
                            )
                            Text(
                                text = "₹600–₹1,200 / boat",
                                fontSize = 10.sp,
                                color = if (selectedTier == BoatType.PRIVATE_ROWBOAT) Saffron100 else PolishTextSecondary
                            )
                        }
                    }

                    // Motorboat Tab
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedTier == BoatType.MOTORBOAT) PolishPrimary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedTier = BoatType.MOTORBOAT
                                if (passengerCount > 10) passengerCount = 5
                            }
                            .testTag("tier_tab_motorboat")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Motorboat",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTier == BoatType.MOTORBOAT) Color.White else PolishTextPrimary
                            )
                            Text(
                                text = "₹1,500–₹2,500 / boat",
                                fontSize = 10.sp,
                                color = if (selectedTier == BoatType.MOTORBOAT) Saffron100 else PolishTextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pricing & Feature Overview Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = when (selectedTier) {
                    BoatType.SHARED_ROWBOAT -> Emerald50
                    BoatType.PRIVATE_ROWBOAT -> Ocean50
                    BoatType.MOTORBOAT -> Saffron50
                },
                border = BorderStroke(
                    1.dp,
                    when (selectedTier) {
                        BoatType.SHARED_ROWBOAT -> Emerald200
                        BoatType.PRIVATE_ROWBOAT -> Ocean200
                        BoatType.MOTORBOAT -> Saffron200
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentFare.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PolishTextPrimary
                            )
                            Text(
                                text = currentFare.hindiTitle,
                                fontSize = 11.sp,
                                color = PolishTextSecondary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (currentFare.isPerHead) "₹${currentFare.minRateInr} – ₹${currentFare.maxRateInr}" else "₹${currentFare.minRateInr} – ₹${currentFare.maxRateInr}",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PolishPrimary
                            )
                            Text(
                                text = if (currentFare.isPerHead) "per devotee (both ways)" else "total boat (round trip)",
                                fontSize = 9.sp,
                                color = PolishTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = PolishBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Inclusions list
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = Emerald700, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Sangam Confluence Halting: ${currentFare.sangamStopDuration}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PolishTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    currentFare.inclusions.forEach { inclusion ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 1.dp)
                        ) {
                            Text(text = "✓ ", fontSize = 10.sp, color = Emerald700, fontWeight = FontWeight.Bold)
                            Text(text = inclusion, fontSize = 11.sp, color = PolishTextSecondary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Official Counter Boarding Point Selector
            Text(
                text = "Select Official Boarding Counter:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = PolishTextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OFFICIAL_BOAT_COUNTERS.forEach { counter ->
                    val isSelected = counter.id == selectedGhatCounter.id
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) PolishPrimaryContainer else PolishBackground,
                        border = BorderStroke(1.dp, if (isSelected) PolishPrimary else PolishBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedGhatCounter = counter }
                            .testTag("counter_option_${counter.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedGhatCounter = counter },
                                colors = RadioButtonDefaults.colors(selectedColor = PolishPrimary),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = counter.ghatName,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) PolishOnPrimaryContainer else PolishTextPrimary
                                )
                                Text(
                                    text = "${counter.counterNumber} • Hours: ${counter.operatingHours}",
                                    fontSize = 10.sp,
                                    color = if (isSelected) PolishSubtext else PolishTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Passenger count adjustment
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PolishBackground, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Number of Devotees / Passengers", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = PolishTextPrimary)
                    Text(text = "Max capacity: ${currentFare.maxCapacity} persons", fontSize = 10.sp, color = PolishTextTertiary)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (passengerCount > 1) passengerCount-- },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Text(text = "−", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PolishPrimary)
                    }
                    Text(
                        text = "$passengerCount",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = PolishTextPrimary
                    )
                    IconButton(
                        onClick = { if (passengerCount < currentFare.maxCapacity) passengerCount++ },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Text(text = "+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PolishPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary 'Book Now at Official Counter' CTA Button
            Button(
                onClick = {
                    confirmedTokenNumber = "PRY-BOAT-TK-${(1000..9999).random()}"
                    showBookingConfirmationDialog = true
                    onBookCounter(currentFare, selectedGhatCounter.ghatName)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("book_now_official_counter_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary)
            ) {
                Icon(imageVector = Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Book Now (Official Counter Token)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "🛡️ 100% Rate Guarantee: No overcharging. Present token at official counter.",
                fontSize = 10.sp,
                color = Emerald800,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Official Booking Token Modal Dialog
    if (showBookingConfirmationDialog) {
        Dialog(onDismissRequest = { showBookingConfirmationDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = PolishSurface,
                border = BorderStroke(1.dp, PolishBorder),
                modifier = Modifier.fillMaxWidth().testTag("official_booking_token_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PolishGreenBg,
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = PolishGreen,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Official Boat Booking Confirmed",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextPrimary
                    )
                    Text(
                        text = "Authorized Fare Token for Sangam Snan",
                        fontSize = 11.sp,
                        color = PolishTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Token Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PolishPrimaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "YOUR OFFICIAL TOKEN NUMBER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PolishSubtext)
                            Text(
                                text = confirmedTokenNumber,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PolishOnPrimaryContainer,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Text(text = "Valid for Today's Scheduled Embarkation", fontSize = 10.sp, color = PolishSubtext)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Booking Details Breakdown
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PolishBackground,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            DetailRow(label = "Boat Type", value = currentFare.title)
                            DetailRow(label = "Selected Stand", value = selectedGhatCounter.ghatName)
                            DetailRow(label = "Location", value = selectedGhatCounter.locationDescription)
                            DetailRow(label = "Passengers", value = "$passengerCount Devotees")
                            DetailRow(
                                label = "Official Tariff",
                                value = if (currentFare.isPerHead) "₹${currentFare.minRateInr * passengerCount} (₹${currentFare.minRateInr}/head)" else "₹${currentFare.minRateInr} (Entire Boat)"
                            )
                            DetailRow(label = "Life Jackets", value = "Mandatory & Included")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showBookingConfirmationDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary)
                    ) {
                        Text(text = "Done & Show to Counter", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = PolishTextTertiary)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = PolishTextPrimary)
    }
}
