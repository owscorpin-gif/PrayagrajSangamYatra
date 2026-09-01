package com.example.ui.screens

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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.example.ui.components.SpiritualSchedulesScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.model.PandaServiceOffering
import com.example.data.model.VerifiedPanda
import com.example.ui.components.LottieSuccessCelebration
import com.example.ui.theme.*
import com.example.ui.viewmodel.PandaViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PandaDirectoryScreen(
    onBack: () -> Unit = {},
    onNavigateToAiGuide: () -> Unit = {},
    viewModel: PandaViewModel = viewModel(),
    initialGhatCategory: String? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedGhatFilter by remember { mutableStateOf(initialGhatCategory ?: "all") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Verified Tirtha Purohits",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PolishTextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PolishGreenBg
                            ) {
                                Text(
                                    text = "GOVT & SABHA CERTIFIED",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PolishGreen,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Prayagraj Tirtha Purohit Maha Sabha Registry",
                            style = MaterialTheme.typography.labelSmall,
                            color = PolishTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("panda_screen_back_button")
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
                        onClick = { viewModel.toggleScheduleHistorySheet(true) },
                        modifier = Modifier.testTag("panda_schedules_action")
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Surface(
                                shape = CircleShape,
                                color = Saffron100,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🕉️", fontSize = 14.sp)
                                }
                            }
                            if (uiState.upcomingBookingsCount > 0) {
                                Surface(
                                    shape = CircleShape,
                                    color = PolishPrimary,
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${uiState.upcomingBookingsCount}",
                                            fontSize = 9.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                    IconButton(
                        onClick = onNavigateToAiGuide,
                        modifier = Modifier.testTag("panda_ai_guide_action")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PolishPrimaryContainer,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("✨", fontSize = 14.sp)
                            }
                        }
                    }
                    IconButton(
                        onClick = { viewModel.toggleFilterSheet(true) },
                        modifier = Modifier.testTag("panda_filter_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filters",
                            tint = PolishPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PolishBackground
                )
            )
        },
        containerColor = PolishBackground,
        modifier = modifier.testTag("panda_directory_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Anti-Exploitation AI Guide Prompt Banner
            Surface(
                color = Saffron50,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToAiGuide() }
                    .border(1.dp, Saffron200)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "✨", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Unsure about fair ritual costs or Dakshina?",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Saffron900
                        )
                        Text(
                            text = "Ask Prayag AI Guide for transparent community rates & verification rules.",
                            fontSize = 10.sp,
                            color = Saffron800
                        )
                    }
                    Text(
                        text = "Ask AI →",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishPrimary
                    )
                }
            }

            // Search Input & Action Bar
            PandaSearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) },
                onOpenFilterSheet = { viewModel.toggleFilterSheet(true) }
            )

            // Ritual Categories Filter Chips (Horizontal)
            RitualCategoryFilterRow(
                categories = viewModel.availableRitualCategories,
                selectedCategory = uiState.selectedRitualCategory,
                onSelectCategory = { viewModel.selectRitualCategory(it) }
            )

            // Ghat Location Filter Chips
            GhatLocationFilterRow(
                ghats = viewModel.availableGhats,
                selectedGhat = uiState.selectedGhat,
                onSelectGhat = { viewModel.selectGhat(it) }
            )

            // Quick Badges: Bahi-Khata filter, Language filter indicator, Count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Bahi-Khata Toggle Chip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (uiState.bahiKhataOnly) PolishPrimaryContainer else PolishCardSurface,
                        border = BorderStroke(
                            1.dp,
                            if (uiState.bahiKhataOnly) PolishPrimary else PolishBorder
                        ),
                        modifier = Modifier
                            .clickable { viewModel.toggleBahiKhataOnly(!uiState.bahiKhataOnly) }
                            .testTag("bahi_khata_filter_chip")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "📜 ", fontSize = 11.sp)
                            Text(
                                text = "Ancestry Bahi-Khata",
                                fontSize = 11.sp,
                                fontWeight = if (uiState.bahiKhataOnly) FontWeight.Bold else FontWeight.Medium,
                                color = if (uiState.bahiKhataOnly) PolishPrimary else PolishTextSecondary
                            )
                        }
                    }

                    // Clear Filters button if any active
                    if (uiState.selectedRitualCategory != "All Rituals" ||
                        uiState.selectedGhat != "all" ||
                        uiState.selectedLanguage != "All" ||
                        uiState.bahiKhataOnly ||
                        uiState.searchQuery.isNotEmpty()
                    ) {
                        Text(
                            text = "Reset Filters",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishRed,
                            modifier = Modifier
                                .clickable { viewModel.clearFilters() }
                                .padding(horizontal = 4.dp)
                                .testTag("reset_filters_button")
                        )
                    }
                }

                Text(
                    text = "${uiState.pandas.size} Purohits",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PolishTextSecondary
                )
            }

            // Pandas List or Empty State
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PolishPrimary)
                }
            } else if (uiState.pandas.isEmpty()) {
                EmptyPandasState(
                    onClearFilters = { viewModel.clearFilters() }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        // Trust & Accreditation Banner
                        AccreditationTrustBanner()
                    }

                    items(uiState.pandas, key = { it.id }) { panda ->
                        PandaDirectoryCard(
                            panda = panda,
                            onCall = { launchPhoneDialer(context, panda.phoneNumber) },
                            onWhatsApp = { launchWhatsAppChat(context, panda.whatsappNumber, panda.name) },
                            onBookRitual = { service -> viewModel.startBooking(panda, service) },
                            onViewDetails = { viewModel.selectPandaForDetails(panda) },
                            onCopyAccreditation = {
                                clipboardManager.setText(AnnotatedString("${panda.name} (${panda.accreditationId}) - Ph: ${panda.phoneNumber}"))
                                Toast.makeText(context, "Contact & Accreditation copied", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    // Panda Full Profile Modal
    uiState.selectedPandaForDetails?.let { panda ->
        PandaDetailsDialog(
            panda = panda,
            onDismiss = { viewModel.selectPandaForDetails(null) },
            onCall = { launchPhoneDialer(context, panda.phoneNumber) },
            onWhatsApp = { launchWhatsAppChat(context, panda.whatsappNumber, panda.name) },
            onBookService = { service ->
                viewModel.selectPandaForDetails(null)
                viewModel.startBooking(panda, service)
            }
        )
    }

    // Ritual Booking Modal
    uiState.bookingPanda?.let { panda ->
        PandaBookingDialog(
            panda = panda,
            selectedService = uiState.selectedServiceForBooking ?: panda.services.firstOrNull(),
            onDismiss = { viewModel.dismissBooking() },
            onConfirmBooking = { service, date, time, devotees, notes ->
                viewModel.confirmBooking(panda, service, date, time, devotees, notes)
            }
        )
    }

    // Booking Success Confirmation Dialog
    uiState.lastConfirmedBookingCode?.let { bookingCode ->
        BookingSuccessDialog(
            bookingCode = bookingCode,
            onDismiss = { viewModel.dismissSuccessConfirmation() },
            onViewSchedules = {
                viewModel.dismissSuccessConfirmation()
                viewModel.toggleScheduleHistorySheet(true)
            }
        )
    }

    // Spiritual Schedules & Room Booking History Full Modal
    if (uiState.isScheduleHistorySheetOpen) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { viewModel.toggleScheduleHistorySheet(false) },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            SpiritualSchedulesScreen(
                onBack = { viewModel.toggleScheduleHistorySheet(false) },
                onNavigateToPurohits = { viewModel.toggleScheduleHistorySheet(false) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    // Advanced Multi-Filter Bottom Sheet
    if (uiState.isFilterSheetOpen) {
        PandaFilterBottomSheet(
            availableLanguages = viewModel.availableLanguages,
            selectedLanguage = uiState.selectedLanguage,
            onSelectLanguage = { viewModel.selectLanguage(it) },
            sortBy = uiState.sortBy,
            onSelectSortBy = { viewModel.setSortBy(it) },
            bahiKhataOnly = uiState.bahiKhataOnly,
            onToggleBahiKhata = { viewModel.toggleBahiKhataOnly(it) },
            onDismiss = { viewModel.toggleFilterSheet(false) },
            onReset = {
                viewModel.clearFilters()
                viewModel.toggleFilterSheet(false)
            }
        )
    }
}

@Composable
fun PandaSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onOpenFilterSheet: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = PolishCardSurface,
        border = BorderStroke(1.dp, PolishBorder),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = PolishPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = "Search by Name, Pind Daan, Ghat, Language...",
                        fontSize = 12.sp,
                        color = PolishTextSecondary
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("panda_search_input")
            )
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = PolishTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RitualCategoryFilterRow(
    categories: List<String>,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val categoryIcons = mapOf(
            "All Rituals" to "🕉️",
            "Snan & Sankalp" to "🌊",
            "Pind Daan & Tarpan" to "🪔",
            "Rudrabhishek" to "🔱",
            "Vedic Havan" to "🔥",
            "Ganga Aarti" to "✨",
            "Asthi Visarjan" to "🙏",
            "Mundan Sanskar" to "✂️"
        )

        categories.forEach { category ->
            val isSelected = selectedCategory.equals(category, ignoreCase = true)
            val icon = categoryIcons[category] ?: "🕉️"

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) PolishPrimary else PolishCardSurface,
                border = BorderStroke(1.dp, if (isSelected) PolishPrimary else PolishBorder),
                modifier = Modifier
                    .clickable { onSelectCategory(category) }
                    .testTag("ritual_filter_$category")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(text = icon, fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = category,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else PolishTextPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun GhatLocationFilterRow(
    ghats: List<Pair<String, String>>,
    selectedGhat: String,
    onSelectGhat: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ghats.forEach { (key, label) ->
            val isSelected = selectedGhat.equals(key, ignoreCase = true)

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) PolishPrimaryContainer else PolishSurface,
                border = BorderStroke(1.dp, if (isSelected) PolishPrimary else PolishBorderDarker),
                modifier = Modifier
                    .clickable { onSelectGhat(key) }
                    .testTag("ghat_filter_$key")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (isSelected) PolishPrimary else PolishTextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) PolishPrimary else PolishTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun AccreditationTrustBanner() {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = PolishPrimaryContainer.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, PolishPrimary.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = PolishPrimary,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Official Prayag Tirtha Purohit Registry",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PolishTextPrimary
                )
                Text(
                    text = "Every Purohit listed is verified with Govt ID, heritage clan registry, and standardized dakshina guidelines.",
                    fontSize = 10.sp,
                    color = PolishTextSecondary,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
fun PandaDirectoryCard(
    panda: VerifiedPanda,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onBookRitual: (PandaServiceOffering?) -> Unit,
    onViewDetails: () -> Unit,
    onCopyAccreditation: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = PolishCardSurface),
        border = BorderStroke(1.dp, PolishBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("panda_card_${panda.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Avatar, Names, Verification Badge, Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar / Photo
                Box(modifier = Modifier.size(68.dp)) {
                    if (!panda.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = panda.avatarUrl,
                            contentDescription = panda.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                    } else {
                        Surface(
                            modifier = Modifier.size(68.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = PolishPrimaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "🕉️", fontSize = 28.sp)
                            }
                        }
                    }

                    // Online Available Status Dot
                    Surface(
                        shape = CircleShape,
                        color = PolishGreen,
                        border = BorderStroke(2.dp, Color.White),
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.BottomEnd)
                    ) {}
                }

                // Name & Heritage Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = panda.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        // Rating Star Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFF8E1),
                            border = BorderStroke(1.dp, Color(0xFFFFD54F))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFF57F17),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = String.format(Locale.US, "%.1f", panda.rating),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF57F17)
                                )
                            }
                        }
                    }

                    // Hindi Name
                    Text(
                        text = panda.hindiName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = PolishPrimary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Title & Experience
                    Text(
                        text = "${panda.yearsOfExperience} yrs exp • ${panda.clanLineage}",
                        fontSize = 11.sp,
                        color = PolishTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Ghat Location
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = PolishTeal,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = panda.ghatLocation,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PolishTeal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Verification ID Badge & Bahi-Khata Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Accreditation Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PolishGreenBg,
                    border = BorderStroke(1.dp, PolishGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.clickable { onCopyAccreditation() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = PolishGreen,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = panda.accreditationId,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishGreen
                        )
                    }
                }

                // Bahi-Khata Ledger badge
                if (panda.bahiKhataAvailable) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PolishPurpleBadge
                    ) {
                        Text(
                            text = "📜 Ancestry Ledger Records",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishPurpleText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Spoken Languages Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Languages",
                    tint = PolishTextTertiary,
                    modifier = Modifier.size(12.dp)
                )
                panda.languages.forEach { lang ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PolishSurface,
                        border = BorderStroke(1.dp, PolishBorderDarker)
                    ) {
                        Text(
                            text = lang,
                            fontSize = 9.sp,
                            color = PolishTextSecondary,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Bio Summary
            Text(
                text = panda.bio,
                fontSize = 11.sp,
                color = PolishTextSecondary,
                maxLines = if (isExpanded) 10 else 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 15.sp,
                modifier = Modifier.padding(top = 6.dp)
            )

            // Primary Services Preview
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PolishSurface,
                border = BorderStroke(1.dp, PolishBorderDarker),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ritual Services & Dakshina Guidelines",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishTextPrimary
                        )
                        Text(
                            text = if (isExpanded) "Hide Details ▲" else "View All (${panda.services.size}) ▼",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishPrimary,
                            modifier = Modifier.clickable { isExpanded = !isExpanded }
                        )
                    }

                    val servicesToShow = if (isExpanded) panda.services else panda.services.take(2)
                    servicesToShow.forEach { service ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = service.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PolishTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${service.durationMinutes} mins • ${if (service.samagriIncluded) "Samagri Included" else "Samagri by Pilgrim"}",
                                    fontSize = 9.sp,
                                    color = PolishTextSecondary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PolishPrimaryContainer
                            ) {
                                Text(
                                    text = service.dakshinaGuide,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PolishPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Call, WhatsApp, Book Ritual
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Direct Phone Call Button
                Button(
                    onClick = onCall,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("call_panda_button_${panda.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Call", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // WhatsApp Direct Chat Button
                Button(
                    onClick = onWhatsApp,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("whatsapp_panda_button_${panda.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "WhatsApp",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Book / Inquire Ritual Button
                Button(
                    onClick = { onBookRitual(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("book_ritual_button_${panda.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = "Book",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Book Ritual", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PandaDetailsDialog(
    panda: VerifiedPanda,
    onDismiss: () -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onBookService: (PandaServiceOffering) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = panda.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = PolishTextPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = PolishTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = "${panda.hindiName} • ${panda.title}",
                    fontSize = 12.sp,
                    color = PolishPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Accreditation & Authority
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PolishGreenBg,
                    border = BorderStroke(1.dp, PolishGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = PolishGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Accreditation: ${panda.accreditationId}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = PolishGreen
                            )
                        }
                        Text(
                            text = panda.verifiedAuthority,
                            fontSize = 10.sp,
                            color = PolishTextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // Ancestry & Bahi-Khata Info
                if (panda.bahiKhataAvailable) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PolishPurpleBadge,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "📜 Bahi-Khata Ancestral Ledger Registry",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = PolishPurpleText
                            )
                            Text(
                                text = "Maintains family genealogy records for pilgrim lineages from: ${panda.bahiKhataRegions.joinToString(", ")}",
                                fontSize = 10.sp,
                                color = PolishTextPrimary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                // Bio
                Text(
                    text = panda.bio,
                    fontSize = 12.sp,
                    color = PolishTextSecondary,
                    lineHeight = 16.sp
                )

                Divider(color = PolishBorder)

                // All Services with direct booking action
                Text(
                    text = "Rituals & Dakshina Guidelines",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = PolishTextPrimary
                )

                panda.services.forEach { service ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PolishSurface,
                        border = BorderStroke(1.dp, PolishBorderDarker),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = service.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PolishTextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = service.dakshinaGuide,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PolishPrimary
                                )
                            }
                            Text(
                                text = service.description,
                                fontSize = 10.sp,
                                color = PolishTextSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⏳ ${service.durationMinutes} mins • ${if (service.samagriIncluded) "Samagri Included" else "Samagri by Pilgrim"}",
                                    fontSize = 9.sp,
                                    color = PolishTextTertiary
                                )
                                Button(
                                    onClick = { onBookService(service) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Select Ritual", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCall,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Call Purohit", fontSize = 11.sp)
                }
                Button(
                    onClick = onWhatsApp,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("WhatsApp", fontSize = 11.sp)
                }
            }
        },
        dismissButton = {}
    )
}

@Composable
fun PandaBookingDialog(
    panda: VerifiedPanda,
    selectedService: PandaServiceOffering?,
    onDismiss: () -> Unit,
    onConfirmBooking: (PandaServiceOffering, String, String, Int, String) -> Unit
) {
    var activeService by remember(selectedService) {
        mutableStateOf(selectedService ?: panda.services.first())
    }
    var selectedDateOption by remember { mutableStateOf("Today Morning (Auspicious)") }
    var selectedTimeSlot by remember { mutableStateOf("06:00 AM (Sunrise Sangam Snan)") }
    var devoteeCount by remember { mutableStateOf(2) }
    var notes by remember { mutableStateOf("") }

    val dateOptions = listOf(
        "Today Morning (Auspicious)",
        "Tomorrow (Brahma Muhurta)",
        "Next Amavasya / Purnima",
        "Upcoming Weekend"
    )

    val timeSlots = listOf(
        "06:00 AM (Sunrise Sangam Snan)",
        "08:30 AM (Morning Tarpan)",
        "11:30 AM (Madhyahna Puja)",
        "05:30 PM (Evening Sandhya Aarti)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Book Ritual with ${panda.name}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = PolishTextPrimary
                )
                Text(
                    text = "Certified Purohit • ${panda.ghatLocation}",
                    fontSize = 11.sp,
                    color = PolishPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Select Ritual Dropdown/List
                Text(
                    text = "Select Ritual / Sankalp",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = PolishTextPrimary
                )

                panda.services.forEach { service ->
                    val isSelected = activeService.id == service.id
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) PolishPrimaryContainer else PolishSurface,
                        border = BorderStroke(1.dp, if (isSelected) PolishPrimary else PolishBorderDarker),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeService = service }
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { activeService = service },
                                colors = RadioButtonDefaults.colors(selectedColor = PolishPrimary)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = service.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = PolishTextPrimary
                                )
                                Text(
                                    text = "${service.dakshinaGuide} • ${service.durationMinutes} mins",
                                    fontSize = 10.sp,
                                    color = PolishPrimary
                                )
                            }
                        }
                    }
                }

                Divider(color = PolishBorder)

                // Date Selection
                Text(
                    text = "Select Preferred Date",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = PolishTextPrimary
                )
                dateOptions.forEach { dateOpt ->
                    val isSelected = selectedDateOption == dateOpt
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) PolishPrimaryContainer else PolishSurface,
                        border = BorderStroke(1.dp, if (isSelected) PolishPrimary else PolishBorderDarker),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedDateOption = dateOpt }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedDateOption = dateOpt },
                                colors = RadioButtonDefaults.colors(selectedColor = PolishPrimary)
                            )
                            Text(text = dateOpt, fontSize = 11.sp, color = PolishTextPrimary)
                        }
                    }
                }

                // Time Slot Selection
                Text(
                    text = "Select Time Slot",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = PolishTextPrimary
                )
                timeSlots.forEach { slot ->
                    val isSelected = selectedTimeSlot == slot
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) PolishPrimaryContainer else PolishSurface,
                        border = BorderStroke(1.dp, if (isSelected) PolishPrimary else PolishBorderDarker),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTimeSlot = slot }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedTimeSlot = slot },
                                colors = RadioButtonDefaults.colors(selectedColor = PolishPrimary)
                            )
                            Text(text = slot, fontSize = 11.sp, color = PolishTextPrimary)
                        }
                    }
                }

                // Devotee Count Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Number of Devotees / Family Members",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishTextPrimary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (devoteeCount > 1) devoteeCount-- },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Text("-", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "$devoteeCount",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        IconButton(
                            onClick = { if (devoteeCount < 20) devoteeCount++ },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Gotra / Ancestral Notes Input
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Gotra / Ancestral Notes (Optional)", fontSize = 11.sp) },
                    placeholder = { Text("e.g. Kashyap Gotra, Grandfather tarpan", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PolishPrimary,
                        unfocusedBorderColor = PolishBorder
                    ),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirmBooking(
                        activeService,
                        selectedDateOption,
                        selectedTimeSlot,
                        devoteeCount,
                        notes
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("confirm_ritual_booking_button")
            ) {
                Text("Confirm & Reserve Slot", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel", color = PolishTextSecondary)
            }
        }
    )
}

@Composable
fun BookingSuccessDialog(
    bookingCode: String,
    onDismiss: () -> Unit,
    onViewSchedules: () -> Unit = onDismiss
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            LottieSuccessCelebration(
                sizeDp = 100,
                modifier = Modifier.testTag("dialog_lottie_success_animation")
            )
        },
        title = {
            Text(
                text = "Ritual Reserved Successfully!",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = PolishTextPrimary,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Your booking request has been registered with the verified Tirtha Purohit and saved to your local Spiritual Schedules (Room DB).",
                    fontSize = 12.sp,
                    color = PolishTextSecondary,
                    textAlign = TextAlign.Center
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PolishPrimaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "BOOKING REFERENCE CODE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishOnPrimaryContainer
                        )
                        Text(
                            text = bookingCode,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PolishPrimary
                        )
                    }
                }
                Text(
                    text = "• Saved to Room Database for instant offline viewing at the Ghats.\n• You will receive SMS & WhatsApp confirmations with exact coordinates.",
                    fontSize = 11.sp,
                    color = PolishTextSecondary,
                    lineHeight = 15.sp
                )
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = onViewSchedules,
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View in Spiritual Schedules →", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = PolishTextPrimary)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PandaFilterBottomSheet(
    availableLanguages: List<String>,
    selectedLanguage: String,
    onSelectLanguage: (String) -> Unit,
    sortBy: String,
    onSelectSortBy: (String) -> Unit,
    bahiKhataOnly: Boolean,
    onToggleBahiKhata: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onReset: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = PolishBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter & Sort Purohits",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PolishTextPrimary
                )
                TextButton(onClick = onReset) {
                    Text("Reset All", color = PolishRed)
                }
            }

            // Sort By
            Text(
                text = "Sort By",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = PolishTextPrimary
            )
            val sortOptions = listOf(
                "rating" to "Highest Rated ★",
                "experience" to "Most Experienced",
                "reviews" to "Most Pilgrim Reviews",
                "name" to "Alphabetical (A-Z)"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sortOptions.forEach { (key, label) ->
                    val isSelected = sortBy == key
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) PolishPrimary else PolishCardSurface,
                        border = BorderStroke(1.dp, if (isSelected) PolishPrimary else PolishBorder),
                        modifier = Modifier.clickable { onSelectSortBy(key) }
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else PolishTextPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Language Filter
            Text(
                text = "Language Preference",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = PolishTextPrimary
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                availableLanguages.forEach { lang ->
                    val isSelected = selectedLanguage.equals(lang, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) PolishPrimaryContainer else PolishCardSurface,
                        border = BorderStroke(1.dp, if (isSelected) PolishPrimary else PolishBorder),
                        modifier = Modifier.clickable { onSelectLanguage(lang) }
                    ) {
                        Text(
                            text = lang,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) PolishPrimary else PolishTextPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Ancestry Ledger Filter
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (bahiKhataOnly) PolishPrimaryContainer else PolishCardSurface,
                border = BorderStroke(1.dp, if (bahiKhataOnly) PolishPrimary else PolishBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleBahiKhata(!bahiKhataOnly) }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "📜 Bahi-Khata Ancestry Ledger Maintainers Only",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishTextPrimary
                        )
                        Text(
                            text = "Filter Purohits who preserve multi-generational family trees and signatures of your ancestors.",
                            fontSize = 10.sp,
                            color = PolishTextSecondary
                        )
                    }
                    RadioButton(
                        selected = bahiKhataOnly,
                        onClick = { onToggleBahiKhata(!bahiKhataOnly) },
                        colors = RadioButtonDefaults.colors(selectedColor = PolishPrimary)
                    )
                }
            }

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Apply Filters & View Purohits", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun EmptyPandasState(
    onClearFilters: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🕉️", fontSize = 42.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No Purohits found matching criteria.",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = PolishTextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Try broadening your search query or reset ritual & ghat filters.",
                fontSize = 12.sp,
                color = PolishTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onClearFilters,
                colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Reset All Filters")
            }
        }
    }
}

fun launchPhoneDialer(context: Context, phoneNumber: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${phoneNumber.replace(" ", "")}")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot launch dialer for $phoneNumber", Toast.LENGTH_SHORT).show()
    }
}

fun launchWhatsAppChat(context: Context, whatsappNumber: String, pandaName: String) {
    try {
        val cleanNumber = whatsappNumber.replace("+", "").replace(" ", "")
        val message = "Pranam $pandaName Ji, I am visiting Prayagraj and would like to inquire about ritual services."
        val url = "https://api.whatsapp.com/send?phone=$cleanNumber&text=${Uri.encode(message)}"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot open WhatsApp for $whatsappNumber", Toast.LENGTH_SHORT).show()
    }
}
