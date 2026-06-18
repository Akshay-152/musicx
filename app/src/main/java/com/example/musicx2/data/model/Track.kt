package com.example.musicx2.data.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Track(
    val id: String = "",
    val title: String = "",
    val artist: String = "Unknown Artist",
    val audioUrl: String = "",
    val coverUrl: String = "",
    val duration: Long = 0,
    val ownerId: String = "",
    @ServerTimestamp val createdAt: Date? = null,
    val isFavorite: Boolean = false
)
