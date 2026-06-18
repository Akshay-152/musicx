package com.example.musicx2.playback

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val presenceCollection = firestore.collection("presence")
    private val requestsCollection = firestore.collection("sync_requests")

    fun getServerTime(): Long = System.currentTimeMillis()

    val isConnected: Flow<Boolean> = callbackFlow {
        // Monitoring Firestore online state by listening to a dummy doc
        // Note: In a real app, you might use a more reliable way to check Firestore connectivity
        val registration = firestore.collection("metadata").document("status")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(false)
                } else {
                    trySend(true)
                }
            }
        awaitClose { registration.remove() }
    }

    fun observePresence(): Flow<Map<String, Boolean>> = callbackFlow {
        val registration = presenceCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val presence = mutableMapOf<String, Boolean>()
                val now = System.currentTimeMillis()
                snapshot?.documents?.forEach { doc ->
                    val userId = doc.id
                    val lastSeen = doc.getTimestamp("lastSeen")?.toDate()?.time ?: 0L
                    val isOnline = doc.getBoolean("status") ?: false
                    // Online if status is true and last seen within 2 minutes
                    if (isOnline && (now - lastSeen) < 120000) {
                        presence[userId] = true
                    }
                }
                trySend(presence)
            }
        awaitClose { registration.remove() }
    }

    fun observeRequests(userId: String): Flow<SyncRequest?> = callbackFlow {
        val registration = requestsCollection
            .whereEqualTo("to", userId)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val request = snapshot?.documents?.firstOrNull()?.toObject(SyncRequest::class.java)
                trySend(request)
            }
        awaitClose { registration.remove() }
    }

    suspend fun setUserOnline(userId: String) {
        val data = mapOf(
            "status" to true,
            "lastSeen" to FieldValue.serverTimestamp()
        )
        presenceCollection.document(userId).set(data).await()
    }

    suspend fun setUserOffline(userId: String) {
        val data = mapOf(
            "status" to false,
            "lastSeen" to FieldValue.serverTimestamp()
        )
        presenceCollection.document(userId).update(data).await()
    }

    suspend fun updateHeartbeat(userId: String) {
        presenceCollection.document(userId).update("lastSeen", FieldValue.serverTimestamp()).await()
    }

    suspend fun sendSyncRequest(fromUserId: String, toUserId: String): String {
        val requestId = "${fromUserId}_${toUserId}"
        val request = SyncRequest(
            id = requestId,
            from = fromUserId,
            to = toUserId,
            status = "pending",
            timestamp = System.currentTimeMillis()
        )
        requestsCollection.document(requestId).set(request).await()
        return requestId
    }

    suspend fun respondToRequest(userId: String, fromUserId: String, accept: Boolean) {
        val requestId = "${fromUserId}_${userId}"
        val status = if (accept) "accepted" else "declined"
        requestsCollection.document(requestId).update("status", status).await()
    }

    fun observeRequestStatus(targetUserId: String, myUserId: String): Flow<SyncRequest?> =
        callbackFlow {
            val requestId = "${myUserId}_${targetUserId}"
            val registration = requestsCollection.document(requestId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(null)
                        return@addSnapshotListener
                    }
                    trySend(snapshot?.toObject(SyncRequest::class.java))
                }
            awaitClose { registration.remove() }
        }

    suspend fun clearRequest(targetUserId: String, myUserId: String) {
        val requestId = "${myUserId}_${targetUserId}"
        requestsCollection.document(requestId).delete().await()
    }
}
