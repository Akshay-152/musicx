package com.example.musicx2.playback

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomSyncManager @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val roomsCollection = firestore.collection("rooms")

    fun observeRoom(roomId: String): Flow<RoomSync?> = callbackFlow {
        val registration = roomsCollection.document(roomId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(RoomSync::class.java))
            }
        awaitClose { registration.remove() }
    }

    fun generateRoomId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_$uid2" else "${uid2}_$uid1"
    }

    suspend fun createRoom(userA: String, userB: String, initialTrack: TrackSync?): String {
        val roomId = generateRoomId(userA, userB)
        val room = RoomSync(
            roomId = roomId,
            connectedIds = mapOf(userA to true, userB to true),
            currentSong = initialTrack,
            active = true,
            masterId = userA,
            secondMasterId = userB,
            controllerId = userA // First user is the initial controller
        )
        roomsCollection.document(roomId).set(room).await()
        return roomId
    }

    suspend fun updateRoomState(
        roomId: String,
        track: TrackSync,
        isPlaying: Boolean,
        positionMs: Long,
        userId: String,
        serverTime: Long,
        actionId: String
    ) {
        firestore.runTransaction { transaction ->
            val roomRef = roomsCollection.document(roomId)
            val snapshot = transaction.get(roomRef)

            val currentController = snapshot.getString("controllerId")
            if (currentController != userId) return@runTransaction

            val currentRevision = snapshot.getLong("playbackState.revision") ?: 0L

            val updates = mapOf(
                "currentSong" to track,
                "playbackState.isPlaying" to isPlaying,
                "playbackState.positionMs" to positionMs,
                "playbackState.lastUpdated" to serverTime,
                "playbackState.updatedBy" to userId,
                "playbackState.revision" to currentRevision + 1,
                "playbackState.lastActionId" to actionId,
                "controllerId" to userId
            )
            transaction.update(roomRef, updates)
        }.await()
    }

    suspend fun renewLease(roomId: String, userId: String) {
        roomsCollection.document(roomId).update("controllerId", userId).await()
    }

    suspend fun updatePlaybackState(
        roomId: String,
        isPlaying: Boolean,
        positionMs: Long,
        userId: String,
        serverTime: Long
    ) {
        val updates = mapOf(
            "playbackState" to mapOf(
                "isPlaying" to isPlaying,
                "positionMs" to positionMs,
                "lastUpdated" to serverTime,
                "updatedBy" to userId
            )
        )
        roomsCollection.document(roomId).set(updates, SetOptions.merge()).await()
    }

    suspend fun updateCurrentTrack(roomId: String, track: TrackSync) {
        roomsCollection.document(roomId).update("currentSong", track).await()
    }

    suspend fun leaveRoom(roomId: String) {
        try {
            val roomRef = roomsCollection.document(roomId)
            val doc = roomRef.get().await()
            if (!doc.exists()) return

            // Delete ephemeral chat messages first
            val messages = roomRef.collection("messages").get().await()
            if (!messages.isEmpty) {
                val batch = firestore.batch()
                messages.documents.forEach { batch.delete(it.reference) }
                batch.commit().await()
            }

            // Delete the room document entirely to cleanup
            roomRef.delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
            // Document might already be deleted or other Firestore error, ignore for leaveRoom
        }
    }

    // --- Chat Methods ---

    fun observeMessages(roomId: String): Flow<List<ChatMessage>> = callbackFlow {
        val registration = roomsCollection.document(roomId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull {
                    it.toObject(ChatMessage::class.java)?.copy(id = it.id)
                }
                trySend(messages ?: emptyList())
            }
        awaitClose { registration.remove() }
    }

    suspend fun sendMessage(roomId: String, message: ChatMessage) {
        roomsCollection.document(roomId).collection("messages").add(message).await()
    }

    suspend fun editMessage(roomId: String, messageId: String, newContent: String) {
        roomsCollection.document(roomId).collection("messages").document(messageId)
            .update("content", newContent, "isEdited", true).await()
    }

    suspend fun deleteMessage(roomId: String, messageId: String) {
        roomsCollection.document(roomId).collection("messages").document(messageId)
            .update("content", "This message was deleted.", "isDeleted", true).await()
    }

    // --- Controller Polling ---

    suspend fun requestControl(roomId: String, userId: String, serverTime: Long) {
        val request = ControllerRequest(userId, serverTime)
        roomsCollection.document(roomId).update("controllerRequest", request).await()
    }

    suspend fun respondToControlRequest(
        roomId: String,
        approve: Boolean,
        newControllerId: String? = null
    ) {
        val updates = mutableMapOf<String, Any?>("controllerRequest" to null)
        if (approve && newControllerId != null) {
            updates["controllerId"] = newControllerId
        }
        roomsCollection.document(roomId).update(updates).await()
    }
}
