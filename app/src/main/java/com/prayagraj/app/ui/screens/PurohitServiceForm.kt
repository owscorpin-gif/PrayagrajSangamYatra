package com.prayagraj.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.R
import com.prayagraj.app.data.local.PurohitFormDraft
import com.prayagraj.app.ui.viewmodel.PurohitFormUiState
import com.prayagraj.app.ui.viewmodel.PurohitServiceViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PurohitServiceForm(
    viewModel: PurohitServiceViewModel = viewModel(),
    onSuccess: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Retrieve initial saved draft from local storage
    val initialDraft = remember { viewModel.getInitialDraft() }

    var poojaName by remember { mutableStateOf(initialDraft.poojaName) }
    var selectedCategory by remember { mutableStateOf(initialDraft.category) }
    var durationHours by remember { mutableStateOf(initialDraft.durationHours) }
    var baseDakshina by remember { mutableStateOf(initialDraft.baseDakshina) }
    var samagriIncluded by remember { mutableStateOf(initialDraft.samagriIncluded) }
    var samagriExtraCost by remember { mutableStateOf(initialDraft.samagriExtraCost) }
    var description by remember { mutableStateOf(initialDraft.description) }
    var showSuccessAnimation by remember { mutableStateOf(false) }

    val categories = listOf("Kumbh Snan Sankalp", "Rudrabhishek", "Hawan & Yajna", "Graha Shanti", "Pitri Dosh Puja")
    val availableLanguages = listOf("Hindi", "Sanskrit", "English", "Bengali", "Marathi", "Gujarati", "Telugu")
    val selectedLanguages = remember {
        mutableStateListOf<String>().apply {
            addAll(if (initialDraft.languages.isNotEmpty()) initialDraft.languages else listOf("Hindi", "Sanskrit"))
        }
    }

    // Auto-save form draft to local storage whenever priest modifies any field
    LaunchedEffect(
        poojaName,
        selectedCategory,
        durationHours,
        baseDakshina,
        samagriIncluded,
        samagriExtraCost,
        description,
        selectedLanguages.toList()
    ) {
        viewModel.saveDraft(
            poojaName = poojaName,
            category = selectedCategory,
            durationHours = durationHours,
            baseDakshina = baseDakshina,
            samagriIncluded = samagriIncluded,
            samagriExtraCost = samagriExtraCost,
            languages = selectedLanguages.toList(),
            description = description
        )
    }

    LaunchedEffect(uiState) {
        if (uiState is PurohitFormUiState.Success) {
            showSuccessAnimation = true
            // Allow user to view the complete Lottie success animation and feedback before dismissing
            delay(2200)
            showSuccessAnimation = false
            onSuccess()
            viewModel.resetState()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Pooja / Ritual Service", style = MaterialTheme.typography.titleLarge)
                }

                // If draft contains user content, provide an option to reset/clear draft
                if (poojaName.isNotBlank() || description.isNotBlank()) {
                    TextButton(
                        onClick = {
                            viewModel.clearDraft()
                            poojaName = ""
                            selectedCategory = "Kumbh Snan Sankalp"
                            durationHours = "2.0"
                            baseDakshina = "1100"
                            samagriIncluded = true
                            samagriExtraCost = "500"
                            description = ""
                            selectedLanguages.clear()
                            selectedLanguages.addAll(listOf("Hindi", "Sanskrit"))
                        }
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Draft", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear Draft", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Visual indicator informing priest that their progress is auto-saved
            if (initialDraft.isNotEmpty() && poojaName.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Restored unsubmitted draft progress",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            OutlinedTextField(
                value = poojaName,
                onValueChange = { poojaName = it },
                label = { Text("Pooja / Sankalp Title *") },
                placeholder = { Text("e.g. Special Sangam Snan Sankalp & Hawan") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Category", style = MaterialTheme.typography.labelLarge)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = durationHours,
                    onValueChange = { durationHours = it },
                    label = { Text("Duration (Hours) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = baseDakshina,
                    onValueChange = { baseDakshina = it },
                    label = { Text("Base Dakshina (₹) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            // Samagri Toggle & Options
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Pooja Samagri Included in Base Dakshina?")
                        Switch(
                            checked = samagriIncluded,
                            onCheckedChange = { samagriIncluded = it }
                        )
                    }

                    AnimatedVisibility(visible = !samagriIncluded) {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            OutlinedTextField(
                                value = samagriExtraCost,
                                onValueChange = { samagriExtraCost = it },
                                label = { Text("Extra Charge for Arranging Samagri (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Text("Languages Supported for Recitation", style = MaterialTheme.typography.labelLarge)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                availableLanguages.forEach { lang ->
                    val isSelected = selectedLanguages.contains(lang)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) selectedLanguages.remove(lang) else selectedLanguages.add(lang)
                        },
                        label = { Text(lang) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Pooja Procedure & Details (Optional)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState is PurohitFormUiState.Error) {
                Text(
                    text = (uiState as PurohitFormUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    viewModel.submitService(
                        poojaName = poojaName,
                        category = selectedCategory,
                        durationHours = durationHours.toDoubleOrNull() ?: 1.0,
                        baseDakshina = baseDakshina.toDoubleOrNull() ?: 0.0,
                        samagriIncluded = samagriIncluded,
                        samagriExtraCost = samagriExtraCost.toDoubleOrNull() ?: 0.0,
                        languages = selectedLanguages.toList(),
                        description = description
                    )
                },
                enabled = poojaName.isNotBlank() && uiState !is PurohitFormUiState.Submitting,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (uiState is PurohitFormUiState.Submitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Publish Pooja Service")
                }
            }
        }

        // Lottie Success Overlay Animation
        if (showSuccessAnimation) {
            PurohitSuccessLottieDialog(poojaTitle = poojaName)
        }
    }
}

/**
 * Full visual feedback dialog rendering Lottie success checkmark and confirmation message.
 */
@Composable
fun PurohitSuccessLottieDialog(
    poojaTitle: String,
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.success_animation))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1
    )

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (composition != null) {
                        LottieAnimation(
                            composition = composition,
                            progress = { progress },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Pooja Service Published!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "\"$poojaTitle\" has been officially listed for pilgrims.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
