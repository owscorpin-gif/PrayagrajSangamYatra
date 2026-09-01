package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

/**
 * 1. Boat Fair Matrix Card
 */
@Composable
fun BoatFareMatrixCard(
    fare: BoatFareMatrix,
    onBookNow: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("boat_fare_card_${fare.boatType.name.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PolishSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fare.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextPrimary
                    )
                    Text(
                        text = "${fare.hindiTitle} • Max ${fare.maxCapacity} Passengers",
                        fontSize = 12.sp,
                        color = PolishTextSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Emerald50,
                    border = BorderStroke(1.dp, Emerald200)
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (fare.isPerHead) "₹${fare.minRateInr} – ₹${fare.maxRateInr}" else "₹${fare.minRateInr} – ₹${fare.maxRateInr}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Emerald700
                        )
                        Text(
                            text = if (fare.isPerHead) "per head (return)" else "total boat (return)",
                            fontSize = 9.sp,
                            color = Emerald800,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Safety & Inclusions
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = PolishBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = PolishTeal,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sangam Stop: ${fare.sangamStopDuration} • Life Jacket Included",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PolishTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    fare.inclusions.forEach { item ->
                        Text(
                            text = "• $item",
                            fontSize = 11.sp,
                            color = PolishTextSecondary,
                            modifier = Modifier.padding(start = 6.dp, top = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onBookNow,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("book_boat_button_${fare.boatType.name.lowercase()}"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary)
            ) {
                Text(
                    text = "Check Verified Boatmen →",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 2. Verified Boatman Profile Card
 */
@Composable
fun VerifiedBoatmanCard(
    boatman: VerifiedBoatmanProfile,
    onCallClick: () -> Unit = {},
    onBookRideClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("boatman_card_${boatman.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PolishSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = PolishPrimaryContainer,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🚣", fontSize = 22.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = boatman.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishTextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = PolishGreenBg
                        ) {
                            Text(
                                text = "VERIFIED",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = PolishGreen,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Text(
                        text = "${boatman.hindiName} • Reg: ${boatman.registrationNumber}",
                        fontSize = 11.sp,
                        color = PolishTextSecondary
                    )

                    Text(
                        text = "Boat: ${boatman.boatIdentificationNumber} • ${boatman.assignedGhatStation}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = PolishTextTertiary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = boatman.rating.toString(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishTextPrimary
                        )
                    }
                    Text(
                        text = "${boatman.totalTrips} trips",
                        fontSize = 10.sp,
                        color = PolishTextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCallClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, PolishPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PolishPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Call Boatman", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onBookRideClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary)
                ) {
                    Text(text = "Book Ride", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * 3. Real-Time Ghat Navigation & Crowd Density Card
 */
@Composable
fun GhatNavigationCard(
    ghat: GhatNavigationInfo,
    onNavigateClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val (crowdBadgeBg, crowdBadgeText, crowdColor) = when (ghat.crowdDensity) {
        CrowdDensityLevel.LOW -> Triple(Emerald50, "LOW CROWD", Emerald700)
        CrowdDensityLevel.MODERATE -> Triple(Color(0xFFFEF3C7), "MODERATE", Color(0xFFD97706))
        CrowdDensityLevel.HIGH -> Triple(Saffron100, "HIGH CROWD", Saffron800)
        CrowdDensityLevel.SEVERE -> Triple(PolishRedBg, "HEAVY SURGE", PolishRed)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ghat_nav_card_${ghat.ghatId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PolishSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ghat.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextPrimary
                    )
                    Text(
                        text = "${ghat.hindiName} • ${ghat.distanceKm} km away (~${ghat.walkingTimeMinutes}m walk)",
                        fontSize = 12.sp,
                        color = PolishTextSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = crowdBadgeBg,
                    border = BorderStroke(1.dp, crowdColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = crowdBadgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = crowdColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = PolishBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalParking,
                            contentDescription = null,
                            tint = PolishPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Parking: ${ghat.parkingLocation}",
                            fontSize = 11.sp,
                            color = PolishTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DirectionsWalk,
                            contentDescription = null,
                            tint = PolishTeal,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "E-Rickshaw Stand: ${ghat.batteryAutoPickupPoint}",
                            fontSize = 11.sp,
                            color = PolishTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ideal window: ${ghat.bestTimeToVisit}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = PolishTextTertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onNavigateClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary)
            ) {
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Navigate to Ghat on Map", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
