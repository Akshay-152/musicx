package com.example.musicx2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.musicx2.ui.theme.Primary
import com.example.musicx2.ui.theme.OnSecondary
import com.example.musicx2.ui.theme.DarkSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginDialog(
    onDismiss: () -> Unit,
    onLogin: (String) -> Unit,
    onGenerateId: () -> String
) {
    var customId by remember { mutableStateOf("") }

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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Icon(
                    Icons.Rounded.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Primary
                )

                Text(
                    "Studio Access",
                    style = MaterialTheme.typography.headlineSmall,
                    color = OnSecondary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "Enter your custom ID to sync your library across devices.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSecondary.copy(alpha = 0.7f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                OutlinedTextField(
                    value = customId,
                    onValueChange = { customId = it },
                    label = { Text("Custom User ID") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Primary,
                        unfocusedIndicatorColor = OnSecondary.copy(alpha = 0.3f),
                        focusedLabelColor = Primary,
                        unfocusedLabelColor = OnSecondary.copy(alpha = 0.5f),
                        cursorColor = Primary
                    ),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { customId = onGenerateId() }) {
                            Icon(Icons.Rounded.Casino, contentDescription = "Generate ID", tint = Primary)
                        }
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OnSecondary.copy(alpha = 0.2f))
                    ) {
                        Text("Cancel", color = OnSecondary)
                    }

                    Button(
                        onClick = {
                            if (customId.isNotBlank()) {
                                onLogin(customId)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("Sign In", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
