package com.example.ui.screens

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.OfficialBookingRecord
import com.example.data.model.ReservationCategory
import com.example.data.model.VerificationHistoryItem
import com.example.data.model.VerificationStatus
import com.example.ui.theme.*
import com.example.ui.viewmodel.BookingVerificationUiState
import com.example.ui.viewmodel.BookingVerificationViewModel
import com.example.ui.viewmodel.HistoryFilter
import com.example.ui.viewmodel.VerificationMode
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingVerificationScreen(
    onBack: () -> Unit = {},
    onNavigateToVerifiedPurohits: () -> Unit = {},
    onNavigateToOfficialBoats: () -> Unit = {},
    viewModel: BookingVerificationViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showAntiFraudInfoDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Booking Verification",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = PolishTextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PolishGreenBg,
                                border = BorderStroke(1.dp, PolishGreen.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = PolishGreen,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "ANTI-FRAUD",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PolishGreen
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Prayagraj Mela Pradhikaran Central Registry",
                            style = MaterialTheme.typography.labelSmall,
                            color = PolishTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("verification_back_button")
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
                        onClick = { showAntiFraudInfoDialog = true },
                        modifier = Modifier.testTag("anti_fraud_info_button")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PolishPrimaryContainer,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Anti-Fraud Rules",
                                    tint = PolishPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PolishCardSurface
                )
            )
        },
        containerColor = PolishBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("booking_verification_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Official Government Anti-Fraud Seal Banner
            item {
                OfficialRegistryNoticeBanner(
                    onOpenHelpline = { dialHelplineNumber(context, "1920") }
                )
            }

            // 2. Mode Tabs (Enter ID vs Scan QR vs History)
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PolishSurface,
                    border = BorderStroke(1.dp, PolishBorder)
                ) {
                    val activeIndex = when (uiState.activeMode) {
                        VerificationMode.ENTER_ID -> 0
                        VerificationMode.SCAN_QR -> 1
                        VerificationMode.RECENT_HISTORY -> 2
                    }

                    TabRow(
                        selectedTabIndex = activeIndex,
                        containerColor = PolishSurface,
                        contentColor = PolishPrimary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[activeIndex]),
                                color = PolishPrimary
                            )
                        }
                    ) {
                        Tab(
                            selected = uiState.activeMode == VerificationMode.ENTER_ID,
                            onClick = { viewModel.switchMode(VerificationMode.ENTER_ID) },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Pin, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Enter ID", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        )
                        Tab(
                            selected = uiState.activeMode == VerificationMode.SCAN_QR,
                            onClick = { viewModel.switchMode(VerificationMode.SCAN_QR) },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Scan QR", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        )
                        Tab(
                            selected = uiState.activeMode == VerificationMode.RECENT_HISTORY,
                            onClick = { viewModel.switchMode(VerificationMode.RECENT_HISTORY) },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (uiState.historyList.isNotEmpty()) "History (${uiState.historyList.size})" else "History",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        )
                    }
                }
            }

            // 3. Mode Content Sections
            when (uiState.activeMode) {
                VerificationMode.ENTER_ID -> {
                    item {
                        EnterBookingIdSection(
                            inputText = uiState.inputBookingId,
                            isVerifying = uiState.isVerifying,
                            onTextChange = { viewModel.updateInput(it) },
                            onPaste = {
                                val clipText = clipboardManager.getText()?.text
                                if (!clipText.isNullOrBlank()) {
                                    viewModel.updateInput(clipText.trim())
                                } else {
                                    Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onVerify = { viewModel.verifyEnteredId() },
                            onSelectSample = { sampleCode -> viewModel.setSampleId(sampleCode) }
                        )
                    }
                }
                VerificationMode.SCAN_QR -> {
                    item {
                        QrCodeScannerHudView(
                            isScanning = uiState.isCameraScanning,
                            isFlashOn = uiState.isFlashOn,
                            onToggleFlash = { viewModel.toggleFlash() },
                            onSimulateScan = { qrPayload -> viewModel.verifyQrPayload(qrPayload) }
                        )
                    }
                }
                VerificationMode.RECENT_HISTORY -> {
                    item {
                        VerificationHistorySection(
                            historyList = uiState.filteredHistoryList,
                            totalHistoryCount = uiState.historyList.size,
                            selectedFilter = uiState.selectedFilter,
                            onSelectFilter = { viewModel.setHistoryFilter(it) },
                            onSelectHistoryItem = { item -> viewModel.selectHistoryItem(item) },
                            onDeleteHistoryItem = { bookingId -> viewModel.deleteHistoryItem(bookingId) },
                            onClearHistory = { viewModel.clearHistory() }
                        )
                    }
                }
            }

            // 4. Verification Result Card (if a result is present)
            uiState.verificationResult?.let { record ->
                item {
                    OfficialVerificationResultCard(
                        record = record,
                        onClearResult = { viewModel.clearResult() },
                        onNavigateToLocation = { launchGoogleMapsNavigation(context, record.latitude, record.longitude, record.serviceLocation) },
                        onCallProvider = { dialHelplineNumber(context, record.providerContact) },
                        onReportScam = { dialHelplineNumber(context, record.reportHelplineNumber) },
                        onShareCertificate = { shareVerificationCertificate(context, record) },
                        onBookOfficialPurohit = onNavigateToVerifiedPurohits,
                        onBookOfficialBoat = onNavigateToOfficialBoats
                    )
                }
            }

            // 5. Anti-Fraud Rules & Security Checklist
            item {
                AntiFraudDirectivesSection(
                    onCallTourismPolice = { dialHelplineNumber(context, "+91 532 250 1199") },
                    onCallMelaHelpline = { dialHelplineNumber(context, "1920") }
                )
            }
        }
    }

    if (showAntiFraudInfoDialog) {
        AntiFraudInfoModalDialog(onDismiss = { showAntiFraudInfoDialog = false })
    }
}

/**
 * Notice banner explaining government anti-fraud guidelines.
 */
@Composable
fun OfficialRegistryNoticeBanner(
    onOpenHelpline: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Saffron50,
        border = BorderStroke(1.dp, Saffron300)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Saffron100,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "🛡️", fontSize = 20.sp)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Official Cross-Reference Registry",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Saffron900
                )
                Text(
                    text = "Verify authorized Purohit sankalps, boat passes & temple VIP permits against the central database to eliminate fake receipts.",
                    fontSize = 11.sp,
                    color = Saffron900.copy(alpha = 0.85f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Saffron700,
                modifier = Modifier.clickable { onOpenHelpline() }
            ) {
                Text(
                    text = "1920",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }
        }
    }
}

/**
 * Section to manually enter or paste Booking ID / Verification Code.
 */
@Composable
fun EnterBookingIdSection(
    inputText: String,
    isVerifying: Boolean,
    onTextChange: (String) -> Unit,
    onPaste: () -> Unit,
    onVerify: () -> Unit,
    onSelectSample: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = PolishCardSurface,
        border = BorderStroke(1.dp, PolishBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enter Booking ID or Permit No.",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PolishTextPrimary
                )
                TextButton(
                    onClick = onPaste,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp), tint = PolishPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Paste", fontSize = 12.sp, color = PolishPrimary, fontWeight = FontWeight.Bold)
                }
            }

            OutlinedTextField(
                value = inputText,
                onValueChange = { onTextChange(it.uppercase(Locale.ROOT)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("booking_id_input"),
                placeholder = {
                    Text("e.g. PY-2026-8841 or BOAT-SG-2026-0429", fontSize = 13.sp, color = PolishTextTertiary)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.ConfirmationNumber,
                        contentDescription = null,
                        tint = PolishPrimary
                    )
                },
                trailingIcon = {
                    if (inputText.isNotEmpty()) {
                        IconButton(onClick = { onTextChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = PolishTextTertiary)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PolishPrimary,
                    unfocusedBorderColor = PolishBorderDarker,
                    focusedContainerColor = PolishSurface,
                    unfocusedContainerColor = PolishSurface
                )
            )

            // Primary Verification Action Button
            Button(
                onClick = onVerify,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("verify_booking_submit_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                enabled = !isVerifying
            ) {
                if (isVerifying) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Cross-referencing Database...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verify with Official Database", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            // Quick Test / Demo Reservation IDs
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Quick Demo Test Samples:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PolishTextSecondary
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        SampleChip(
                            label = "🪔 Vedic Sankalp",
                            code = "PY-2026-8841",
                            isFraud = false,
                            onClick = { onSelectSample("PY-2026-8841") }
                        )
                    }
                    item {
                        SampleChip(
                            label = "🛶 Sangam Boat",
                            code = "BOAT-SG-2026-0429",
                            isFraud = false,
                            onClick = { onSelectSample("BOAT-SG-2026-0429") }
                        )
                    }
                    item {
                        SampleChip(
                            label = "🛕 VIP Darshan",
                            code = "DARSHAN-BH-2026-108",
                            isFraud = false,
                            onClick = { onSelectSample("DARSHAN-BH-2026-108") }
                        )
                    }
                    item {
                        SampleChip(
                            label = "⛺ Tent City",
                            code = "TENT-KMB-2026-99",
                            isFraud = false,
                            onClick = { onSelectSample("TENT-KMB-2026-99") }
                        )
                    }
                    item {
                        SampleChip(
                            label = "⚠️ Fake Tout Slip",
                            code = "FAKE-TOUT-9988",
                            isFraud = true,
                            onClick = { onSelectSample("FAKE-TOUT-9988") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SampleChip(
    label: String,
    code: String,
    isFraud: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isFraud) Color(0xFFFEF2F2) else PolishPrimaryContainer,
        border = BorderStroke(1.dp, if (isFraud) Color(0xFFFCA5A5) else PolishPrimary.copy(alpha = 0.3f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isFraud) Color(0xFF991B1B) else PolishPrimary
            )
            Text(
                text = code,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = PolishTextSecondary
            )
        }
    }
}

/**
 * QR Code Scanner Viewfinder HUD with animated scanning laser.
 */
@Composable
fun QrCodeScannerHudView(
    isScanning: Boolean,
    isFlashOn: Boolean,
    onToggleFlash: () -> Unit,
    onSimulateScan: (String) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, Color(0xFF334155))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF22C55E),
                        modifier = Modifier.size(8.dp)
                    ) {}
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LIVE QR SCANNER HUD",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }

                IconButton(
                    onClick = onToggleFlash,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Toggle Torch",
                        tint = if (isFlashOn) Color(0xFFFBBF24) else Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Viewfinder Box
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E293B))
                    .border(1.dp, Color(0xFF475569), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Background Scanner / Radar Lottie Animation
                val radarComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.verification_searching_animation))
                val radarProgress by animateLottieCompositionAsState(
                    composition = radarComposition,
                    iterations = LottieConstants.IterateForever,
                    isPlaying = isScanning
                )
                if (radarComposition != null) {
                    LottieAnimation(
                        composition = radarComposition,
                        progress = { radarProgress },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }

                // Corner Brackets HUD
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .border(2.dp, Saffron400, RoundedCornerShape(12.dp))
                )

                // Laser scan line
                if (isScanning) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(2.dp)
                            .offset(y = ((laserOffset - 0.5f) * 160).dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.Transparent, Saffron400, Color(0xFF22C55E), Saffron400, Color.Transparent)
                                )
                            )
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "📷", fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Align QR Code inside frame",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }

            Text(
                text = "Point camera at your printed booking receipt or digital passes issued by UP Tourism, Panda Parishad, or Boat Authority.",
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            // Direct QR payload test triggers
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Tap to Simulate Live QR Scan:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Saffron300
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onSimulateScan("https://prayagraj.up.gov.in/verify?id=PY-2026-8841") },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFF475569)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text("Vedic QR", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { onSimulateScan("https://prayagraj.up.gov.in/verify?id=BOAT-SG-2026-0429") },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFF475569)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text("Boat QR", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { onSimulateScan("https://prayagraj.up.gov.in/verify?id=FAKE-TOUT-9988") },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF87171)),
                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text("Fake QR ⚠️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Official Verification Result Card displaying authentic certificate, counterfeit warnings, or not found.
 */
@Composable
fun OfficialVerificationResultCard(
    record: OfficialBookingRecord,
    onClearResult: () -> Unit,
    onNavigateToLocation: () -> Unit,
    onCallProvider: () -> Unit,
    onReportScam: () -> Unit,
    onShareCertificate: () -> Unit,
    onBookOfficialPurohit: () -> Unit,
    onBookOfficialBoat: () -> Unit
) {
    val isVerified = record.status == VerificationStatus.OFFICIALLY_VERIFIED
    val isFraud = record.status == VerificationStatus.FLAGGED_BLACKLISTED_TOUT || record.isBlacklisted
    val isNotFound = record.status == VerificationStatus.SUSPICIOUS_UNREGISTERED_FRAUD
    val isExpired = record.status == VerificationStatus.EXPIRED || record.status == VerificationStatus.CANCELLED

    val containerBg = when {
        isVerified -> PolishGreenBg
        isFraud -> Color(0xFFFEF2F2)
        isNotFound -> Saffron50
        else -> Color(0xFFF1F5F9)
    }

    val borderStrokeColor = when {
        isVerified -> PolishGreen
        isFraud -> Color(0xFFEF4444)
        isNotFound -> Saffron500
        else -> PolishBorderDarker
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .testTag("verification_result_card"),
        shape = RoundedCornerShape(20.dp),
        color = containerBg,
        border = BorderStroke(1.5.dp, borderStrokeColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row with Status Badge, Lottie Visual Indicator & Dismiss
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    val statusColor = when {
                        isVerified -> PolishGreen
                        isFraud -> Color(0xFFDC2626)
                        isNotFound -> Saffron700
                        else -> PolishTextSecondary
                    }

                    // Lottie Animation Visual Feedback (Success Checkmark / Red Warning / Neutral)
                    VerificationLottieFeedbackIcon(
                        isVerified = isVerified,
                        isFraud = isFraud,
                        isNotFound = isNotFound,
                        modifier = Modifier.size(36.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when {
                                isVerified -> "OFFICIALLY VERIFIED & AUTHENTIC"
                                isFraud -> "CRITICAL FRAUD / TOUT WARNING"
                                isNotFound -> "UNREGISTERED RESERVATION"
                                else -> "EXPIRED / CANCELLED PASS"
                            },
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = statusColor
                        )
                        Text(
                            text = "Certificate: ${record.registrationCertificateNo}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = PolishTextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onClearResult,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = PolishTextSecondary)
                }
            }

            HorizontalDivider(color = borderStrokeColor.copy(alpha = 0.3f))

            // Main Title & Category
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = record.category.icon, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = record.category.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishPrimary
                    )
                }
                Text(
                    text = record.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PolishTextPrimary,
                    modifier = Modifier.padding(top = 2.dp)
                )
                if (record.hindiTitle.isNotBlank()) {
                    Text(
                        text = record.hindiTitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = PolishPrimary,
                        modifier = Modifier.padding(top = 1.dp)
                    )
                }
            }

            // Key Details Grid
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PolishCardSurface,
                border = BorderStroke(1.dp, PolishBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailRow(label = "Authority", value = record.authorityName)
                    DetailRow(label = "Pilgrim Name", value = "${record.pilgrimName} (${record.gotraOrParty})")
                    DetailRow(label = "Assigned Provider", value = "${record.assignedProviderName} [${record.providerBadgeNo}]")
                    DetailRow(label = "Designated Location", value = record.serviceLocation)
                    DetailRow(label = "Scheduled Slot", value = record.slotDateTime)
                    DetailRow(
                        label = "Fixed Govt Tariff",
                        value = record.fixedGovtTariff,
                        isHighlight = true
                    )
                    DetailRow(
                        label = "Security Seal",
                        value = "${record.antiCounterfeitSeal} (${record.qrHashSignature.take(16)}...)"
                    )
                }
            }

            // Security Notes / Warning text
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isFraud) Color(0xFFFEE2E2) else PolishSurface,
                border = BorderStroke(1.dp, if (isFraud) Color(0xFFFCA5A5) else PolishBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = if (isFraud) "🚨" else "📌",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = record.securityNotes,
                        fontSize = 11.sp,
                        color = if (isFraud) Color(0xFF991B1B) else PolishTextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }

            // Action Buttons
            if (isVerified) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onNavigateToLocation,
                        colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.2f).testTag("verify_navigate_button"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Navigate to Berth", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onCallProvider,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("verify_call_button"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call Provider", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = onShareCertificate,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = PolishPrimary)
                    }
                }
            } else if (isFraud) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onReportScam,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("report_fraud_button")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🚨 Report Scam to Tourism Police (1920 / 112)", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onBookOfficialPurohit,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Find Verified Pandas", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onBookOfficialBoat,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Official Boats", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (isNotFound) {
                Button(
                    onClick = onReportScam,
                    colors = ButtonDefaults.buttonColors(containerColor = Saffron700),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Connect to Sangam Helpdesk (1920)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = PolishTextSecondary,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = if (isHighlight) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (isHighlight) Saffron900 else PolishTextPrimary,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.6f)
        )
    }
}

/**
 * Lottie animation badge providing dynamic feedback for verified vs fraudulent vs unregistered checks.
 */
@Composable
fun VerificationLottieFeedbackIcon(
    isVerified: Boolean,
    isFraud: Boolean,
    isNotFound: Boolean,
    modifier: Modifier = Modifier
) {
    val rawRes = when {
        isVerified -> R.raw.success_animation
        isFraud -> R.raw.error_warning_animation
        else -> R.raw.error_warning_animation
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(rawRes))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = if (isFraud) LottieConstants.IterateForever else 1
    )

    if (composition != null) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = modifier
        )
    } else {
        // Fallback Vector Icon while composition is resolving
        val fallbackIcon = when {
            isVerified -> Icons.Default.CheckCircle
            isFraud -> Icons.Default.GppBad
            isNotFound -> Icons.Default.Warning
            else -> Icons.Default.Cancel
        }
        val fallbackColor = when {
            isVerified -> PolishGreen
            isFraud -> Color(0xFFDC2626)
            isNotFound -> Saffron700
            else -> PolishTextSecondary
        }
        Icon(
            imageVector = fallbackIcon,
            contentDescription = null,
            tint = fallbackColor,
            modifier = modifier
        )
    }
}

/**
 * Section displaying recent verified searches with Room DB persistence and quick reload.
 */
@Composable
fun VerificationHistorySection(
    historyList: List<VerificationHistoryItem>,
    totalHistoryCount: Int,
    selectedFilter: HistoryFilter,
    onSelectFilter: (HistoryFilter) -> Unit,
    onSelectHistoryItem: (VerificationHistoryItem) -> Unit,
    onDeleteHistoryItem: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = PolishCardSurface,
        border = BorderStroke(1.dp, PolishBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Recent Verification Log",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PolishTextPrimary
                    )
                    if (totalHistoryCount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = CircleShape,
                            color = Saffron100
                        ) {
                            Text(
                                text = "$totalHistoryCount",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Saffron900,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (totalHistoryCount > 0) {
                    TextButton(
                        onClick = onClearHistory,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = PolishRed, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Clear All", color = PolishRed, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Filter Chips
            if (totalHistoryCount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (filter in HistoryFilter.entries) {
                        val isSelected = selectedFilter == filter
                        Surface(
                            modifier = Modifier
                                .clickable { onSelectFilter(filter) },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Saffron700 else PolishSurface,
                            border = BorderStroke(1.dp, if (isSelected) Saffron700 else PolishBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = filter.icon, fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = filter.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else PolishTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            if (historyList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = if (totalHistoryCount == 0) "📜" else "🔍", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (totalHistoryCount == 0) {
                                "No recent verifications recorded.\nEnter a booking code or scan a QR to check passes."
                            } else {
                                "No verification records match '${selectedFilter.label}'."
                            },
                            fontSize = 12.sp,
                            color = PolishTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (item in historyList) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectHistoryItem(item) },
                            shape = RoundedCornerShape(12.dp),
                            color = PolishSurface,
                            border = BorderStroke(1.dp, PolishBorderDarker)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.bookingId,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = PolishTextPrimary
                                        )
                                        if (item.tariff.isNotBlank()) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "• ${item.tariff}",
                                                fontSize = 11.sp,
                                                color = PolishTextSecondary
                                            )
                                        }
                                    }
                                    Text(
                                        text = item.title,
                                        fontSize = 11.sp,
                                        color = PolishTextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (item.providerName.isNotBlank() && item.providerName != "N/A") {
                                        Text(
                                            text = "Provider: ${item.providerName}",
                                            fontSize = 10.sp,
                                            color = PolishTextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when (item.status) {
                                            VerificationStatus.OFFICIALLY_VERIFIED -> PolishGreenBg
                                            VerificationStatus.FLAGGED_BLACKLISTED_TOUT -> Color(0xFFFEF2F2)
                                            VerificationStatus.EXPIRED -> Color(0xFFF3F4F6)
                                            else -> Saffron100
                                        }
                                    ) {
                                        Text(
                                            text = when (item.status) {
                                                VerificationStatus.OFFICIALLY_VERIFIED -> "VERIFIED"
                                                VerificationStatus.FLAGGED_BLACKLISTED_TOUT -> "FRAUD"
                                                VerificationStatus.EXPIRED -> "EXPIRED"
                                                else -> "UNVERIFIED"
                                            },
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = when (item.status) {
                                                VerificationStatus.OFFICIALLY_VERIFIED -> PolishGreen
                                                VerificationStatus.FLAGGED_BLACKLISTED_TOUT -> Color(0xFFDC2626)
                                                VerificationStatus.EXPIRED -> Color(0xFF6B7280)
                                                else -> Saffron900
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDeleteHistoryItem(item.bookingId) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove item",
                                            tint = PolishTextSecondary,
                                            modifier = Modifier.size(14.dp)
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
}

/**
 * Anti-Fraud Directives & Police Contacts.
 */
@Composable
fun AntiFraudDirectivesSection(
    onCallTourismPolice: () -> Unit,
    onCallMelaHelpline: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = PolishCardSurface,
        border = BorderStroke(1.dp, PolishBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "⚖️", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pilgrim Anti-Exploitation Directives",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PolishTextPrimary
                )
            }

            AntiFraudRuleItem(
                num = "1",
                title = "Demand Official QR Receipt",
                desc = "Never pay cash to ghat touts without a system-generated QR permit issued by Shri Prayag Mahatmya Parishad or Mela Authority."
            )
            AntiFraudRuleItem(
                num = "2",
                title = "Verify Brass & Digital ID Badges",
                desc = "Official Pandas wear registered brass badges with their assigned lineage registration number. Check before initiating sankalp."
            )
            AntiFraudRuleItem(
                num = "3",
                title = "Fixed Boat Tariffs & Free Life Jackets",
                desc = "Motor/wooden boat fares are strictly capped at ₹120-₹250 per passenger. Life jackets must be provided at zero extra cost."
            )

            HorizontalDivider(color = PolishBorder)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCallTourismPolice,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.LocalPolice, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tourist Police", fontSize = 11.sp)
                }

                Button(
                    onClick = onCallMelaHelpline,
                    colors = ButtonDefaults.buttonColors(containerColor = Saffron700),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Helpline 1920", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AntiFraudRuleItem(num: String, title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = PolishPrimaryContainer,
            modifier = Modifier.size(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = num, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PolishPrimary)
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PolishTextPrimary)
            Text(text = desc, fontSize = 11.sp, color = PolishTextSecondary, lineHeight = 14.sp)
        }
    }
}

@Composable
fun AntiFraudInfoModalDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = PolishCardSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Anti-Fraud Security System",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PolishTextPrimary
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "This portal directly connects to the Prayagraj Mela Pradhikaran and Prayag Mahatmya Purohit Parishad digital registry. Each booking code is cross-referenced using cryptographic hashes to ensure that pilgrims are not overcharged by unregistered brokers.",
                    fontSize = 12.sp,
                    color = PolishTextSecondary,
                    lineHeight = 17.sp
                )

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Understood")
                }
            }
        }
    }
}

private fun dialHelplineNumber(context: Context, phoneNumber: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phoneNumber.replace(" ", "")}"))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not launch dialer for $phoneNumber", Toast.LENGTH_SHORT).show()
    }
}

private fun launchGoogleMapsNavigation(context: Context, latitude: Double, longitude: Double, label: String) {
    val navUri = Uri.parse("google.navigation:q=$latitude,$longitude&mode=d")
    val mapIntent = Intent(Intent.ACTION_VIEW, navUri).apply {
        setPackage("com.google.android.apps.maps")
    }
    try {
        context.startActivity(mapIntent)
    } catch (e: Exception) {
        val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude")
        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
    }
}

private fun shareVerificationCertificate(context: Context, record: OfficialBookingRecord) {
    val shareText = """
        🔱 OFFICIAL PRAYAGRAJ SHRINE VERIFICATION 🔱
        Registration No: ${record.registrationCertificateNo}
        Booking ID: ${record.bookingId}
        Service: ${record.title}
        Pilgrim: ${record.pilgrimName} (${record.gotraOrParty})
        Assigned Provider: ${record.assignedProviderName}
        Location: ${record.serviceLocation}
        Time Slot: ${record.slotDateTime}
        Fixed Govt Tariff: ${record.fixedGovtTariff}
        Status: ${record.status.name} (OFFICIALLY VERIFIED)
        Security Hash: ${record.qrHashSignature}
        
        Verified via Prayagraj Sangam Yatra Anti-Fraud Central Registry.
    """.trimIndent()

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share Official Verification Certificate"))
}
