package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.R
import com.example.data.model.PandaServiceOffering
import com.example.data.model.VerifiedPanda
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Data structure representing a completed ritual booking confirmation.
 */
data class RitualBookingDetails(
    val bookingId: String,
    val pandaId: String,
    val pandaName: String,
    val ghatLocation: String,
    val ritualTitle: String,
    val selectedDate: String,
    val selectedTimeSlot: String,
    val pilgrimName: String,
    val pilgrimPhone: String,
    val gotra: String,
    val numPersons: Int,
    val estimatedDakshina: String,
    val samagriRequired: Boolean,
    val specialNotes: String
)

/**
 * Reusable RitualBookingBottomSheet Modal
 * 
 * Allows pilgrims to select a ritual, date, auspicious time slot (Brahma Muhurta, Morning, Evening Sandhya),
 * provide pilgrim & gotra details, and confirm the booking with instant receipt generation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RitualBookingBottomSheet(
    panda: VerifiedPanda,
    onDismiss: () -> Unit,
    onConfirmBooking: (RitualBookingDetails) -> Unit,
    modifier: Modifier = Modifier,
    initialService: PandaServiceOffering? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Ritual Selection State
    var selectedService: PandaServiceOffering by remember {
        mutableStateOf(
            initialService ?: panda.services.firstOrNull() ?: PandaServiceOffering(
                id = "srv_sankalp",
                title = "Triveni Sangam Snan Sankalp",
                hindiTitle = "त्रिवेणी संगम स्नान संकल्प",
                category = "Snan & Sankalp",
                description = "Traditional holy dip sankalp with Vedic mantras and Ganga Poojan.",
                durationMinutes = 30,
                dakshinaGuide = "₹101 – ₹351 (Voluntary)",
                samagriIncluded = true
            )
        )
    }

    // Available Dates: Next 7 days
    val calendarDays = remember {
        val list = mutableListOf<Triple<String, String, String>>() // DayName, DayNum, FullDate
        val cal = Calendar.getInstance()
        val dayNameFmt = SimpleDateFormat("EEE", Locale.ENGLISH)
        val dayNumFmt = SimpleDateFormat("dd", Locale.ENGLISH)
        val fullFmt = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        for (i in 0 until 7) {
            val date = cal.time
            list.add(Triple(dayNameFmt.format(date), dayNumFmt.format(date), fullFmt.format(date)))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    var selectedDateIndex by remember { mutableIntStateOf(0) }

    // Auspicious Time Slots (Muhurta based)
    val timeSlots = remember {
        listOf(
            "04:30 AM (Brahma Muhurta)" to "Most Auspicious for Sangam Snan",
            "06:00 AM (Sunrise / Surya Puja)" to "Ideal for Ganga Aarti & Sankalp",
            "08:00 AM (Morning Puja)" to "Standard Vedic Ritual Time",
            "10:30 AM (Mid-Day Pind Daan)" to "Optimal for Pitra Tarpan",
            "03:00 PM (Afternoon Puja)" to "Calm Ghat slots",
            "05:30 PM (Sandhya Aarti)" to "Ganga Sandhya & Deep Daan"
        )
    }
    var selectedTimeSlotIndex by remember { mutableIntStateOf(1) }

    // Pilgrim Form Details
    var pilgrimName by remember { mutableStateOf("") }
    var pilgrimPhone by remember { mutableStateOf("") }
    var gotra by remember { mutableStateOf("") }
    var numPersons by remember { mutableIntStateOf(2) }
    var samagriIncluded by remember { mutableStateOf(selectedService.samagriIncluded) }
    var specialNotes by remember { mutableStateOf("") }

    var isSuccessModalOpen by remember { mutableStateOf(false) }
    var confirmedDetails by remember { mutableStateOf<RitualBookingDetails?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PolishBackground,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = modifier.testTag("ritual_booking_bottom_sheet")
    ) {
        if (confirmedDetails != null) {
            val details = confirmedDetails!!
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Celebratory Lottie Animation
                LottieSuccessCelebration(
                    sizeDp = 130,
                    modifier = Modifier.testTag("lottie_success_animation")
                )

                Text(
                    text = "Ritual Booking Confirmed!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PolishTextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "हर हर गंगे! May your sacred Sankalp bring peace and auspicious blessings.",
                    fontSize = 12.sp,
                    color = Saffron900,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Official Pilgrim Token Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Saffron50,
                    border = BorderStroke(1.5.dp, Saffron300),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "OFFICIAL PILGRIM TOKEN ID",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Saffron800,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = details.bookingId,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = PolishPrimary,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Present at Purohit Ji's Ghat Counter on Arrival",
                            fontSize = 10.sp,
                            color = PolishTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Itemized Summary Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PolishCardSurface,
                    border = BorderStroke(1.dp, PolishBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "📋 Ritual Booking Summary",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishTextPrimary
                        )

                        HorizontalDivider(color = PolishBorder.copy(alpha = 0.6f))

                        RitualDetailRow(label = "Ritual", value = details.ritualTitle)
                        RitualDetailRow(label = "Purohit Ji", value = "${details.pandaName} (${panda.hindiName})")
                        RitualDetailRow(label = "Ghat Location", value = details.ghatLocation)
                        RitualDetailRow(label = "Date & Time", value = "${details.selectedDate} • ${details.selectedTimeSlot}")
                        RitualDetailRow(label = "Devotee", value = "${details.pilgrimName} (Gotra: ${details.gotra})")
                        RitualDetailRow(label = "Devotees Count", value = "${details.numPersons} Persons")
                        RitualDetailRow(label = "Samagri", value = if (details.samagriRequired) "Arranged by Purohit Ji" else "Self-Arranged")
                        RitualDetailRow(label = "Dakshina Guide", value = details.estimatedDakshina)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // SMS Notification Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Emerald50,
                    border = BorderStroke(1.dp, Emerald200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📲", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Confirmation SMS Logged",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald900
                            )
                            Text(
                                text = "Token details sent to ${details.pilgrimPhone}. Purohit Ji has been notified.",
                                fontSize = 9.sp,
                                color = Emerald800
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "🕉️ Prayagraj Yatra - Sacred Ritual Booking\n" +
                                            "Token ID: ${details.bookingId}\n" +
                                            "Ritual: ${details.ritualTitle}\n" +
                                            "Purohit: ${details.pandaName}\n" +
                                            "Location: ${details.ghatLocation}\n" +
                                            "Date & Time: ${details.selectedDate} at ${details.selectedTimeSlot}\n" +
                                            "Yatri: ${details.pilgrimName}"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Booking Token"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, PolishPrimary)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp), tint = PolishPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share", color = PolishPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("done_booking_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary)
                    ) {
                        Text("Done", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(scrollState)
            ) {
            // Header: Priest Summary & Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Saffron100,
                    border = BorderStroke(1.dp, Saffron400),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🕉️", fontSize = 22.sp)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Book Sacred Ritual",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PolishTextPrimary
                    )
                    Text(
                        text = "With ${panda.name} (${panda.hindiName})",
                        fontSize = 12.sp,
                        color = PolishPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "📍 ${panda.ghatLocation} • Verified ID: ${panda.accreditationId}",
                        fontSize = 10.sp,
                        color = PolishTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Select Ritual Type
            Text(
                text = "1. Select Ritual Service",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = PolishTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            val servicesList = if (panda.services.isNotEmpty()) panda.services else listOf(selectedService)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(servicesList) { srv ->
                    val isSelected = selectedService.id == srv.id
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Saffron100 else PolishCardSurface,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) Saffron600 else PolishBorder
                        ),
                        modifier = Modifier
                            .clickable {
                                selectedService = srv
                                samagriIncluded = srv.samagriIncluded
                            }
                            .testTag("service_chip_${srv.id}")
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Text(
                                text = srv.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Saffron900 else PolishTextPrimary
                            )
                            Text(
                                text = "⏱️ ${srv.durationMinutes} mins • ${srv.dakshinaGuide}",
                                fontSize = 10.sp,
                                color = if (isSelected) Saffron700 else PolishTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Select Date (Horizontal Date Picker)
            Text(
                text = "2. Select Date",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = PolishTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(calendarDays.indices.toList()) { index ->
                    val (dayName, dayNum, fullDate) = calendarDays[index]
                    val isSelected = selectedDateIndex == index
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) PolishPrimary else PolishCardSurface,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) PolishPrimary else PolishBorder
                        ),
                        modifier = Modifier
                            .width(58.dp)
                            .clickable { selectedDateIndex = index }
                            .testTag("date_chip_$index")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = dayName.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White.copy(alpha = 0.85f) else PolishTextTertiary
                            )
                            Text(
                                text = dayNum,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSelected) Color.White else PolishTextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Select Time Slot (Auspicious Muhurta)
            Text(
                text = "3. Select Time Slot (Muhurta)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = PolishTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                timeSlots.forEachIndexed { index, (slot, description) ->
                    val isSelected = selectedTimeSlotIndex == index
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) PolishPrimaryContainer else PolishCardSurface,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) PolishPrimary else PolishBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTimeSlotIndex = index }
                            .testTag("time_slot_$index")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedTimeSlotIndex = index },
                                colors = RadioButtonDefaults.colors(selectedColor = PolishPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = slot,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PolishTextPrimary
                                )
                                Text(
                                    text = description,
                                    fontSize = 10.sp,
                                    color = PolishTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Devotee & Gotra Information Form
            Text(
                text = "4. Pilgrim & Sankalp Details",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = PolishTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = pilgrimName,
                onValueChange = { pilgrimName = it },
                label = { Text("Yatri / Head of Family Name *") },
                placeholder = { Text("e.g., Rajesh Sharma") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PolishPrimary) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_pilgrim_name")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = pilgrimPhone,
                    onValueChange = { pilgrimPhone = it },
                    label = { Text("Mobile Number *") },
                    placeholder = { Text("10-digit number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PolishPrimary) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("input_pilgrim_phone")
                )

                OutlinedTextField(
                    value = gotra,
                    onValueChange = { gotra = it },
                    label = { Text("Gotra (Optional)") },
                    placeholder = { Text("e.g., Kashyap") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_pilgrim_gotra")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Number of Devotees Counter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(PolishCardSurface)
                    .border(1.dp, PolishBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Number of Devotees",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextPrimary
                    )
                    Text(
                        text = "Joining for the holy sankalp",
                        fontSize = 10.sp,
                        color = PolishTextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (numPersons > 1) numPersons-- },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("－", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PolishPrimary)
                    }
                    Text(
                        text = "$numPersons",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PolishTextPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(
                        onClick = { if (numPersons < 25) numPersons++ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("＋", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PolishPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Puja Samagri Checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { samagriIncluded = samagriIncluded.not() }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = samagriIncluded,
                    onCheckedChange = { samagriIncluded = it },
                    colors = CheckboxDefaults.colors(checkedColor = PolishPrimary)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Puja Samagri to be arranged by Purohit Ji",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = PolishTextPrimary
                    )
                    Text(
                        text = "Includes Flowers, Ganga Jal, Diya, Roli, Akshat, Janeu, Sweet Prashad",
                        fontSize = 10.sp,
                        color = PolishTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Voluntary Dakshina & No Forceful Charges Assurance Banner
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Emerald50,
                border = BorderStroke(1.dp, Emerald200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🛡️", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Pilgrim Protection Policy (Kumbh 1920)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald900
                        )
                        Text(
                            text = "Dakshina is 100% voluntary at your devotion. No advance payment required online. Pay directly to Purohit Ji after the ritual is performed peacefully.",
                            fontSize = 9.sp,
                            color = Emerald800
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Booking Summary & Confirm Button
            Button(
                onClick = {
                    if (pilgrimName.trim().isEmpty()) {
                        Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (pilgrimPhone.trim().length < 8) {
                        Toast.makeText(context, "Please enter a valid mobile number", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val chosenDate = calendarDays[selectedDateIndex].third
                    val chosenTime = timeSlots[selectedTimeSlotIndex].first

                    val details = RitualBookingDetails(
                        bookingId = "PY-${System.currentTimeMillis().toString().takeLast(6)}",
                        pandaId = panda.id,
                        pandaName = panda.name,
                        ghatLocation = panda.ghatLocation,
                        ritualTitle = selectedService.title,
                        selectedDate = chosenDate,
                        selectedTimeSlot = chosenTime,
                        pilgrimName = pilgrimName.trim(),
                        pilgrimPhone = pilgrimPhone.trim(),
                        gotra = gotra.trim().ifEmpty { "Not specified" },
                        numPersons = numPersons,
                        estimatedDakshina = selectedService.dakshinaGuide,
                        samagriRequired = samagriIncluded,
                        specialNotes = specialNotes
                    )

                    confirmedDetails = details
                    isSuccessModalOpen = true
                    onConfirmBooking(details)
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("confirm_booking_button")
            ) {
                Text(
                    text = "Confirm Booking for ${calendarDays[selectedDateIndex].first} ${calendarDays[selectedDateIndex].second}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
}

/**
 * Lottie Success Celebration Composable
 * Renders celebratory Lottie animation from raw resources with graceful animated fallback.
 */
@Composable
fun LottieSuccessCelebration(
    modifier: Modifier = Modifier,
    sizeDp: Int = 130
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.success_animation))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        speed = 1.0f
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(sizeDp.dp)
    ) {
        if (composition != null) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            val pulseScale by animateFloatAsState(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                label = "fallback_pulse"
            )
            Surface(
                shape = CircleShape,
                color = Emerald50,
                border = BorderStroke(2.dp, Emerald200),
                modifier = Modifier
                    .size((sizeDp * 0.7).dp)
                    .scale(pulseScale)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Emerald600,
                        modifier = Modifier.size((sizeDp * 0.45).dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RitualDetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = PolishTextSecondary
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = PolishTextPrimary,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
