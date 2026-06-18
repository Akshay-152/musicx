package com.example.musicx2.ui.components

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.example.musicx2.ui.components.GlassmorphicCard
import com.example.musicx2.ui.theme.*

@Composable
fun UploadDialog(
    onDismiss: () -> Unit,
    onUploadFile: (String, String, android.net.Uri, android.net.Uri?) -> Unit,
    isUploading: Boolean
) {
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var audioUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var coverUri by remember { mutableStateOf<android.net.Uri?>(null) }
    
    val context = LocalContext.current
    
    fun getFileName(uri: android.net.Uri?): String? {
        if (uri == null) return null
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        }
    }

    val audioLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { audioUri = it }
    val coverLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { coverUri = it }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        GlassmorphicCard(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            contentPadding = PaddingValues(24.dp)
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Upload Track",
                    color = OnSecondary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Track Title", color = OnSecondary.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = OnSecondary.copy(alpha = 0.2f),
                            focusedTextColor = OnSecondary,
                            unfocusedTextColor = OnSecondary
                        )
                    )
                    OutlinedTextField(
                        value = artist,
                        onValueChange = { artist = it },
                        label = { Text("Artist Name", color = OnSecondary.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = OnSecondary.copy(alpha = 0.2f),
                            focusedTextColor = OnSecondary,
                            unfocusedTextColor = OnSecondary
                        )
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SelectionButton(
                            text = getFileName(audioUri) ?: "Select Audio",
                            icon = Icons.Rounded.MusicNote,
                            isSelected = audioUri != null,
                            onClick = { audioLauncher.launch("audio/*") },
                            modifier = Modifier.weight(1f)
                        )
                        SelectionButton(
                            text = getFileName(coverUri) ?: "Select Cover",
                            icon = Icons.Rounded.Image,
                            isSelected = coverUri != null,
                            onClick = { coverLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    if (coverUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(coverUri),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .align(Alignment.CenterHorizontally),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Button(
                    onClick = {
                        if (title.isNotBlank() && artist.isNotBlank() && audioUri != null) {
                            onUploadFile(title, artist, audioUri!!, coverUri)
                            // Clear inputs
                            title = ""
                            artist = ""
                            audioUri = null
                            coverUri = null
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isUploading
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                    } else {
                        Icon(Icons.Rounded.CloudUpload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Publish Track", fontWeight = FontWeight.Bold)
                    }
                }

                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = OnSecondary.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
fun TabItem(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Primary else Color.Transparent,
        contentColor = if (isSelected) Color.Black else OnSecondary.copy(alpha = 0.6f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun SelectionButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isSelected) Primary else OnSecondary.copy(alpha = 0.2f)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (isSelected) Primary else OnSecondary.copy(alpha = 0.7f)
        )
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, fontSize = 11.sp, maxLines = 1)
    }
}
