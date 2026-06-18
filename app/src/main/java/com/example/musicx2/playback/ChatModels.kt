package com.example.musicx2.playback

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val content: String = "",
    @ServerTimestamp
    val timestamp: Timestamp? = null,
    val type: String = "text", // "text", "reply", "system"
    val replyTo: ReplyMetadata? = null,
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false
)

data class ReplyMetadata(
    val id: String = "",
    val content: String = "",
    val senderId: String = ""
)
