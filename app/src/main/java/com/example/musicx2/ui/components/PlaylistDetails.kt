package com.example.musicx2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.RemoveCircleOutline
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.musicx2.data.model.Playlist
import com.example.musicx2.data.model.Track
import com.example.musicx2.ui.theme.Background
import com.example.musicx2.ui.theme.OnSecondary
import com.example.musicx2.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetails(
    playlist: Playlist,
    tracks: List<Track>,
    onBackClick: () -> Unit,
    onPlayClick: (Track) -> Unit,
    onRemoveTrack: (String) -> Unit,
    onMoveTrack: (Int, Int) -> Unit,
    onShuffleClick: () -> Unit,
    onRenamePlaylist: (String) -> Unit,
    onDeletePlaylist: () -> Unit,
    currentTrackId: String? = null,
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = Background
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                PlaylistHeader(
                    playlist = playlist,
                    trackCount = tracks.size,
                    onShuffleClick = onShuffleClick,
                    onBackClick = onBackClick,
                    onRenamePlaylist = onRenamePlaylist,
                    onDeletePlaylist = onDeletePlaylist
                )
            }

            items(tracks, key = { it.id }) { track ->
                val index = tracks.indexOfFirst { it.id == track.id }
                PlaylistTrackItem(
                    track = track,
                    canMoveUp = index > 0,
                    canMoveDown = index < tracks.size - 1,
                    onMoveUp = { onMoveTrack(index, index - 1) },
                    onMoveDown = { onMoveTrack(index, index + 1) },
                    onClick = { onPlayClick(track) },
                    onRemove = { onRemoveTrack(track.id) },
                    isCurrentlyPlaying = track.id == currentTrackId
                )
            }
            
            if (tracks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No tracks in this playlist",
                            color = OnSecondary.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistHeader(
    playlist: Playlist,
    trackCount: Int,
    onShuffleClick: () -> Unit,
    onBackClick: () -> Unit,
    onRenamePlaylist: (String) -> Unit,
    onDeletePlaylist: () -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember(playlist.name) { mutableStateOf(playlist.name) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = OnSecondary)
            }
            IconButton(
                onClick = onDeletePlaylist,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Rounded.Delete, "Delete Playlist", tint = Color.Red.copy(alpha = 0.7f))
            }
        }

        Spacer(Modifier.height(8.dp))

        // Placeholder or first track cover for playlist art
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(OnSecondary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.QueueMusic,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Primary.copy(alpha = 0.5f)
            )
        }

        Spacer(Modifier.height(24.dp))

        if (isEditing) {
            OutlinedTextField(
                value = editedName,
                onValueChange = { if (it.length <= 50) editedName = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    color = OnSecondary,
                    fontWeight = FontWeight.Bold
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = OnSecondary.copy(alpha = 0.3f),
                    cursorColor = Primary
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (editedName.isNotBlank()) {
                            onRenamePlaylist(editedName)
                            isEditing = false
                            focusManager.clearFocus()
                        }
                    }
                ),
                trailingIcon = {
                    IconButton(onClick = {
                        if (editedName.isNotBlank()) {
                            onRenamePlaylist(editedName)
                            isEditing = false
                            focusManager.clearFocus()
                        }
                    }) {
                        Icon(Icons.Rounded.Check, "Save", tint = Primary)
                    }
                }
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { isEditing = true }
            ) {
                Text(
                    text = playlist.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = OnSecondary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Rounded.Edit,
                    contentDescription = "Rename",
                    tint = OnSecondary.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Text(
            text = "$trackCount tracks",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSecondary.copy(alpha = 0.6f)
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onShuffleClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Rounded.Shuffle, null)
            Spacer(Modifier.width(8.dp))
            Text("Shuffle Play", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PlaylistTrackItem(
    track: Track,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    isCurrentlyPlaying: Boolean = false
) {
    val backgroundColor = if (isCurrentlyPlaying) {
        Color(0xFFA5D6A7).copy(alpha = 0.2f)
    } else {
        OnSecondary.copy(alpha = 0.05f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = track.coverUrl,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                track.title,
                color = OnSecondary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                track.artist,
                color = OnSecondary.copy(alpha = 0.6f),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row {
            IconButton(
                onClick = onMoveUp,
                enabled = canMoveUp,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Rounded.KeyboardArrowUp,
                    contentDescription = "Move Up",
                    tint = if (canMoveUp) OnSecondary else OnSecondary.copy(alpha = 0.2f)
                )
            }
            IconButton(
                onClick = onMoveDown,
                enabled = canMoveDown,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Move Down",
                    tint = if (canMoveDown) OnSecondary else OnSecondary.copy(alpha = 0.2f)
                )
            }
        }

        IconButton(onClick = onRemove) {
            Icon(
                Icons.Rounded.RemoveCircleOutline,
                contentDescription = "Remove",
                tint = Color.Red.copy(alpha = 0.7f)
            )
        }
    }
}
