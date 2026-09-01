package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.PandaServiceOffering
import com.example.data.model.VerifiedPanda
import com.example.ui.theme.*

/**
 * Reusable PandaProfileCard UI Component
 * 
 * Displays a verified priest's name, titles, lineage, specialized rituals,
 * rating, accreditation badge, contact details (Phone/WhatsApp/Location),
 * and action buttons for booking rituals and instant calling.
 */
@Composable
fun PandaProfileCard(
    panda: VerifiedPanda,
    onBookClick: (VerifiedPanda) -> Unit,
    modifier: Modifier = Modifier,
    onCallClick: ((VerifiedPanda) -> Unit)? = null,
    onWhatsAppClick: ((VerifiedPanda) -> Unit)? = null,
    onSelectService: ((VerifiedPanda, PandaServiceOffering) -> Unit)? = null,
    onCardClick: ((VerifiedPanda) -> Unit)? = null,
    initiallyExpanded: Boolean = false
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                if (panda.isVerified) Saffron200 else PolishBorder,
                RoundedCornerShape(16.dp)
            )
            .clickable(enabled = onCardClick != null) { onCardClick?.invoke(panda) }
            .animateContentSize()
            .testTag("panda_profile_card_${panda.id}"),
        shape = RoundedCornerShape(16.dp),
        color = PolishCardSurface,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Section: Avatar, Name, Verification, Rating & Lineage
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Profile Avatar / Sacred Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Saffron100)
                        .border(1.5.dp, Saffron400, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!panda.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = panda.avatarUrl,
                            contentDescription = "${panda.name} Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = "🕉️",
                            fontSize = 26.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name, Hindi Name, Title & Experience
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = panda.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = PolishTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (panda.isVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified Priest",
                                    tint = Emerald600,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Rating Badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Saffron50,
                            border = BorderStroke(1.dp, Saffron200)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Saffron500,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${panda.rating}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Saffron900
                                )
                                Text(
                                    text = " (${panda.totalReviews})",
                                    fontSize = 9.sp,
                                    color = PolishTextSecondary
                                )
                            }
                        }
                    }

                    // Hindi Name & Title
                    Text(
                        text = "${panda.hindiName} • ${panda.title}",
                        fontSize = 12.sp,
                        color = PolishPrimary,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Ghat Location & Experience
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = PolishTextTertiary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = panda.ghatLocation,
                            fontSize = 11.sp,
                            color = PolishTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = " • ${panda.yearsOfExperience} yrs exp",
                            fontSize = 11.sp,
                            color = PolishTextTertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Clan Lineage & Accreditation Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = PolishPrimaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "📜 ${panda.clanLineage}",
                        fontSize = 10.sp,
                        color = PolishPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Emerald50,
                    border = BorderStroke(1.dp, Emerald200)
                ) {
                    Text(
                        text = "ID: ${panda.accreditationId}",
                        fontSize = 9.sp,
                        color = Emerald800,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Specialized Rituals Tag Strip
            Text(
                text = "Specialized Rituals:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = PolishTextPrimary
            )
            
            Spacer(modifier = Modifier.height(4.dp))

            if (panda.services.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(panda.services) { service ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Saffron50,
                            border = BorderStroke(1.dp, Saffron200),
                            modifier = Modifier.clickable {
                                onSelectService?.invoke(panda, service)
                            }
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text(
                                    text = service.title,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Saffron900
                                )
                                Text(
                                    text = service.dakshinaGuide,
                                    fontSize = 9.sp,
                                    color = PolishPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PolishSurface,
                    border = BorderStroke(1.dp, PolishBorder)
                ) {
                    Text(
                        text = "Sangam Snan Sankalp • Pind Daan • Rudrabhishek • Ganga Aarti",
                        fontSize = 10.sp,
                        color = PolishTextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Expandable Bio & Bahi-Khata Details
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Divider(color = PolishBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = panda.bio,
                        fontSize = 11.sp,
                        color = PolishTextSecondary,
                        lineHeight = 15.sp
                    )

                    if (panda.bahiKhataAvailable && panda.bahiKhataRegions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Ocean50,
                            border = BorderStroke(1.dp, Ocean200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "📖", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "Ancestral Record Ledger (Bahi-Khata) Available",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Ocean800
                                    )
                                    Text(
                                        text = "Regions: ${panda.bahiKhataRegions.joinToString(", ")}",
                                        fontSize = 9.sp,
                                        color = Ocean800.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    if (panda.languages.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = PolishTextTertiary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Spoken: ${panda.languages.joinToString(", ")}",
                                fontSize = 10.sp,
                                color = PolishTextTertiary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Contact (Call & WhatsApp) and Book Ritual
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Call Button
                OutlinedButton(
                    onClick = {
                        if (onCallClick != null) {
                            onCallClick(panda)
                        } else {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${panda.phoneNumber}"))
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot open dialer: ${panda.phoneNumber}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, PolishPrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("call_panda_${panda.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = PolishPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Call",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishPrimary
                    )
                }

                // WhatsApp Button
                OutlinedButton(
                    onClick = {
                        if (onWhatsAppClick != null) {
                            onWhatsAppClick(panda)
                        } else {
                            try {
                                val cleanNum = panda.whatsappNumber.replace("+", "").replace(" ", "").replace("-", "")
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://wa.me/$cleanNum?text=Namaste%20Purohit%20Ji,%20I%20would%20like%20to%20inquire%20about%20sacred%20rituals%20at%20Prayagraj.")
                                )
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot open WhatsApp", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Emerald600),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("whatsapp_panda_${panda.id}")
                ) {
                    Text(
                        text = "💬",
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "WhatsApp",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Emerald700
                    )
                }

                // Primary Book Ritual Action Button
                Button(
                    onClick = { onBookClick(panda) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("book_panda_${panda.id}")
                ) {
                    Text(
                        text = "Book Ritual",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Expand / Collapse Footer Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "Less details" else "View Lineage & Ledger details",
                    fontSize = 10.sp,
                    color = PolishTextTertiary
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = PolishTextTertiary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
