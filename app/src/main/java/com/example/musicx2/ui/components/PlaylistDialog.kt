package com.example.musicx2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.musicx2.data.model.Playlist
import com.example.musicx2.ui.theme.OnSecondary
import com.example.musicx2.ui.theme.Primary
import com.example.musicx2.ui.theme.Surface

@Composable
fun PlaylistDialog(
    playlists: List<Playlist>,
    onPlaylistSelected: (String) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newPlaylistName by remember { mutableStateOf("") }
    var showCreateNew by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Surface)
                .padding(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add to Playlist",
                    color = OnSecondary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                if (showCreateNew) {
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        label = { Text("Playlist Name", color = OnSecondary.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = OnSecondary.copy(alpha = 0.2f),
                            focusedTextColor = OnSecondary,
                            unfocusedTextColor = OnSecondary
                        ),
                        singleLine = true
                    )
                    Button(
                        onClick = { 
                            if (newPlaylistName.isNotBlank()) {
                                onCreatePlaylist(newPlaylistName)
                                showCreateNew = false
                                newPlaylistName = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Create", fontWeight = FontWeight.Bold)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(playlists) { playlist ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(OnSecondary.copy(alpha = 0.05f))
                                    .clickable { onPlaylistSelected(playlist.id) }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.List, contentDescription = null, tint = OnSecondary.copy(alpha = 0.6f))
                                Text(text = playlist.name, color = OnSecondary)
                            }
                        }
                    }

                    Button(
                        onClick = { showCreateNew = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OnSecondary.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = null, tint = OnSecondary)
                        Spacer(Modifier.width(8.dp))
                        Text("Create New Playlist", color = OnSecondary, fontWeight = FontWeight.Bold)
                    }
                }

                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = OnSecondary.copy(alpha = 0.6f))
                }
            }
        }
    }
}
