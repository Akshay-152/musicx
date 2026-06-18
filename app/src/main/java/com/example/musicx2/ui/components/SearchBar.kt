package com.example.musicx2.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.musicx2.ui.theme.OnSecondary
import com.example.musicx2.ui.theme.Surface

@Composable
fun MusicSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        placeholder = {
            Text(
                text = "Search tracks, artists...",
                color = OnSecondary.copy(alpha = 0.5f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "Search",
                tint = OnSecondary.copy(alpha = 0.5f)
            )
        },
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Surface.copy(alpha = 0.8f),
            unfocusedContainerColor = Surface.copy(alpha = 0.5f),
            disabledContainerColor = Surface.copy(alpha = 0.5f),
            cursorColor = OnSecondary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = OnSecondary,
            unfocusedTextColor = OnSecondary
        ),
        singleLine = true
    )
}
