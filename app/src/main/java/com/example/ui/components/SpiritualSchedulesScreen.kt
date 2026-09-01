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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
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
import com.example.data.local.RitualBookingEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.RitualBookingViewModel
import com.example.ui.viewmodel.ScheduleFilterTab
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpiritualSchedulesScreen(
    onBack: () -> Unit = {},
    onNavigateToPurohits: () -> Unit = {},
    viewModel: RitualBookingViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var bookingToCancel by remember { mutableStateOf<RitualBookingEntity?>(null) }
    var bookingToDelete by remember { mutableStateOf<RitualBookingEntity?>(null) }

    LaunchedEffect(uiState.userFeedbackMessage) {
        uiState.userFeedbackMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearFeedbackMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Spiritual Schedules",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = PolishTextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Saffron100
                            ) {
                                Text(
                                    text = "Room DB",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Saffron900,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Upcoming Darshan & Ritual Booking History",
                            style = MaterialTheme.typography.labelSmall,
                            color = PolishTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("schedules_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PolishTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToPurohits,
                        modifier = Modifier.testTag("schedules_book_new_button")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PolishPrimaryContainer,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Book Ritual",
                                    tint = PolishPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PolishBackground
                )
            )
        },
        containerColor = PolishBackground,
        modifier = modifier.testTag("spiritual_schedules_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Summary Stats & Storage Indicator Banner
            Surface(
                color = Saffron50,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Saffron200)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🕉️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Local Room Persistence Active",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Saffron900
                            )
                            Text(
                                text = "${uiState.upcomingCount} Active Sankalp • ${uiState.pastBookings.size} Past Records",
                                fontSize = 10.sp,
                                color = Saffron800
                            )
                        }
                    }
                    Button(
                        onClick = onNavigateToPurohits,
                        colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Book Ritual", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Filter Tabs (Upcoming / Past / All)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScheduleTabButton(
                    label = "Upcoming (${uiState.upcomingBookings.size})",
                    isSelected = uiState.selectedTab == ScheduleFilterTab.UPCOMING,
                    onClick = { viewModel.selectTab(ScheduleFilterTab.UPCOMING) },
                    modifier = Modifier.weight(1f)
                )
                ScheduleTabButton(
                    label = "Past History (${uiState.pastBookings.size})",
                    isSelected = uiState.selectedTab == ScheduleFilterTab.PAST,
                    onClick = { viewModel.selectTab(ScheduleFilterTab.PAST) },
                    modifier = Modifier.weight(1f)
                )
                ScheduleTabButton(
                    label = "All (${uiState.bookings.size})",
                    isSelected = uiState.selectedTab == ScheduleFilterTab.ALL,
                    onClick = { viewModel.selectTab(ScheduleFilterTab.ALL) },
                    modifier = Modifier.weight(0.8f)
                )
            }

            // Content List based on selected tab
            val currentList = when (uiState.selectedTab) {
                ScheduleFilterTab.UPCOMING -> uiState.upcomingBookings
                ScheduleFilterTab.PAST -> uiState.pastBookings
                ScheduleFilterTab.ALL -> uiState.bookings
            }

            if (currentList.isEmpty()) {
                EmptySchedulesView(
                    tab = uiState.selectedTab,
                    onBookRitual = onNavigateToPurohits
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(
                        items = currentList,
                        key = { it.id }
                    ) { booking ->
                        RitualBookingScheduleCard(
                            booking = booking,
                            onComplete = { viewModel.markBookingCompleted(booking.id) },
                            onCancel = { bookingToCancel = booking },
                            onDelete = { bookingToDelete = booking },
                            onViewDetails = { viewModel.selectBookingForDetail(booking) }
                        )
                    }
                }
            }
        }
    }

    // Cancellation Confirmation Dialog
    bookingToCancel?.let { booking ->
        AlertDialog(
            onDismissRequest = { bookingToCancel = null },
            title = {
                Text(
                    text = "Cancel Ritual Booking?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = PolishTextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to cancel the booking for '${booking.ritualTitle}' with ${booking.pandaName} at ${booking.ghatLocation}?",
                    fontSize = 12.sp,
                    color = PolishTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelBooking(booking.id)
                        bookingToCancel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm Cancel")
                }
            },
            dismissButton = {
                TextButton(onClick = { bookingToCancel = null }) {
                    Text("Keep Booking", color = PolishPrimary)
                }
            }
        )
    }

    // Delete Record Confirmation Dialog
    bookingToDelete?.let { booking ->
        AlertDialog(
            onDismissRequest = { bookingToDelete = null },
            title = {
                Text(
                    text = "Delete History Record?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = PolishTextPrimary
                )
            },
            text = {
                Text(
                    text = "This will permanently remove booking record ${booking.id} from local Room storage.",
                    fontSize = 12.sp,
                    color = PolishTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteBooking(booking.id)
                        bookingToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { bookingToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Full Details Modal Bottom Sheet
    uiState.selectedBookingForDetail?.let { booking ->
        RitualBookingDetailSheet(
            booking = booking,
            onDismiss = { viewModel.selectBookingForDetail(null) },
            onComplete = {
                viewModel.markBookingCompleted(booking.id)
                viewModel.selectBookingForDetail(null)
            },
            onCancel = {
                bookingToCancel = booking
                viewModel.selectBookingForDetail(null)
            }
        )
    }
}

@Composable
fun ScheduleTabButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) PolishPrimary else PolishCardSurface,
        border = BorderStroke(1.dp, if (isSelected) PolishPrimary else PolishBorder),
        modifier = modifier
            .height(38.dp)
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else PolishTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun RitualBookingScheduleCard(
    booking: RitualBookingEntity,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onViewDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = PolishCardSurface,
        border = BorderStroke(1.dp, if (booking.isUpcoming) PolishPrimary.copy(alpha = 0.3f) else PolishBorder),
        shadowElevation = if (booking.isUpcoming) 2.dp else 0.dp,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("ritual_card_${booking.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Token ID + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "TOKEN: ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Saffron900
                    )
                    Text(
                        text = booking.id,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PolishPrimary,
                        letterSpacing = 1.sp
                    )
                }

                when (booking.status) {
                    "CONFIRMED" -> {
                        Surface(shape = RoundedCornerShape(6.dp), color = Emerald50, border = BorderStroke(1.dp, Emerald200)) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Emerald600)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "UPCOMING",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald900
                                )
                            }
                        }
                    }
                    "COMPLETED" -> {
                        Surface(shape = RoundedCornerShape(6.dp), color = Saffron50, border = BorderStroke(1.dp, Saffron200)) {
                            Text(
                                text = "COMPLETED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Saffron900,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    else -> {
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFFEBEE), border = BorderStroke(1.dp, Color(0xFFFFCDD2))) {
                            Text(
                                text = "CANCELLED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = PolishBorder.copy(alpha = 0.6f))

            // Main Details: Ritual Title + Purohit Ji
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = booking.ritualTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PolishTextPrimary
                )
                Text(
                    text = "Purohit: ${booking.pandaName}",
                    fontSize = 12.sp,
                    color = PolishPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "📍 ${booking.ghatLocation}",
                    fontSize = 11.sp,
                    color = PolishTextSecondary
                )
            }

            // Auspicious Muhurta / Scheduled Date & Time Chip
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Saffron50,
                border = BorderStroke(1.dp, Saffron200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⏰", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = booking.bookingDate,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Saffron900
                        )
                        Text(
                            text = booking.timeSlot,
                            fontSize = 10.sp,
                            color = Saffron800
                        )
                    }
                }
            }

            // Devotee Meta info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "👥 ${booking.numPersons} Devotees (Gotra: ${booking.gotra})",
                    fontSize = 10.sp,
                    color = PolishTextSecondary
                )
                Text(
                    text = "Dakshina: ${booking.estimatedDakshina}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = PolishTextPrimary
                )
            }

            // Interactive Action Buttons
            if (booking.isUpcoming) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Call Purohit
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${booking.pandaPhone.ifEmpty { "+919415028471" }}")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(14.dp), tint = PolishPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call", fontSize = 11.sp, color = PolishPrimary)
                    }

                    // Share Token
                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "🕉️ Prayagraj Yatra - Sacred Ritual Booking\n" +
                                            "Token ID: ${booking.id}\n" +
                                            "Ritual: ${booking.ritualTitle}\n" +
                                            "Purohit: ${booking.pandaName}\n" +
                                            "Location: ${booking.ghatLocation}\n" +
                                            "Time: ${booking.bookingDate} (${booking.timeSlot})\n" +
                                            "Yatri: ${booking.pilgrimName}"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Booking Token"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(14.dp), tint = PolishPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", fontSize = 11.sp, color = PolishPrimary)
                    }

                    // Complete / Done
                    Button(
                        onClick = onComplete,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(34.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Complete", modifier = Modifier.size(14.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark Done", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onCancel,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Cancel Booking", fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                    }

                    TextButton(
                        onClick = onViewDetails,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Full Token Details →", fontSize = 10.sp, color = PolishPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDelete,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(14.dp), tint = PolishTextSecondary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Remove Record", fontSize = 10.sp, color = PolishTextSecondary)
                    }

                    TextButton(
                        onClick = onViewDetails,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("View Receipt →", fontSize = 10.sp, color = PolishPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptySchedulesView(
    tab: ScheduleFilterTab,
    onBookRitual: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Saffron50,
                border = BorderStroke(1.dp, Saffron200),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (tab == ScheduleFilterTab.UPCOMING) "🕉️" else "📜",
                        fontSize = 32.sp
                    )
                }
            }

            Text(
                text = when (tab) {
                    ScheduleFilterTab.UPCOMING -> "No Upcoming Ritual Schedules"
                    ScheduleFilterTab.PAST -> "No Past Ritual Records"
                    ScheduleFilterTab.ALL -> "No Spiritual Schedules Recorded"
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PolishTextPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = when (tab) {
                    ScheduleFilterTab.UPCOMING -> "You haven't scheduled any Snan, Pind Daan, or Rudrabhishek ceremonies yet. Book with a verified Tirtha Purohit to reserve your auspicious Muhurta."
                    ScheduleFilterTab.PAST -> "Completed ceremonies and historical Sankalps will appear here once marked as done."
                    ScheduleFilterTab.ALL -> "Your sacred ritual bookings are stored locally in Room database for offline access at the Ghats."
                },
                fontSize = 12.sp,
                color = PolishTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Button(
                onClick = onBookRitual,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary)
            ) {
                Text("Explore Verified Tirtha Purohits", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RitualBookingDetailSheet(
    booking: RitualBookingEntity,
    onDismiss: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val modalState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalState,
        containerColor = PolishBackground,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Sacred Ritual Booking Pass",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PolishTextPrimary
            )

            // Pilgrim Token Box
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
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Saffron800,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = booking.id,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = PolishPrimary,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Status: ${booking.status}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (booking.isUpcoming) Emerald600 else Saffron900
                    )
                }
            }

            // Detailed Itemized Summary
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PolishCardSurface,
                border = BorderStroke(1.dp, PolishBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "📋 Ritual Details", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PolishTextPrimary)
                    HorizontalDivider(color = PolishBorder.copy(alpha = 0.6f))
                    RitualDetailRow(label = "Ritual", value = booking.ritualTitle)
                    RitualDetailRow(label = "Purohit Ji", value = booking.pandaName)
                    RitualDetailRow(label = "Ghat Location", value = booking.ghatLocation)
                    RitualDetailRow(label = "Scheduled Date", value = booking.bookingDate)
                    RitualDetailRow(label = "Muhurta Slot", value = booking.timeSlot)
                    RitualDetailRow(label = "Devotee Name", value = "${booking.pilgrimName} (Gotra: ${booking.gotra})")
                    RitualDetailRow(label = "Devotees Count", value = "${booking.numPersons} Persons")
                    RitualDetailRow(label = "Samagri", value = if (booking.samagriRequired) "Arranged by Purohit Ji" else "Self-Arranged")
                    RitualDetailRow(label = "Dakshina Guide", value = booking.estimatedDakshina)
                    if (booking.specialNotes.isNotBlank()) {
                        RitualDetailRow(label = "Sankalp Notes", value = booking.specialNotes)
                    }
                }
            }

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
                                "🕉️ Prayagraj Yatra - Ritual Booking\n" +
                                        "Token ID: ${booking.id}\n" +
                                        "Ritual: ${booking.ritualTitle}\n" +
                                        "Purohit: ${booking.pandaName}\n" +
                                        "Location: ${booking.ghatLocation}\n" +
                                        "Date: ${booking.bookingDate} (${booking.timeSlot})\n" +
                                        "Yatri: ${booking.pilgrimName}"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Booking Token"))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp), tint = PolishPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share", color = PolishPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary)
                ) {
                    Text("Close", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
