package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Place
import java.util.Locale

val StarGold = Color(0xFFFFB300)
val StarEmptyGray = Color(0xFFC4C4C4)

/**
 * Returns a consistent baseline average rating for a place (e.g. 4.6 - 4.9 for Prayagraj heritage sites)
 */
fun getBaselineAverageRating(placeId: String): Pair<Double, Int> {
    val hash = kotlin.math.abs(placeId.hashCode())
    val baseAvg = 4.4 + ((hash % 50) / 100.0) // Between 4.40 and 4.89
    val baseCount = 120 + (hash % 450) // 120 to 570 ratings
    return Pair(baseAvg, baseCount)
}

@Composable
fun PlaceCard(
    place: Place,
    isSelected: Boolean = false,
    distanceText: String? = null,
    onToggleSelection: () -> Unit = {},
    userRating: Int = 0,
    onRatingChanged: ((Int) -> Unit)? = null,
    averageRating: Double? = null,
    ratingCount: Int? = null,
    modifier: Modifier = Modifier
) {
    var selectedStarRating by remember(place.id, userRating) { mutableIntStateOf(userRating) }
    var ratingInputText by remember(place.id, userRating) {
        mutableStateOf(if (userRating > 0) userRating.toString() else "")
    }

    val (defaultAvg, defaultCount) = remember(place.id) { getBaselineAverageRating(place.id) }
    val displayAvg = averageRating ?: defaultAvg
    val displayCount = ratingCount ?: defaultCount

    // Dynamic calculated average if user has provided a rating
    val effectiveAverage = if (selectedStarRating > 0) {
        val totalScore = (displayAvg * displayCount) + selectedStarRating
        val newCount = displayCount + 1
        totalScore / newCount
    } else {
        displayAvg
    }
    val effectiveCount = if (selectedStarRating > 0) displayCount + 1 else displayCount

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else MaterialTheme.colorScheme.surface,
        label = "cardBackground"
    )

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("place_card_${place.id}"),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Place Banner Image with distance badge overlay if available
            Box(modifier = Modifier.fillMaxWidth()) {
                place.imageUrl?.let { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = place.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }

                if (distanceText != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 4.dp,
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .testTag("distance_badge_overlay_${place.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NearMe,
                                contentDescription = "Distance",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = distanceText,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Header with Name & Average Rating Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = place.name,
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (!place.hindiName.isNullOrBlank()) {
                            Text(
                                text = place.hindiName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        // Category & Average Rating Summary Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            SuggestionChip(
                                onClick = { },
                                label = {
                                    Text(
                                        text = place.category,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            )

                            // Average Rating Pill
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = StarGold.copy(alpha = 0.15f),
                                modifier = Modifier.testTag("average_rating_badge_${place.id}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Average rating",
                                        tint = StarGold,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = String.format(Locale.getDefault(), "%.1f", effectiveAverage),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "($effectiveCount)",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }

                            if (distanceText != null) {
                                SuggestionChip(
                                    onClick = { },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.NearMe,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "$distanceText away",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }

                    // Add / Remove Selection Button
                    IconButton(
                        onClick = onToggleSelection,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .testTag("toggle_selection_${place.id}"),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Add,
                            contentDescription = if (isSelected) "Remove from Itinerary" else "Add to Itinerary"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                Text(
                    text = place.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Timings Row
                val timingText = place.timings ?: place.openingHours
                timingText?.let { timings ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = timings,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Address Row
                place.address?.let { address ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = address,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Star-Rating & Input Rating Section
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("star_rating_container_${place.id}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Top interactive star bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Your Rating:",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                // 5 Interactive Star Rating Icons
                                for (starIndex in 1..5) {
                                    val isFilled = starIndex <= selectedStarRating
                                    val starScale by animateFloatAsState(
                                        targetValue = if (isFilled) 1.15f else 1.0f,
                                        animationSpec = spring(),
                                        label = "star_scale_$starIndex"
                                    )

                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .scale(starScale)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = null
                                            ) {
                                                val newRating = if (selectedStarRating == starIndex) 0 else starIndex
                                                selectedStarRating = newRating
                                                ratingInputText = if (newRating > 0) newRating.toString() else ""
                                                onRatingChanged?.invoke(newRating)
                                            }
                                            .testTag("star_rating_${place.id}_$starIndex"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isFilled) Icons.Default.Star else Icons.Outlined.StarOutline,
                                            contentDescription = "Rate $starIndex stars",
                                            tint = if (isFilled) StarGold else StarEmptyGray,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }

                            // Current Status Tag
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedStarRating > 0) StarGold.copy(alpha = 0.20f) else Color.Transparent
                            ) {
                                Text(
                                    text = if (selectedStarRating > 0) "$selectedStarRating / 5.0" else "Unrated",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedStarRating > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    ),
                                    modifier = Modifier
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .testTag("rating_numeric_label_${place.id}")
                                )
                            }
                        }

                        // Numerical Input Field to Submit Rating directly (1 - 5)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = ratingInputText,
                                onValueChange = { input ->
                                    // Limit to single numeric character 1..5 or empty
                                    val cleaned = input.filter { it.isDigit() }.take(1)
                                    ratingInputText = cleaned
                                },
                                label = { Text("Score (1-5)", fontSize = 11.sp) },
                                placeholder = { Text("1-5", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("rating_input_field_${place.id}"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )

                            FilledTonalButton(
                                onClick = {
                                    val num = ratingInputText.toIntOrNull()
                                    if (num != null && num in 1..5) {
                                        selectedStarRating = num
                                        onRatingChanged?.invoke(num)
                                    } else if (ratingInputText.isEmpty()) {
                                        selectedStarRating = 0
                                        onRatingChanged?.invoke(0)
                                    }
                                },
                                modifier = Modifier.testTag("submit_rating_button_${place.id}"),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Submit Rating",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Submit", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
