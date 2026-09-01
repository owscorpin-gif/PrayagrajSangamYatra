package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AiGuideMessage
import com.example.data.model.MessageSender
import com.example.data.model.STANDARD_DAKSHINA_TARIFF
import com.example.ui.theme.*
import com.example.ui.viewmodel.AiGuideViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiGuideScreen(
    onBack: () -> Unit,
    onNavigateToPurohits: () -> Unit,
    viewModel: AiGuideViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Auto-scroll when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = PolishBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Prayag AI Guide",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PolishTextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PolishPrimaryContainer
                            ) {
                                Text(
                                    text = "Gemini 3.5 Flash",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PolishPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Transparent Dakshina & Anti-Exploitation Advisor",
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
                actions = {
                    IconButton(onClick = { viewModel.toggleTariffSheet(true) }) {
                        Icon(
                            imageVector = Icons.Default.CurrencyRupee,
                            contentDescription = "Official Tariff",
                            tint = PolishPrimary
                        )
                    }
                    IconButton(onClick = { viewModel.clearChat() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Clear Chat",
                            tint = PolishTextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PolishSurface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(PolishBackground)
        ) {
            // Anti-Exploitation Safety Banner
            Surface(
                color = Saffron50,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Saffron200)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🛡️", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dakshina is voluntary. Never pay exorbitant fees under pressure. Ask for PRY- Accreditation.",
                        fontSize = 11.sp,
                        color = Saffron900,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { viewModel.toggleTariffSheet(true) },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("View Rates", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PolishPrimary)
                    }
                }
            }

            // Chat Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    ChatMessageItem(
                        message = message,
                        onNavigateToPurohits = onNavigateToPurohits,
                        onCallHelpline = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))
                            context.startActivity(intent)
                        }
                    )
                }

                if (uiState.isLoading) {
                    item {
                        TypingIndicator()
                    }
                }
            }

            // Quick Suggestion Prompt Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                items(uiState.quickPrompts) { prompt ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PolishSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorderDarker),
                        modifier = Modifier.clickable {
                            viewModel.sendMessage(prompt)
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "💬",
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = prompt,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = PolishTextPrimary
                            )
                        }
                    }
                }
            }

            // Input Row
            Surface(
                color = PolishSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorderDarker),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.inputText,
                        onValueChange = { viewModel.onInputTextChanged(it) },
                        placeholder = {
                            Text("Ask about rituals, fair Dakshina, or pandas...", fontSize = 13.sp, color = PolishTextTertiary)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 46.dp, max = 100.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PolishPrimary,
                            unfocusedBorderColor = PolishBorderDarker,
                            focusedContainerColor = PolishBackground,
                            unfocusedContainerColor = PolishBackground
                        ),
                        singleLine = false,
                        maxLines = 3,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                viewModel.sendMessage()
                            }
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FilledIconButton(
                        onClick = { viewModel.sendMessage() },
                        enabled = uiState.inputText.isNotBlank() && !uiState.isLoading,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = PolishPrimary,
                            contentColor = Color.White,
                            disabledContainerColor = PolishBorderDarker,
                            disabledContentColor = PolishTextTertiary
                        ),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    // Official Dakshina Tariff Sheet
    if (uiState.showTariffSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.toggleTariffSheet(false) },
            containerColor = PolishSurface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Standard Community Dakshina Tariff",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishTextPrimary
                        )
                        Text(
                            text = "Approved reference ranges for Prayagraj Tirth Rituals",
                            fontSize = 12.sp,
                            color = PolishTextSecondary
                        )
                    }
                    IconButton(onClick = { viewModel.toggleTariffSheet(false) }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = PolishTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(STANDARD_DAKSHINA_TARIFF) { item ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PolishBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorderDarker),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.ritualName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = PolishTextPrimary
                                        )
                                        Text(
                                            text = item.hindiName,
                                            fontSize = 12.sp,
                                            color = PolishTextSecondary
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Emerald50,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Emerald200)
                                    ) {
                                        Text(
                                            text = "₹${item.standardMinFee} – ₹${item.standardMaxFee}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Emerald700,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = item.description,
                                    fontSize = 12.sp,
                                    color = PolishTextSecondary,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "💡 ", fontSize = 11.sp)
                                    Text(
                                        text = item.warnings,
                                        fontSize = 11.sp,
                                        color = Saffron700,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.toggleTariffSheet(false)
                        onNavigateToPurohits()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PolishPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Browse Verified Purohits Directory", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: AiGuideMessage,
    onNavigateToPurohits: () -> Unit,
    onCallHelpline: () -> Unit
) {
    val isUser = message.sender == MessageSender.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Surface(
                shape = CircleShape,
                color = PolishPrimaryContainer,
                modifier = Modifier
                    .size(34.dp)
                    .padding(top = 2.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🤖", fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 310.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = if (isUser) PolishPrimary else PolishSurface,
                border = if (!isUser) androidx.compose.foundation.BorderStroke(1.dp, PolishBorderDarker) else null,
                shadowElevation = if (!isUser) 1.dp else 0.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Header tag for AI message if ritual detected
                    if (!isUser && message.fairDakshinaRange != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Emerald50,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Emerald200),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text("💰 Fair Dakshina Range: ", fontSize = 11.sp, color = Emerald700)
                                Text(
                                    text = message.fairDakshinaRange,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald700
                                )
                            }
                        }
                    }

                    Text(
                        text = message.text,
                        fontSize = 13.sp,
                        color = if (isUser) Color.White else PolishTextPrimary,
                        lineHeight = 18.sp
                    )

                    // Actionable buttons inside AI response
                    if (!isUser && (message.showVerifiedPurohitAction || message.showHelplineAction)) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (message.showVerifiedPurohitAction) {
                                OutlinedButton(
                                    onClick = onNavigateToPurohits,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = PolishPrimary
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, PolishPrimary),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Verified Pandas", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (message.showHelplineAction) {
                                OutlinedButton(
                                    onClick = onCallHelpline,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFFD32F2F)
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD32F2F)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Police 112", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = message.timestamp,
                fontSize = 10.sp,
                color = PolishTextTertiary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = CircleShape,
                color = PolishPrimary.copy(alpha = 0.2f),
                modifier = Modifier
                    .size(34.dp)
                    .padding(top = 2.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("👤", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 42.dp, top = 4.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = PolishSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, PolishBorderDarker)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = PolishPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Gemini is analyzing ritual guidance...",
                    fontSize = 11.sp,
                    color = PolishTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
