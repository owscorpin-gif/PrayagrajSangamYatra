package com.prayagraj.app.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.prayagraj.app.data.repository.VendorStorageRepository
import kotlinx.coroutines.launch

@Composable
fun VendorPhotoUploader(
    category: String, // "accommodations" or "vehicles"
    currentPhotoUrl: String?,
    onPhotoUploaded: (String) -> Unit,
    onPhotoDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    storageRepository: VendorStorageRepository = remember { VendorStorageRepository() }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            scope.launch {
                isLoading = true
                errorMessage = null
                runCatching {
                    val inputStream = context.contentResolver.openInputStream(selectedUri)
                    val bytes = inputStream?.use { it.readBytes() }
                        ?: throw IllegalStateException("Could not read image file.")

                    storageRepository.uploadVendorPhoto(bytes, category)
                }.onSuccess { result ->
                    result.fold(
                        onSuccess = { publicUrl ->
                            onPhotoUploaded(publicUrl)
                        },
                        onFailure = { error ->
                            errorMessage = error.localizedMessage
                        }
                    )
                }.onFailure { error ->
                    errorMessage = error.localizedMessage
                }
                isLoading = false
            }
        }
    }

    Column(modifier = modifier) {
        if (!currentPhotoUrl.isNull_or_blank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = currentPhotoUrl,
                    contentDescription = "Uploaded Vendor Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                IconButton(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            storageRepository.deleteVendorPhoto(currentPhotoUrl ?: "")
                            onPhotoDeleted()
                            isLoading = false
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Photo",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        } else {
            Card(
                onClick = { photoPickerLauncher.launch("image/*") },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Add Photo",
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Upload ${category.replaceFirstChar { it.uppercase() }} Photo",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private fun String?.isNull_or_blank(): Boolean = this == null || this.isBlank()
