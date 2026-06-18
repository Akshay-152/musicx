package com.example.musicx2.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Reply
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.SyncDisabled
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.musicx2.playback.ChatMessage
import com.example.musicx2.playback.ConnectionState
import com.example.musicx2.playback.ReplyMetadata
import com.example.musicx2.ui.theme.OnSecondary
import com.example.musicx2.ui.theme.Primary
import com.example.musicx2.ui.viewmodel.MusicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncStudio(
    viewModel: MusicViewModel,
    onBack: () -> Unit
) {
    val syncInputId = remember { mutableStateOf("") }
    val isSyncing by viewModel.isSyncing.collectAsState()
    val isController by viewModel.isController.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val currentRoom by viewModel.currentRoom.collectAsState()
    val onlineUsers by viewModel.onlineUsers.collectAsState()
    val isFirebaseConnected by viewModel.isFirebaseConnected.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val showControlRequestModal by viewModel.showControlRequestModal.collectAsState()
    val messages by viewModel.messages.collectAsState()

    var showChat by remember { mutableStateOf(false) }

    if (showControlRequestModal != null) {
        AlertDialog(
            onDismissRequest = { viewModel.respondToControlRequest(false) },
            title = { Text("Request Control") },
            text = { Text("${showControlRequestModal?.uid} wants to control playback. Allow?") },
            confirmButton = {
                Button(onClick = { viewModel.respondToControlRequest(true) }) {
                    Text("Allow")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.respondToControlRequest(false) }) {
                    Text("Deny")
                }
            },
            containerColor = Color.Black,
            titleContentColor = OnSecondary,
            textContentColor = OnSecondary.copy(alpha = 0.8f)
        )
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Sync Studio", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (isSyncing) {
                            IconButton(onClick = { showChat = !showChat }) {
                                Icon(
                                    Icons.Rounded.Chat,
                                    contentDescription = "Chat",
                                    tint = if (showChat) Primary else OnSecondary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = OnSecondary,
                        navigationIconContentColor = OnSecondary
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(padding)) {
                if (showChat && isSyncing) {
                    ChatView(
                        modifier = Modifier.weight(1f),
                        messages = messages,
                        currentUserId = currentUserId ?: "",
                        onSendMessage = { content, replyTo ->
                            viewModel.sendMessage(
                                content,
                                replyTo
                            )
                        },
                        onDeleteMessage = { viewModel.deleteMessage(it) },
                        onEditMessage = { id, content -> viewModel.editMessage(id, content) }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // 🟢 Online Status Card
                        item {
                            val statusScale by animateFloatAsState(
                                targetValue = if (isFirebaseConnected) 1.2f else 1f,
                                animationSpec = tween(1000),
                                label = "statusScale"
                            )
                            GlassmorphicCard(
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = Color.White.copy(alpha = 0.1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .scale(statusScale)
                                            .background(
                                                if (isFirebaseConnected) Color.Green else Color.Red,
                                                CircleShape
                                            )
                                    )
                                    Spacer(Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            if (isFirebaseConnected) "System Online" else "System Offline",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = OnSecondary.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            currentUserId ?: "Connecting...",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    if (isSyncing) {
                                        val composition by rememberLottieComposition(
                                            LottieCompositionSpec.RawRes(com.example.musicx2.R.raw.sync_pulse)
                                        )
                                        LottieAnimation(
                                            composition = composition,
                                            iterations = LottieConstants.IterateForever,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    } else {
                                        Icon(
                                            Icons.Rounded.Group,
                                            contentDescription = null,
                                            tint = OnSecondary.copy(alpha = 0.3f)
                                        )
                                    }
                                }
                            }
                        }

                        // 📤 Sync Control Card
                        item {
                            SyncControlCard(
                                syncInputId = syncInputId.value,
                                onIdChange = { syncInputId.value = it },
                                isSyncing = isSyncing,
                                isController = isController,
                                connectionState = connectionState,
                                currentRoomId = currentRoom?.roomId,
                                onRequestSync = { viewModel.startSyncWithRoom(syncInputId.value) },
                                onRequestControl = { viewModel.requestControl() },
                                onStopSync = {
                                    if (connectionState == ConnectionState.CONNECTING) {
                                        viewModel.cancelConnecting()
                                    } else {
                                        viewModel.leaveRoom()
                                    }
                                }
                            )
                        }

                        // 👥 Online Peers List
                        if (onlineUsers.isNotEmpty()) {
                            val otherUsers = onlineUsers.filter { it.key != currentUserId }
                            if (otherUsers.isNotEmpty()) {
                                item {
                                    Text(
                                        "Available Peers",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = OnSecondary,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }
                                items(otherUsers.toList()) { (userId, isOnline) ->
                                    GlassmorphicCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = { syncInputId.value = userId },
                                        contentPadding = PaddingValues(16.dp),
                                        containerColor = Color.White.copy(alpha = 0.1f)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(
                                                        if (isOnline) Color.Green else Color.Gray,
                                                        CircleShape
                                                    )
                                            )
                                            Spacer(Modifier.width(16.dp))
                                            Text(
                                                userId,
                                                color = OnSecondary,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(Modifier.weight(1f))
                                            Icon(
                                                Icons.AutoMirrored.Rounded.ArrowForward,
                                                contentDescription = null,
                                                tint = OnSecondary.copy(alpha = 0.3f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SyncControlCard(
    syncInputId: String,
    onIdChange: (String) -> Unit,
    isSyncing: Boolean,
    isController: Boolean,
    connectionState: ConnectionState,
    currentRoomId: String?,
    onRequestSync: () -> Unit,
    onRequestControl: () -> Unit,
    onStopSync: () -> Unit
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = if (isController) Color.Green.copy(alpha = 0.5f) else null,
            containerColor = Color.White.copy(alpha = 0.1f)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(com.example.musicx2.R.raw.sync_pulse))
                    if (isSyncing) {
                        LottieAnimation(
                            composition = composition,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Icon(
                            if (connectionState == ConnectionState.CONNECTING) Icons.Rounded.Sync else Icons.Rounded.SyncDisabled,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Sync Listening",
                                style = MaterialTheme.typography.titleLarge,
                                color = OnSecondary,
                                fontWeight = FontWeight.Bold
                            )
                            if (isController) {
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    Icons.Rounded.Star,
                                    contentDescription = "Controller",
                                    tint = Color.Green,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            when {
                                connectionState == ConnectionState.CONNECTING -> "Connecting to peer..."
                                isSyncing -> "Synced with $currentRoomId 🎧"
                                else -> "Enter peer ID to start sync"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSecondary.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                if (!isSyncing && connectionState != ConnectionState.CONNECTING) {
                    OutlinedTextField(
                        value = syncInputId,
                        onValueChange = onIdChange,
                        label = { Text("Peer User ID") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnSecondary,
                            unfocusedTextColor = OnSecondary,
                            cursorColor = Primary,
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = OnSecondary.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = onRequestSync,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(12.dp),
                        enabled = syncInputId.isNotBlank()
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.Send,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("Request Sync", fontWeight = FontWeight.Bold)
                    }
                } else if (connectionState == ConnectionState.CONNECTING) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Primary, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Waiting for response...", color = OnSecondary.copy(alpha = 0.7f))
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = onStopSync,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Red
                            )
                        ) {
                            Text("Cancel Connecting")
                        }
                    }
                } else {
                    if (!isController) {
                        Button(
                            onClick = onRequestControl,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(
                                    alpha = 0.1f
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Become Controller", color = OnSecondary)
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    Button(
                        onClick = onStopSync,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Stop,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("Terminate Sync", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatView(
    modifier: Modifier = Modifier,
    messages: List<ChatMessage>,
    currentUserId: String,
    onSendMessage: (String, ReplyMetadata?) -> Unit,
    onDeleteMessage: (String) -> Unit,
    onEditMessage: (String, String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var replyTo by remember { mutableStateOf<ReplyMetadata?>(null) }
    var editingMessageId by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = listState,
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                val isMe = message.senderId == currentUserId
                MessageBubble(
                    message = message,
                    isMe = isMe,
                    onReply = {
                        replyTo = ReplyMetadata(message.id, message.content, message.senderId)
                    },
                    onDelete = { onDeleteMessage(message.id) },
                    onEdit = {
                        text = message.content
                        editingMessageId = message.id
                    }
                )
            }
        }

        // Reply Preview
        if (replyTo != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color.White.copy(alpha = 0.05f),
                        RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Reply,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        replyTo?.senderId ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary
                    )
                    Text(
                        replyTo?.content ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSecondary.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }
                IconButton(onClick = { replyTo = null }) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = null,
                        tint = OnSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Edit Preview
        if (editingMessageId != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color.White.copy(alpha = 0.05f),
                        RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Edit,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Editing message",
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    editingMessageId = null
                    text = ""
                }) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = null,
                        tint = OnSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message") },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = OnSecondary,
                    unfocusedTextColor = OnSecondary,
                    focusedContainerColor = Color.White.copy(alpha = 0.05f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (text.isNotBlank()) {
                        if (editingMessageId != null) {
                            onEditMessage(editingMessageId!!, text)
                            editingMessageId = null
                        } else {
                            onSendMessage(text, replyTo)
                            replyTo = null
                        }
                        text = ""
                    }
                },
                modifier = Modifier.background(Primary, CircleShape)
            ) {
                Icon(
                    if (editingMessageId != null) Icons.Rounded.Check else Icons.AutoMirrored.Rounded.Send,
                    contentDescription = null,
                    tint = Color.Black
                )
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isMe: Boolean,
    onReply: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        if (!isMe) {
            Text(
                message.senderId,
                style = MaterialTheme.typography.labelSmall,
                color = OnSecondary.copy(alpha = 0.5f),
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
        }

        Box(
            modifier = Modifier
                .background(
                    if (isMe) Primary else Color.White.copy(alpha = 0.1f),
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMe) 16.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 16.dp
                    )
                )
                .clickable { showMenu = !showMenu }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
                if (message.replyTo != null) {
                    Column(
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(4.dp)
                    ) {
                        Text(
                            message.replyTo.senderId,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isMe) Color.Black else Primary
                        )
                        Text(
                            message.replyTo.content,
                            style = MaterialTheme.typography.bodySmall,
                            color = (if (isMe) Color.Black else OnSecondary).copy(alpha = 0.6f),
                            maxLines = 1
                        )
                    }
                }

                Text(
                    message.content,
                    color = if (isMe) Color.Black else OnSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )

                if (message.isEdited) {
                    Text(
                        "(edited)",
                        style = MaterialTheme.typography.labelSmall,
                        color = (if (isMe) Color.Black else OnSecondary).copy(alpha = 0.4f),
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }

        AnimatedVisibility(visible = showMenu) {
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(onClick = {
                    onReply()
                    showMenu = false
                }, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Rounded.Reply,
                        contentDescription = "Reply",
                        tint = OnSecondary.copy(alpha = 0.6f)
                    )
                }
                if (isMe && !message.isDeleted) {
                    IconButton(onClick = {
                        onEdit()
                        showMenu = false
                    }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Rounded.Edit,
                            contentDescription = "Edit",
                            tint = OnSecondary.copy(alpha = 0.6f)
                        )
                    }
                    IconButton(onClick = {
                        onDelete()
                        showMenu = false
                    }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

