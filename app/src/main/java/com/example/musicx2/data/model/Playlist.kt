package com.example.musicx2.data.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Playlist(
    @get:PropertyName("id") @set:PropertyName("id")
    var id: String = "",
    var name: String = "",
    var userId: String = "",
    var trackIds: List<String> = emptyList(),
    @ServerTimestamp var createdAt: Date? = null
)
