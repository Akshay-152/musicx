package com.example.musicx2.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicx2.ui.theme.OnSecondary
import com.example.musicx2.ui.theme.Primary

import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder

@Composable
fun LibraryHeader(
    trackCount: Int,
    onShuffleClick: () -> Unit,
    onSortClick: () -> Unit,
    showFavoritesOnly: Boolean = false,
    onToggleFavorites: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = if (showFavoritesOnly) "Favorites" else "Your Library",
                color = OnSecondary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$trackCount Tracks",
                color = OnSecondary.copy(alpha = 0.6f),
                fontSize = 14.sp
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            // Favorites Toggle
            IconButton(onClick = onToggleFavorites) {
                Icon(
                    imageVector = if (showFavoritesOnly) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = "Filter Favorites",
                    tint = if (showFavoritesOnly) Primary else OnSecondary
                )
            }

            FilledTonalIconButton(
                onClick = onShuffleClick,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Primary.copy(alpha = 0.15f),
                    contentColor = Primary
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shuffle,
                    contentDescription = "Shuffle All"
                )
            }
            
            IconButton(onClick = onSortClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Sort,
                    contentDescription = "Sort Library",
                    tint = OnSecondary
                )
            }
        }
    }
}
