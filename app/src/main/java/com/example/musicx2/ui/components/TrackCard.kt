package com.example.musicx2.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.musicx2.data.model.Track
import com.example.musicx2.ui.theme.Musicx2Theme
import com.example.musicx2.ui.theme.OnSecondary
import com.example.musicx2.ui.theme.Primary
import com.example.musicx2.ui.theme.Surface

@Composable
fun TrackCard(
    track: Track,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit,
    modifier: Modifier = Modifier,
    onPlaylistClick: () -> Unit = {},
    onDownloadClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    isCurrentlyPlaying: Boolean = false,
    isOwner: Boolean = false
) {
    var showMenu by remember { mutableStateOf(false) }

    val backgroundColor = if (isCurrentlyPlaying) {
        Color(0xFFA5D6A7).copy(alpha = 0.2f) // Pale green highlight
    } else {
        Color.Transparent
    }

    GlassmorphicCard(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        cornerRadius = 16.dp,
        onClick = onClick,
        containerColor = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Circular Artwork
            Image(
                painter = rememberAsyncImagePainter(track.coverUrl),
                contentDescription = "Cover Art for ${track.title}",
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            // Track Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = track.title.ifBlank { "Unknown Title" },
                    color = OnSecondary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist.ifBlank { "Unknown Artist" },
                    color = OnSecondary.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Favorite Button (Standalone)
            IconButton(
                onClick = { onFavoriteClick() },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Primary else OnSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(22.dp)
                )
            }

            // Options Menu
            Box {
                IconButton(
                    onClick = { showMenu = true }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Options",
                        tint = OnSecondary.copy(alpha = 0.7f)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Surface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Add to Playlist", color = OnSecondary) },
                        onClick = {
                            showMenu = false
                            onPlaylistClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                    contentDescription = null,
                                    tint = if (isFavorite) Primary else OnSecondary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(if (isFavorite) "Remove Favorite" else "Add to Favorites", color = OnSecondary)
                            }
                        },
                        onClick = {
                            showMenu = false
                            onFavoriteClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Download, null, tint = OnSecondary.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Download", color = OnSecondary)
                            }
                        },
                        onClick = {
                            showMenu = false
                            onDownloadClick()
                        }
                    )
                    HorizontalDivider(color = OnSecondary.copy(alpha = 0.1f))

                    if (isOwner) {
                        DropdownMenuItem(
                            text = { Text("Edit Track", color = OnSecondary) },
                            onClick = {
                                showMenu = false
                                onEditClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Track", color = Color.Red) },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            }
                        )
                    } else {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Rounded.Lock,
                                        null,
                                        tint = OnSecondary.copy(alpha = 0.3f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Not your upload",
                                        color = OnSecondary.copy(alpha = 0.3f),
                                        fontSize = 12.sp
                                    )
                                }
                            },
                            onClick = { showMenu = false },
                            enabled = false
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TrackCardPreview() {
    Musicx2Theme {
        Box(modifier = Modifier.padding(16.dp)) {
            TrackCard(
                track = Track(
                    id = "1",
                    title = "Starboy",
                    artist = "The Weeknd",
                    duration = 230000
                ),
                isFavorite = false,
                onFavoriteClick = { },
                onClick = { },
                onDoubleClick = { },
                onEditClick = { }
            )
        }
    }
}
