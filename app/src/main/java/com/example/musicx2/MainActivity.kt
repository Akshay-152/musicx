package com.example.musicx2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicx2.playback.ConnectionState
import com.example.musicx2.ui.components.EditTrackDialog
import com.example.musicx2.ui.components.ImmersivePlayer
import com.example.musicx2.ui.components.LibraryHeader
import com.example.musicx2.ui.components.LoginDialog
import com.example.musicx2.ui.components.MiniPlayer
import com.example.musicx2.ui.components.MusicSearchBar
import com.example.musicx2.ui.components.PlaylistDetails
import com.example.musicx2.ui.components.PlaylistDialog
import com.example.musicx2.ui.components.SyncStudio
import com.example.musicx2.ui.components.TrackCard
import com.example.musicx2.ui.components.UploadDialog
import com.example.musicx2.ui.theme.Background
import com.example.musicx2.ui.theme.DarkSurface
import com.example.musicx2.ui.theme.Musicx2Theme
import com.example.musicx2.ui.theme.OnSecondary
import com.example.musicx2.ui.theme.Primary
import com.example.musicx2.ui.viewmodel.ActivePanel
import com.example.musicx2.ui.viewmodel.MusicViewModel
import com.example.musicx2.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Musicx2Theme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MusicViewModel = hiltViewModel()) {
    val trackResource by viewModel.filteredTracks.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    
    val activePanelState by viewModel.activePanel.collectAsState()
    val isPlayerOpen = currentTrack != null && activePanelState == ActivePanel.PLAYER

    // Combined BackHandler to manage all panel states and drawer
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BackHandler(enabled = activePanelState != ActivePanel.NONE || drawerState.isOpen) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            viewModel.closePanel()
        }
    }

    val progress by viewModel.progress.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val favoriteTrackIds by viewModel.favoriteTrackIds.collectAsState()
    val shuffleModeEnabled by viewModel.shuffleModeEnabled.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val volume by viewModel.volume.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState(initial = "")

    val currentUserId by viewModel.currentUserId.collectAsState(initial = null)

    val activePanel = activePanelState
    val selectedTrack by viewModel.selectedTrack.collectAsState()
    val showFavoritesOnly by viewModel.showFavoritesOnly.collectAsState()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsState()
    val playlistTracks by viewModel.playlistTracks.collectAsState()

    val isImmersiveVisible = isPlayerOpen || (activePanel == ActivePanel.PLAYLIST_DETAILS && selectedPlaylist != null)

    val snackbarHostState = remember { SnackbarHostState() }

    val incomingRequest by viewModel.incomingRequest.collectAsState()

    // Global Sync Request Popup
    incomingRequest?.let { request ->
        AlertDialog(
            onDismissRequest = { /* Must respond */ },
            containerColor = DarkSurface,
            title = { Text("Sync Request", color = OnSecondary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "User ${request.from} wants to sync music with you. Allow real-time listening?",
                    color = OnSecondary.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.respondToRoomRequest(request.from, true) }
                ) {
                    Text("Allow", color = Primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.respondToRoomRequest(request.from, false) }
                ) {
                    Text("Deny", color = Color.Red.copy(alpha = 0.8f))
                }
            }
        )
    }

    LaunchedEffect(userMessage) {
        if (userMessage.isNotBlank()) {
            snackbarHostState.showSnackbar(userMessage)
        }
    }

    val backgroundColors by viewModel.backgroundColors.collectAsState()

    var currentGradient by remember {
        mutableStateOf(Brush.verticalGradient(listOf(Background, Background)))
    }

    LaunchedEffect(backgroundColors) {
        currentGradient = Brush.verticalGradient(
            colors = backgroundColors
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DarkSurface,
                drawerShape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp),
                modifier = Modifier.width(300.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Primary.copy(alpha = 0.2f), Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Primary
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            currentUserId ?: "Guest User",
                            style = MaterialTheme.typography.titleLarge,
                            color = OnSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        if (currentUserId != null) {
                            Text(
                                "ID: $currentUserId",
                                style = MaterialTheme.typography.labelMedium,
                                color = OnSecondary.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (currentUserId != null) {
                    val playlists by viewModel.playlists.collectAsState()

                    NavigationDrawerItem(
                        label = { Text("Library", fontWeight = FontWeight.Medium) },
                        selected = activePanel == ActivePanel.NONE && selectedPlaylist == null,
                        onClick = {
                            scope.launch { drawerState.close() }
                            viewModel.closePanel()
                        },
                        icon = { Icon(Icons.Rounded.LibraryMusic, null) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = Primary.copy(alpha = 0.15f),
                            selectedTextColor = Primary,
                            selectedIconColor = Primary,
                            unselectedTextColor = OnSecondary.copy(alpha = 0.7f),
                            unselectedIconColor = OnSecondary.copy(alpha = 0.7f)
                        )
                    )

                    NavigationDrawerItem(
                        label = { Text("Sync Studio", fontWeight = FontWeight.Medium) },
                        selected = activePanel == ActivePanel.SYNC_STUDIO,
                        onClick = {
                            scope.launch { drawerState.close() }
                            viewModel.showPanel(ActivePanel.SYNC_STUDIO)
                        },
                        icon = { Icon(Icons.Rounded.Group, null) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedTextColor = OnSecondary.copy(alpha = 0.7f),
                            unselectedIconColor = OnSecondary.copy(alpha = 0.7f)
                        )
                    )

                    var playlistsExpanded by remember { mutableStateOf(false) }
                    NavigationDrawerItem(
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Playlists",
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    if (playlistsExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                    null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        selected = false,
                        onClick = { playlistsExpanded = !playlistsExpanded },
                        icon = { Icon(Icons.AutoMirrored.Rounded.PlaylistPlay, null) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedTextColor = OnSecondary.copy(alpha = 0.7f),
                            unselectedIconColor = OnSecondary.copy(alpha = 0.7f)
                        )
                    )

                    if (playlistsExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            playlists.forEach { playlist ->
                                NavigationDrawerItem(
                                    label = { Text(playlist.name, fontSize = 14.sp) },
                                    selected = selectedPlaylist?.id == playlist.id,
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        viewModel.showPlaylistDetails(playlist)
                                    },
                                    icon = {
                                        Icon(
                                            Icons.AutoMirrored.Rounded.QueueMusic,
                                            null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    modifier = Modifier.padding(
                                        horizontal = 32.dp,
                                        vertical = 2.dp
                                    ),
                                    colors = NavigationDrawerItemDefaults.colors(
                                        unselectedTextColor = OnSecondary.copy(alpha = 0.6f),
                                        unselectedIconColor = OnSecondary.copy(alpha = 0.6f),
                                        selectedContainerColor = Primary.copy(alpha = 0.1f),
                                        selectedTextColor = Primary,
                                        selectedIconColor = Primary
                                    )
                                )
                            }
                        }
                    }

                    NavigationDrawerItem(
                        label = { Text("Upload Song", fontWeight = FontWeight.Medium) },
                        selected = activePanel == ActivePanel.UPLOAD,
                        onClick = {
                            scope.launch { drawerState.close() }
                            viewModel.showPanel(ActivePanel.UPLOAD)
                        },
                        icon = { Icon(Icons.Rounded.Upload, null) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedTextColor = OnSecondary.copy(alpha = 0.7f),
                            unselectedIconColor = OnSecondary.copy(alpha = 0.7f)
                        )
                    )
                }

                Spacer(Modifier.weight(1f))

                NavigationDrawerItem(
                    label = { 
                        Text(
                            if (currentUserId != null) "My Profile" else "Login / Studio Access", 
                            fontWeight = FontWeight.Medium
                        ) 
                    },
                    selected = activePanel == ActivePanel.LOGIN,
                    onClick = {
                        scope.launch { drawerState.close() }
                        viewModel.showPanel(ActivePanel.LOGIN)
                    },
                    icon = { Icon(Icons.Rounded.AccountCircle, null) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedTextColor = OnSecondary.copy(alpha = 0.7f),
                        unselectedIconColor = OnSecondary.copy(alpha = 0.7f)
                    )
                )

                if (currentUserId != null) {
                    NavigationDrawerItem(
                        label = { Text("Sign Out", fontWeight = FontWeight.Medium) },
                        selected = false,
                        onClick = {
                            viewModel.logout()
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.AutoMirrored.Rounded.Logout, null) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedTextColor = Color.Red.copy(alpha = 0.7f),
                            unselectedIconColor = Color.Red.copy(alpha = 0.7f)
                        )
                    )
                }

                Spacer(Modifier.weight(1f))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "MADE BY",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSecondary.copy(alpha = 0.3f),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "AKSHAY PK",
                        style = MaterialTheme.typography.labelMedium,
                        color = Primary.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                if (activePanel == ActivePanel.NONE) {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                "MUSIX",
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                fontSize = 18.sp
                            )
                        },
                        navigationIcon = {
                            Box {
                                IconButton(
                                    onClick = { scope.launch { drawerState.open() } },
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Menu,
                                        contentDescription = "Menu",
                                        tint = OnSecondary
                                    )
                                }

                            }
                        },
                        actions = {
                            IconButton(
                                onClick = { viewModel.showPanel(ActivePanel.LOGIN) },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.AccountCircle,
                                    contentDescription = "Profile",
                                    tint = Primary
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = OnSecondary
                        )
                    )
                }
            },
            bottomBar = {
                val isChatOpen = activePanel == ActivePanel.SYNC_STUDIO
                AnimatedVisibility(
                    visible = currentTrack != null && activePanel != ActivePanel.PLAYER && !isChatOpen,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    currentTrack?.let { track ->
                        MiniPlayer(
                            track = track,
                            isPlaying = isPlaying,
                            progress = progress,
                            onPlayPauseClick = { viewModel.togglePlayPause() },
                            onPreviousClick = { viewModel.skipPrevious() },
                            onNextClick = { viewModel.skipNext() },
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            onClick = { 
                                if (!isImmersiveVisible) {
                                    viewModel.showPanel(ActivePanel.PLAYER) 
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .background(currentGradient)
            ) {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .blur(if (isImmersiveVisible) 20.dp else 0.dp)
                        .graphicsLayer {
                            alpha = if (isImmersiveVisible) 0.3f else 1.0f
                        }
                        .pointerInput(isImmersiveVisible) {
                            if (isImmersiveVisible) {
                                detectTapGestures { /* Block interactions */ }
                            }
                        }
                ) {
                    if (currentUserId == null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "My Music Player",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = OnSecondary
                            )
                        }
                    } else {
                        MusicSearchBar(
                            query = searchQuery,
                            onQueryChange = { viewModel.onSearchQueryChange(it) },
                            modifier = Modifier.padding(vertical = 8.dp),
                            enabled = !isImmersiveVisible
                        )

                        when (val resource = trackResource) {
                            is Resource.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Primary)
                                }
                            }

                            is Resource.Error -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = resource.message,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }

                            is Resource.Success -> {
                                val tracks = resource.data
                                val listState = rememberLazyListState()

                                val headerScrollProgress by remember {
                                    derivedStateOf {
                                        if (listState.firstVisibleItemIndex > 0) 1f
                                        else {
                                            val firstItemOffset =
                                                listState.firstVisibleItemScrollOffset.toFloat()
                                            (firstItemOffset / 300f).coerceIn(0f, 1f)
                                        }
                                    }
                                }

                                val shouldLoadMore = remember {
                                    derivedStateOf {
                                        val lastVisibleItem =
                                            listState.layoutInfo.visibleItemsInfo.lastOrNull()
                                                ?: return@derivedStateOf false
                                        lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 5
                                    }
                                }

                                LaunchedEffect(shouldLoadMore.value) {
                                    if (shouldLoadMore.value) {
                                        viewModel.loadMoreTracks()
                                    }
                                }

                                var showSortMenu by remember { mutableStateOf(false) }

                                LibraryHeader(
                                    trackCount = tracks.size,
                                    onShuffleClick = { viewModel.shuffleAll() },
                                    onSortClick = { showSortMenu = true },
                                    showFavoritesOnly = showFavoritesOnly,
                                    onToggleFavorites = { viewModel.toggleFavoritesFilter() },
                                    modifier = Modifier.graphicsLayer {
                                        val scale = 1f - (headerScrollProgress * 0.2f)
                                        scaleX = scale
                                        scaleY = scale
                                        alpha = 1f - (headerScrollProgress * 0.5f)
                                    }
                                )

                                Box(modifier = Modifier.fillMaxWidth()) {
                                    DropdownMenu(
                                        expanded = showSortMenu,
                                        onDismissRequest = { showSortMenu = false },
                                        modifier = Modifier.background(DarkSurface)
                                    ) {
                                        MusicViewModel.SortOrder.entries.forEach { order ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = when (order) {
                                                            MusicViewModel.SortOrder.TITLE -> "Title"
                                                            MusicViewModel.SortOrder.ARTIST -> "Artist"
                                                            MusicViewModel.SortOrder.DATE_ADDED -> "Date Added"
                                                        },
                                                        color = OnSecondary
                                                    )
                                                },
                                                onClick = {
                                                    viewModel.setSortOrder(order)
                                                    showSortMenu = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                LazyColumn(
                                    state = listState,
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    contentPadding = PaddingValues(bottom = 80.dp),
                                    userScrollEnabled = !isImmersiveVisible
                                ) {
                                    items(
                                        tracks,
                                        key = { it.id.ifBlank { "track_${tracks.indexOf(it)}" } }) { track ->
                                        val itemOffset = remember {
                                            derivedStateOf {
                                                listState.layoutInfo.visibleItemsInfo.find { it.key == track.id }?.offset
                                                    ?: 0
                                            }
                                        }
                                        val scale by remember {
                                            derivedStateOf {
                                                val viewPortHeight =
                                                    listState.layoutInfo.viewportEndOffset
                                                if (viewPortHeight == 0) 1f
                                                else {
                                                    val progress =
                                                        itemOffset.value.toFloat() / viewPortHeight
                                                    (1f - (progress * 0.1f)).coerceIn(0.9f, 1f)
                                                }
                                            }
                                        }

                                        val currentUserId by viewModel.currentUserId.collectAsState()
                                        val isAdmin by viewModel.isAdmin.collectAsState()

                                        TrackCard(
                                            track = track,
                                            isFavorite = favoriteTrackIds.contains(track.id),
                                            isOwner = track.ownerId == currentUserId || isAdmin,
                                            onFavoriteClick = {
                                                if (!isImmersiveVisible) viewModel.toggleFavorite(
                                                    track.id
                                                )
                                            },
                                            onClick = {
                                                if (!isImmersiveVisible) {
                                                    viewModel.playTrack(track)
                                                }
                                            },
                                            onDoubleClick = {
                                                if (!isImmersiveVisible) {
                                                    viewModel.playTrack(track)
                                                }
                                            },
                                            onPlaylistClick = {
                                                if (!isImmersiveVisible) {
                                                    viewModel.showPanel(ActivePanel.PLAYLIST, track)
                                                }
                                            },
                                            onDownloadClick = {
                                                if (!isImmersiveVisible) viewModel.downloadTrack(
                                                    track
                                                )
                                            },
                                            onEditClick = {
                                                if (!isImmersiveVisible) {
                                                    viewModel.showPanel(ActivePanel.EDIT, track)
                                                }
                                            },
                                            onDeleteClick = {
                                                if (!isImmersiveVisible) {
                                                    viewModel.showPanel(ActivePanel.DELETE, track)
                                                }
                                            },
                                            isCurrentlyPlaying = currentTrack?.id == track.id,
                                            modifier = Modifier.graphicsLayer {
                                                scaleX = scale
                                                scaleY = scale
                                                alpha = scale
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Dark Scrim for Player
                if (isImmersiveVisible) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .pointerInput(Unit) { detectTapGestures { /* Block background clicks */ } }
                    )
                }

                // --- UI State Management: Panels ---

                AnimatedVisibility(
                    visible = activePanel == ActivePanel.UPLOAD,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    UploadDialog(
                        onDismiss = { viewModel.closePanel() },
                        onUploadFile = { title, artist, audioUri, coverUri ->
                            viewModel.uploadTrack(audioUri, title, artist, coverUri)
                        },
                        isUploading = isUploading
                    )
                }

                AnimatedVisibility(
                    visible = activePanel == ActivePanel.LOGIN,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    LoginDialog(
                        onDismiss = { viewModel.closePanel() },
                        onLogin = { userId ->
                            viewModel.login(userId)
                            viewModel.closePanel()
                        },
                        onGenerateId = { viewModel.generateRandomId() }
                    )
                }

                AnimatedVisibility(
                    visible = activePanel == ActivePanel.PLAYLIST && selectedTrack != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    val playlists by viewModel.playlists.collectAsState()
                    selectedTrack?.let { track ->
                        PlaylistDialog(
                            playlists = playlists,
                            onPlaylistSelected = { playlistId ->
                                viewModel.addTrackToPlaylist(playlistId, track.id)
                                viewModel.closePanel()
                            },
                            onCreatePlaylist = { name ->
                                viewModel.createPlaylist(name)
                            },
                            onDismiss = { viewModel.closePanel() }
                        )
                    }
                }

                AnimatedVisibility(
                    visible = activePanel == ActivePanel.EDIT && selectedTrack != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    selectedTrack?.let { track ->
                        EditTrackDialog(
                            track = track,
                            onDismiss = { viewModel.closePanel() },
                            onUpdate = { title, artist, coverUri ->
                                viewModel.updateTrack(track.id, title, artist)
                            }
                        )
                    }
                }

                if (activePanel == ActivePanel.DELETE && selectedTrack != null) {
                    selectedTrack?.let { trackToDelete ->
                        AlertDialog(
                            onDismissRequest = { viewModel.closePanel() },
                            containerColor = DarkSurface,
                            title = { Text("Delete Track?", color = OnSecondary) },
                            text = { Text("Are you sure you want to delete \"${trackToDelete.title}\"?", color = OnSecondary.copy(alpha = 0.7f)) },
                            confirmButton = {
                                TextButton(onClick = {
                                    viewModel.deleteTrack(trackToDelete.id)
                                }) {
                                    Text("Delete", color = Color.Red)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { viewModel.closePanel() }) {
                                    Text("Cancel", color = OnSecondary)
                                }
                            }
                        )
                    }
                }

                AnimatedVisibility(
                    visible = activePanel == ActivePanel.SYNC_STUDIO,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    SyncStudio(
                        viewModel = viewModel,
                        onBack = { viewModel.closePanel() }
                    )
                }

                AnimatedVisibility(
                    visible = activePanel == ActivePanel.PLAYER,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    currentTrack?.let { track ->
                        ImmersivePlayer(
                            track = track,
                            isPlaying = isPlaying,
                            progress = progress,
                            isFavorite = favoriteTrackIds.contains(track.id),
                            shuffleModeEnabled = shuffleModeEnabled,
                            repeatMode = repeatMode,
                            playbackSpeed = playbackSpeed,
                            volume = volume,
                            onPlayPauseClick = { viewModel.togglePlayPause() },
                            onNextClick = { viewModel.skipNext() },
                            onPreviousClick = { viewModel.skipPrevious() },
                            onSeek = { position ->
                                viewModel.seekTo(position)
                            },
                            onVolumeChange = { viewModel.setVolume(it) },
                            onFavoriteClick = { viewModel.toggleFavorite(track.id) },
                            onShuffleClick = { viewModel.toggleShuffle() },
                            onRepeatClick = { viewModel.toggleRepeatMode() },
                            onSpeedClick = {
                                val nextSpeed = when (playbackSpeed) {
                                    1.0f -> 1.5f
                                    1.5f -> 2.0f
                                    2.0f -> 0.5f
                                    else -> 1.0f
                                }
                                viewModel.setPlaybackSpeed(nextSpeed)
                            },
                            onPlaylistClick = {
                                viewModel.showPanel(ActivePanel.PLAYLIST, track)
                            },
                            onCloseClick = { viewModel.closePanel() }
                        )
                    }
                }

                AnimatedVisibility(
                    visible = activePanel == ActivePanel.PLAYLIST_DETAILS && selectedPlaylist != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    selectedPlaylist?.let { playlist ->
                        PlaylistDetails(
                            playlist = playlist,
                            tracks = playlistTracks,
                            onBackClick = { viewModel.closePanel() },
                            onPlayClick = { viewModel.playTrack(it, playlistTracks) },
                            onRemoveTrack = { trackId ->
                                viewModel.removeTrackFromPlaylist(playlist.id, trackId)
                            },
                            onMoveTrack = { from, to ->
                                viewModel.moveTrackInPlaylist(playlist.id, from, to)
                            },
                            onRenamePlaylist = { newName ->
                                viewModel.renamePlaylist(playlist.id, newName)
                            },
                            onDeletePlaylist = {
                                viewModel.deletePlaylist(playlist.id)
                                viewModel.closePanel()
                            },
                            onShuffleClick = {
                                if (playlistTracks.isNotEmpty()) {
                                    viewModel.playTrack(playlistTracks.shuffled().first(), playlistTracks)
                                }
                            },
                            currentTrackId = currentTrack?.id
                        )
                    }
                }
            }
        }

        if (connectionState == ConnectionState.CONNECTING) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { /* Block interactions */ },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Connecting...", color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { viewModel.cancelConnecting() },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White,
                            containerColor = Color.Transparent
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
                    ) {
                        Text("Cancel Connecting")
                    }
                }
            }
        }
    }
}

