package com.prayagraj.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.prayagraj.app.data.model.BookingRequestDto
import com.prayagraj.app.ui.viewmodel.BookingDashboardUiState
import com.prayagraj.app.ui.viewmodel.RealtimeBookingViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealtimeBookingDashboardScreen(
    viewModel: RealtimeBookingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.newBookingAlert.collectLatest { newBooking ->
            snackbarHostState.showSnackbar(
                message = "New Booking: ${newBooking.serviceTitle} from ${newBooking.customerName}!",
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Live Booking Requests") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is BookingDashboardUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is BookingDashboardUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is BookingDashboardUiState.Success -> {
                    if (state.bookings.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No booking requests yet. Listening for new orders...")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = state.bookings,
                                key = { it.id }
                            ) { booking ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn() + expandVertically()
                                ) {
                                    BookingRequestCard(
                                        booking = booking,
                                        onAccept = { viewModel.respondToBooking(booking.id, true) },
                                        onReject = { viewModel.respondToBooking(booking.id, false) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookingRequestCard(
    booking: BookingRequestDto,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(booking.serviceTitle, style = MaterialTheme.typography.titleMedium)
                StatusBadge(status = booking.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Customer: ${booking.customerName} (${booking.customerPhone})")
            Text("Booking Date: ${booking.bookingDate}")
            Text("Amount: ₹${booking.amount}", style = MaterialTheme.typography.titleSmall)

            if (booking.status == "PENDING") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Decline")
                    }

                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Accept")
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor) = when (status) {
        "ACCEPTED" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "REJECTED" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        else -> Color(0xFFFFF3E0) to Color(0xFFEF6C00)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = status,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
