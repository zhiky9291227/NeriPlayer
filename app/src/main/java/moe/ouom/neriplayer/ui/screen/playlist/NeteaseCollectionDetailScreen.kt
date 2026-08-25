package moe.ouom.neriplayer.ui.screen.playlist

/*
 * NeriPlayer - A unified Android player for streaming music and videos from multiple online platforms.
 * Copyright (C) 2025-2025 NeriPlayer developers
 * https://github.com/cwuom/NeriPlayer
 *
 * This software is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software.
 * If not, see <https://www.gnu.org/licenses/>.
 *
 * File: moe.ouom.neriplayer.ui.screen.playlist/NeteaseCollectionDetailScreen
 * Created: 2025/8/10
 */

import android.app.Application
import android.content.ClipData
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import moe.ouom.neriplayer.R
import moe.ouom.neriplayer.core.di.AppContainer
import moe.ouom.neriplayer.data.local.playlist.sync.NeteaseRemotePlaylist
import moe.ouom.neriplayer.core.download.GlobalDownloadManager
import moe.ouom.neriplayer.core.player.download.AudioDownloadManager
import moe.ouom.neriplayer.core.player.PlayerManager
import moe.ouom.neriplayer.data.local.playlist.LocalPlaylistRepository
import moe.ouom.neriplayer.data.local.playlist.launchLocalPlaylistMutation
import moe.ouom.neriplayer.data.local.playlist.system.FavoritesPlaylist
import moe.ouom.neriplayer.data.local.playlist.system.LocalFilesPlaylist
import moe.ouom.neriplayer.data.model.displayArtist
import moe.ouom.neriplayer.data.model.displayName
import moe.ouom.neriplayer.data.model.sameIdentityAs
import moe.ouom.neriplayer.data.model.stableKey
import moe.ouom.neriplayer.data.playlist.favorite.FavoritePlaylistRepository
import moe.ouom.neriplayer.ui.LocalMiniPlayerHeight
import moe.ouom.neriplayer.ui.rememberMainTabDetailVisibilityState
import moe.ouom.neriplayer.ui.component.download.BatchDownloadManagerSheet
import moe.ouom.neriplayer.ui.component.playlist.PlaylistExportSheet
import moe.ouom.neriplayer.ui.component.playlist.showPlaylistBatchExportAddedResult
import moe.ouom.neriplayer.ui.component.playlist.showPlaylistBatchExportCreatedResult
import moe.ouom.neriplayer.ui.viewmodel.playlist.NeteaseCollectionDetailUiState
import moe.ouom.neriplayer.ui.viewmodel.playlist.NeteaseCollectionDetailViewModel
import moe.ouom.neriplayer.ui.viewmodel.tab.isNeteaseRadarPlaylist
import moe.ouom.neriplayer.ui.viewmodel.playlist.NeteaseCollectionHeader
import moe.ouom.neriplayer.data.model.SongItem
import moe.ouom.neriplayer.ui.viewmodel.tab.AlbumSummary
import moe.ouom.neriplayer.ui.viewmodel.tab.PlaylistSummary
import moe.ouom.neriplayer.ui.util.rememberSongDisplayCoverUrl
import moe.ouom.neriplayer.ui.haptic.HapticFloatingActionButton
import moe.ouom.neriplayer.ui.haptic.HapticIconButton
import moe.ouom.neriplayer.ui.haptic.HapticTextButton
import moe.ouom.neriplayer.ui.feedback.NeriOverlaySnackbarHost
import moe.ouom.neriplayer.ui.feedback.AppFeedback
import moe.ouom.neriplayer.ui.feedback.showNeriSnackbar
import moe.ouom.neriplayer.core.logging.NPLogger
import moe.ouom.neriplayer.util.format.formatDuration
import moe.ouom.neriplayer.util.format.formatPlayCount
import moe.ouom.neriplayer.util.media.offlineCachedImageRequest
import moe.ouom.neriplayer.ui.haptic.performHapticFeedback
import moe.ouom.neriplayer.util.search.playlistSearchValues
import kotlin.random.Random

internal fun isNeteaseCollectionHeaderForRoute(
    header: NeteaseCollectionHeader?,
    playlistId: Long,
    playlistSource: String
): Boolean {
    return header?.id == playlistId &&
        header.isAlbum == (playlistSource == "neteaseAlbum")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NeteasePlaylistDetailScreen(
    playlist: PlaylistSummary,
    onBack: () -> Unit = {},
    onSongClick: (List<SongItem>, Int) -> Unit = { _, _ -> },
    offlineMode: Boolean = false
) {
    val context = LocalContext.current
    val vm: NeteaseCollectionDetailViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val app = context.applicationContext as Application
                NeteaseCollectionDetailViewModel(app)
            }
        }
    )

    val ui by vm.uiState.collectAsState()
    // 使用 Unit 作为 key, 确保每次进入都重新加载最新数据
    LaunchedEffect(Unit) { vm.startPlaylist(playlist) }

    // 保存最新的header数据, 用于在Screen销毁时更新使用记录
    var latestHeader by remember { mutableStateOf<NeteaseCollectionHeader?>(null) }
    LaunchedEffect(ui.header) {
        ui.header?.let { latestHeader = it }
    }

    // 是否为登录用户自己创建的歌单（决定多选工具栏是否显示"从网易云删除"按钮）
    var canDeleteRemoteTracks by remember { mutableStateOf(false) }
    LaunchedEffect(ui.header?.id, offlineMode) {
        val header = ui.header
        if (offlineMode || header == null || header.isAlbum || header.id <= 0L ||
            isNeteaseRadarPlaylist(header.id)
        ) {
            canDeleteRemoteTracks = false
        } else {
            canDeleteRemoteTracks = vm.isCurrentPlaylistOwnedByUser()
        }
    }

    // 在 Screen 销毁时更新使用记录, 确保返回主页时卡片显示最新信息
    DisposableEffect(Unit) {
        onDispose {
            latestHeader?.let { header ->
                AppContainer.launchBackgroundIo {
                    AppContainer.playlistUsageRepo.updateInfo(
                        id = header.id,
                        name = header.name,
                        picUrl = header.coverUrl,
                        trackCount = header.trackCount,
                        source = "netease"
                    )
                }
            }
        }
    }

    DetailScreen(
        ui = ui,
        playlistId = playlist.id,
        playlistSource = "netease",
        initialCoverUrl = playlist.picUrl,
        onRetry = vm::retry,
        onBack = onBack,
        onSongClick = onSongClick,
        offlineMode = offlineMode,
        canDeleteRemoteTracks = canDeleteRemoteTracks,
        onDeleteRemoteTracks = { ids -> vm.removeTracksFromCurrentPlaylist(ids) }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NeteaseAlbumDetailScreen(
    album: AlbumSummary,
    onBack: () -> Unit = {},
    onSongClick: (List<SongItem>, Int) -> Unit = { _, _ -> },
    offlineMode: Boolean = false
) {
    val context = LocalContext.current
    val vm: NeteaseCollectionDetailViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val app = context.applicationContext as Application
                NeteaseCollectionDetailViewModel(app)
            }
        }
    )

    val ui by vm.uiState.collectAsState()
    // 使用 Unit 作为 key, 确保每次进入都重新加载最新数据
    LaunchedEffect(Unit) { vm.startAlbum(album) }

    // 保存最新的header数据, 用于在Screen销毁时更新使用记录
    var latestHeader by remember { mutableStateOf<NeteaseCollectionHeader?>(null) }
    LaunchedEffect(ui.header) {
        ui.header?.let { latestHeader = it }
    }

    // 在 Screen 销毁时更新使用记录, 确保返回主页时卡片显示最新信息
    DisposableEffect(Unit) {
        onDispose {
            latestHeader?.let { header ->
                AppContainer.launchBackgroundIo {
                    AppContainer.playlistUsageRepo.updateInfo(
                        id = header.id,
                        name = header.name,
                        picUrl = header.coverUrl,
                        trackCount = header.trackCount,
                        source = "neteaseAlbum"
                    )
                }
            }
        }
    }

    DetailScreen(
        ui = ui,
        playlistId = album.id,
        playlistSource = "neteaseAlbum",
        initialCoverUrl = album.picUrl,
        onRetry = vm::retry,
        onBack = onBack,
        onSongClick = onSongClick,
        offlineMode = offlineMode
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
@Suppress("AssignedValueIsNeverRead")
fun DetailScreen(
    ui: NeteaseCollectionDetailUiState,
    playlistId: Long,
    playlistSource: String,
    initialCoverUrl: String? = null,
    onRetry: () -> Unit,
    onBack: () -> Unit = {},
    onSongClick: (List<SongItem>, Int) -> Unit = { _, _ -> },
    offlineMode: Boolean = false,
    canDeleteRemoteTracks: Boolean = false,
    onDeleteRemoteTracks: (suspend (List<Long>) -> Unit)? = null
) {

    val context = LocalContext.current

    // 下载进度
    var showDownloadManager by remember { mutableStateOf(false) }
    val downloadTaskSummary by GlobalDownloadManager.downloadTaskSummary.collectAsState()
    val pendingTaskCount = downloadTaskSummary.pendingTaskCount
    val hasDownloadManagerEntry = downloadTaskSummary.hasPendingTasks

    val currentSong by PlayerManager.currentSongFlow.collectAsState()
    val shuffleEnabled by PlayerManager.shuffleModeFlow.collectAsState()
    val repeatMode by PlayerManager.repeatModeFlow.collectAsState()
    val listState = rememberSaveable(playlistId, saver = LazyListState.Saver) {
        LazyListState(firstVisibleItemIndex = 0, firstVisibleItemScrollOffset = 0)
    }
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    var showSearch by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var headerSearchFocused by remember { mutableStateOf(false) }
    var dockedSearchFocused by remember { mutableStateOf(false) }
    val searchInputState = rememberPlaylistSearchInputState(
        query = searchQuery,
        onQueryChange = { searchQuery = it }
    )
    val searchFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // 多选与导出到本地歌单
    val repo = remember(context) { LocalPlaylistRepository.getInstance(context) }
    val allPlaylists by repo.playlists.collectAsState()
    val favoriteSongs = remember(allPlaylists, context) {
        FavoritesPlaylist.firstOrNull(allPlaylists, context)?.songs.orEmpty()
    }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var isDeletingRemote by remember { mutableStateOf(false) }
    // 登录用户即可往自己的网易云歌单添加歌曲（含收藏的歌单）
    val neteaseCookies by AppContainer.neteaseCookieRepo.cookieFlow.collectAsState()
    val canAddToNetease = !offlineMode && neteaseCookies.containsKey("MUSIC_U")
    // 单曲级网易云操作（三点菜单入口）
    var neteaseActionSong by remember { mutableStateOf<SongItem?>(null) }
    var showNeteasePlaylistPicker by remember { mutableStateOf(false) }
    var neteaseRemotePlaylists by remember {
        mutableStateOf<List<NeteaseRemotePlaylist>>(emptyList())
    }
    var neteasePlaylistsLoading by remember { mutableStateOf(false) }
    var neteasePlaylistsError by remember { mutableStateOf<String?>(null) }
    var showNeteaseDeleteSongConfirm by remember { mutableStateOf(false) }
    fun toggleSelect(id: Long) {
        selectedIds = if (selectedIds.contains(id)) selectedIds - id else selectedIds + id
    }
    fun clearSelection() { selectedIds = emptySet() }
    fun selectAll() { selectedIds = ui.tracks.map { it.id }.toSet() }
    fun exitSelection() { selectionMode = false; clearSelection();}

    // 收藏歌单
    val favoriteRepo = remember(context) { FavoritePlaylistRepository.getInstance(context) }
    val favorites by favoriteRepo.favorites.collectAsState()
    val isFavorite = remember(favorites, playlistId) {
        favoriteRepo.isFavorite(playlistId, playlistSource)
    }

    LaunchedEffect(isFavorite, ui.header, ui.tracks) {
        if (!isFavorite) return@LaunchedEffect
        val header = ui.header ?: return@LaunchedEffect
        favoriteRepo.updateFavoriteMeta(
            id = header.id,
            name = header.name,
            coverUrl = header.coverUrl,
            trackCount = header.trackCount,
            source = playlistSource,
            songs = ui.tracks
        )
    }

    var showExportSheet by remember { mutableStateOf(false) }
    var showExportAllSheet by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val favoriteAddedText = stringResource(R.string.favorite_added)
    val favoriteRemovedText = stringResource(R.string.favorite_removed)
    fun toggleSongFavorite(song: SongItem, isFavoriteSong: Boolean) {
        val message = if (isFavoriteSong) favoriteRemovedText else favoriteAddedText
        scope.launchLocalPlaylistMutation(
            operation = "toggleNeteaseDetailSongFavorite",
            onResult = { result ->
                if (result.isSuccess) {
                    scope.launch {
                        snackbarHostState.showNeriSnackbar(message)
                    }
                }
            }
        ) {
            if (isFavoriteSong) {
                repo.removeFromFavorites(song)
            } else {
                repo.addToFavorites(song)
            }
        }
    }
    val routeHeader = ui.header?.takeIf { header ->
        isNeteaseCollectionHeaderForRoute(
            header = header,
            playlistId = playlistId,
            playlistSource = playlistSource
        )
    }
    val displayCoverUrl = resolvePlaylistDetailCoverUrl(
        headerCoverUrl = routeHeader?.coverUrl,
        fallbackCoverUrl = initialCoverUrl
    )
    val playlistChromeColor = rememberPlaylistModernHeroBackgroundColor(
        coverUrl = displayCoverUrl,
        offlineMode = offlineMode
    )
    val searchVisible = shouldShowPlaylistSearch(
        showSearch = showSearch,
        selectionMode = selectionMode
    )
    val searchVisibilityProgress = playlistModernSearchVisibilityProgress(
        searchVisible = searchVisible,
        label = "netease-playlist-search-visibility"
    )
    val searchVisibilityEased = resolvePlaylistEasedProgress(searchVisibilityProgress)
    val playlistHeroHeight = interpolatePlaylistDp(
        start = PlaylistModernHeroHeight,
        end = PlaylistModernHeroSearchHeight,
        fraction = searchVisibilityEased
    )
    val playlistChromeCollapseProgress by remember(
        listState,
        density,
        playlistHeroHeight
    ) {
        derivedStateOf {
            resolvePlaylistChromeCollapseProgress(
                firstVisibleItemIndex = listState.firstVisibleItemIndex,
                firstVisibleItemScrollOffsetPx = listState.firstVisibleItemScrollOffset,
                expandedHeroHeightPx = with(density) {
                    playlistHeroHeight.roundToPx()
                }
            )
        }
    }
    val playlistChromeVisualProgress = resolvePlaylistEasedProgress(
        playlistChromeCollapseProgress
    )
    val dockedSearchRevealProgress by remember(listState, density) {
        derivedStateOf {
            resolvePlaylistDockedSearchRevealProgress(
                firstVisibleItemIndex = listState.firstVisibleItemIndex,
                firstVisibleItemScrollOffsetPx = listState.firstVisibleItemScrollOffset,
                revealDistancePx = with(density) {
                    PlaylistModernDockedSearchSlotHeight.roundToPx()
                }
            )
        }
    }
    val searchDockedVisualProgress = resolvePlaylistEasedProgress(
        dockedSearchRevealProgress
    )
    val dockedSearchProgress = resolvePlaylistDockedSearchSlotProgress(
        searchVisibilityProgress = searchVisibilityProgress,
        dockedRevealProgress = dockedSearchRevealProgress
    )
    val searchSlotVisible = shouldComposePlaylistSearchSlot(
        searchVisible = searchVisible,
        visibilityProgress = dockedSearchProgress
    )
    val headerSearchAlpha = resolvePlaylistHeaderSearchAlpha(
        searchVisibilityProgress = searchVisibilityProgress,
        chromeCollapseProgress = playlistChromeCollapseProgress
    )
    val headerSearchVisible = shouldComposePlaylistSearchSlot(
        searchVisible = searchVisible,
        visibilityProgress = headerSearchAlpha
    )
    val searchFieldFocusInHeader =
        headerSearchVisible && dockedSearchRevealProgress < 0.5f
    val searchFieldComposed = headerSearchVisible || searchSlotVisible
    val playlistTopBarColor = resolvePlaylistTranslucentTopBarColor(
        playlistColor = playlistChromeColor,
        collapseProgress = playlistChromeVisualProgress
    )
    val playlistTopBarContentColor = interpolatePlaylistColor(
        start = resolvePlaylistSolidTopBarContentColor(playlistChromeColor),
        end = playlistModernCollapsedTopBarContentColor(),
        fraction = playlistChromeVisualProgress
    )
    val playlistSelectionTopBarColor = resolvePlaylistSelectionTopBarColor(
        playlistColor = playlistChromeColor,
        collapseProgress = playlistChromeCollapseProgress
    )
    val playlistSelectionTopBarContentColor = resolvePlaylistSelectionTopBarContentColor(
        playlistColor = playlistChromeColor,
        collapsedContentColor = playlistModernCollapsedTopBarContentColor(),
        collapseProgress = playlistChromeCollapseProgress
    )
    val autoShowKeyboard by AppContainer.settingsRepo.autoShowKeyboardFlow.collectAsState(
        initial = false
    )
    val backgroundImageUri by AppContainer.settingsRepo.backgroundImageUriFlow.collectAsState(
        initial = null
    )
    val hasCustomBackground = backgroundImageUri != null
    LaunchedEffect(
        showSearch,
        selectionMode,
        searchFieldComposed,
        autoShowKeyboard,
        searchFieldFocusInHeader
    ) {
        if (!searchFieldComposed) return@LaunchedEffect
        val shouldAutoFocus = shouldRequestPlaylistSearchFocus(
            showSearch,
            selectionMode,
            autoShowKeyboard
        )
        val shouldTransferFocus = shouldTransferPlaylistSearchFocus(
            showSearch = showSearch,
            selectionMode = selectionMode,
            searchFieldComposed = searchFieldComposed,
            searchInputFocused = headerSearchFocused || dockedSearchFocused,
            searchQuery = searchQuery
        )
        if (!shouldAutoFocus && !shouldTransferFocus) return@LaunchedEffect
        if (shouldAutoFocus) delay(120)
        searchFocusRequester.requestFocus()
        keyboardController?.show()
    }

    val detailVisibilityState = rememberMainTabDetailVisibilityState(playlistId)
    AnimatedVisibility(
        visibleState = detailVisibilityState,
        enter = fadeIn() + slideInVertically { it / 6 },
        exit = fadeOut() + slideOutVertically { it / 6 }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent
            ) {
                val miniPlayerHeight = LocalMiniPlayerHeight.current
                Column {
                    // 顶部栏: 普通模式 / 多选模式
                    if (!selectionMode) {
                        TopAppBar(
                            title = {
                                Text(
                                    text = ui.header?.name ?: "Playlist Shuffling",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            navigationIcon = {
                                HapticIconButton(onClick = onBack) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = stringResource(R.string.cd_back)
                                    )
                                }
                            },
                            actions = {
                                HapticIconButton(onClick = {
                                    showSearch = !showSearch
                                    if (!showSearch) {
                                        searchQuery = ""
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                    }
                                }) { Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.cd_search_songs)) }

                                // 收藏按钮
                                HapticIconButton(onClick = {
                                    scope.launch {
                                        if (isFavorite) {
                                            favoriteRepo.removeFavorite(playlistId, playlistSource)
                                        } else {
                                            ui.header?.let { header ->
                                                favoriteRepo.addFavorite(
                                                    id = playlistId,
                                                    name = header.name,
                                                    coverUrl = header.coverUrl,
                                                    trackCount = header.trackCount,
                                                    source = playlistSource,
                                                    songs = ui.tracks
                                                )
                                            }
                                        }
                                    }
                                }) {
                                    Icon(
                                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = if (isFavorite) stringResource(R.string.action_unfavorite) else stringResource(R.string.action_favorite_playlist),
                                        tint = playlistTopBarContentColor
                                    )
                                }

                                if (hasDownloadManagerEntry) {
                                    HapticIconButton(onClick = { showDownloadManager = true }) {
                                        Icon(
                                            Icons.Outlined.Download,
                                            contentDescription = stringResource(R.string.cd_download_manager),
                                            tint = playlistTopBarContentColor
                                        )
                                    }
                                }
                            },
                            windowInsets = WindowInsets.statusBars,
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = playlistTopBarColor,
                                scrolledContainerColor = playlistTopBarColor,
                                titleContentColor = playlistTopBarContentColor,
                                navigationIconContentColor = playlistTopBarContentColor,
                                actionIconContentColor = playlistTopBarContentColor
                            )
                        )
                    } else {
                        val allSelected =
                            selectedIds.size == ui.tracks.size && ui.tracks.isNotEmpty()
                        TopAppBar(
                    title = {
                        Text(
                            pluralStringResource(
                                R.plurals.common_selected_count,
                                selectedIds.size,
                                selectedIds.size
                            )
                        )
                    },
                            navigationIcon = {
                                HapticIconButton(onClick = { exitSelection() }) {
                                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cd_exit_select))
                                }
                            },
                            actions = {
                                HapticIconButton(onClick = { if (allSelected) clearSelection() else selectAll() }) {
                                    Icon(
                                        imageVector = if (allSelected) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                                        contentDescription = if (allSelected) {
                                            stringResource(R.string.action_deselect_all)
                                        } else {
                                            stringResource(R.string.action_select_all)
                                        }
                                    )
                                }
                                HapticIconButton(
                                    onClick = {
                                        if (selectedIds.isNotEmpty()) showExportSheet = true
                                    },
                                    enabled = selectedIds.isNotEmpty()
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Outlined.PlaylistAdd,
                                        contentDescription = stringResource(R.string.cd_export_playlist)
                                    )
                                }
                                if (canDeleteRemoteTracks && onDeleteRemoteTracks != null) {
                                    HapticIconButton(
                                        onClick = {
                                            if (selectedIds.isNotEmpty()) showDeleteConfirmDialog = true
                                        },
                                        enabled = selectedIds.isNotEmpty()
                                    ) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = stringResource(R.string.cd_delete_selected_from_netease)
                                        )
                                    }
                                }
                                HapticIconButton(
                                    onClick = {
                                        if (selectedIds.isNotEmpty()) {
                                            val selectedSongs =
                                                ui.tracks.filter { it.id in selectedIds }
                                            showDownloadManager = true
                                            GlobalDownloadManager.startBatchDownload(
                                                context,
                                                selectedSongs
                                            )
                                            exitSelection()
                                        }
                                    },
                                    enabled = selectedIds.isNotEmpty()
                                ) {
                                    Icon(
                                        Icons.Outlined.Download,
                                        contentDescription = stringResource(R.string.cd_download_selected)
                                    )
                                }
                            },
                            windowInsets = WindowInsets.statusBars,
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = playlistSelectionTopBarColor,
                                scrolledContainerColor = playlistSelectionTopBarColor,
                                titleContentColor = playlistSelectionTopBarContentColor,
                                navigationIconContentColor = playlistSelectionTopBarContentColor,
                                actionIconContentColor = playlistSelectionTopBarContentColor
                            )
                        )
                    }

                    PlaylistModernDockedSearchSlot(
                        revealProgress = dockedSearchProgress,
                        coverUrl = displayCoverUrl,
                        offlineMode = offlineMode,
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholder = stringResource(R.string.playlist_search_hint),
                        inputState = searchInputState,
                        onFocusChanged = { dockedSearchFocused = it },
                        focusRequester = if (searchFieldFocusInHeader) {
                            null
                        } else {
                            searchFocusRequester
                        },
                        dockedProgress = searchDockedVisualProgress
                    )
                    val displayedTracks = rememberPlaylistSearchResults(
                        query = searchQuery,
                        items = ui.tracks,
                        tokens = { song -> song.playlistSearchValues(context) }
                    )
                    val trackCount = ui.header?.trackCount ?: ui.tracks.size
                    val heroTitle = ui.header?.name ?: stringResource(R.string.playlist_title)
                    val heroSubtitle = if (ui.header?.isAlbum == true) {
                        stringResource(
                            R.string.collection_track_count_format,
                            trackCount
                        )
                    } else {
                        stringResource(
                            R.string.playlist_play_count_format,
                            formatPlayCount(context, ui.header?.playCount ?: 0),
                            trackCount
                        )
                    }
                    fun playCollection(shuffle: Boolean) {
                        val startIndex = resolvePlaylistPlaybackStartIndex(
                            songCount = ui.tracks.size,
                            shuffleEnabled = shuffle,
                            randomIndex = if (ui.tracks.isEmpty()) 0 else Random.nextInt(ui.tracks.size)
                        )
                        if (startIndex < 0) return
                        PlayerManager.setShuffle(shuffle)
                        onSongClick(ui.tracks, startIndex)
                    }
                    val currentIndex = displayedTracks.indexOfFirst { it.sameIdentityAs(currentSong) }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        PlaylistModernVisualColorsProvider(
                            coverUrl = displayCoverUrl,
                            offlineMode = offlineMode
                        ) {
                            LazyColumn(
                                state = listState,
                                contentPadding = PaddingValues(
                                    bottom = 24.dp + miniPlayerHeight
                                ),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                item {
                                    PlaylistModernHeroHeader(
                                        displayName = heroTitle,
                                        coverUrl = displayCoverUrl,
                                        subtitle = heroSubtitle,
                                        offlineMode = offlineMode,
                                        height = playlistHeroHeight,
                                        coverContentDescription = heroTitle,
                                        actions = if (headerSearchVisible) {
                                            {
                                                Box(
                                                    modifier = Modifier.graphicsLayer {
                                                        alpha = headerSearchAlpha
                                                    }
                                                ) {
                                                    PlaylistModernHeroSearchField(
                                                        query = searchQuery,
                                                        onQueryChange = { searchQuery = it },
                                                        placeholder = stringResource(R.string.playlist_search_hint),
                                                        inputState = searchInputState,
                                                        onFocusChanged = { headerSearchFocused = it },
                                                        focusRequester = if (searchFieldFocusInHeader) {
                                                            searchFocusRequester
                                                        } else {
                                                            null
                                                        }
                                                    )
                                                }
                                            }
                                        } else {
                                            null
                                        }
                                    )
                                }

                            item(
                                key = PLAYLIST_ACTIONS_KEY,
                                contentType = "playlist_actions"
                            ) {
                                PlaylistModernActionSheet(
                                    coverUrl = displayCoverUrl,
                                    offlineMode = offlineMode,
                                    hasCustomBackground = hasCustomBackground
                                ) {
                                        PlaylistModernPlaybackActions(
                                            songCount = ui.tracks.size,
                                            shuffleEnabled = shuffleEnabled,
                                            repeatMode = repeatMode,
                                            onPlayInOrder = { playCollection(shuffle = false) },
                                            onShufflePlay = { playCollection(shuffle = true) },
                                            onToggleShuffle = {
                                                PlayerManager.setShuffle(!shuffleEnabled)
                                            },
                                            onCycleRepeatMode = {
                                                PlayerManager.cycleRepeatMode()
                                            },
                                            onExportToLocalPlaylist = {
                                                showExportAllSheet = true
                                            }
                                        )
                                }
                            }

                            // 状态块
                            when {
                                ui.loading && ui.tracks.isEmpty() -> {
                                    item {
                                        PlaylistModernListItemSurface(
                                            coverUrl = displayCoverUrl,
                                            offlineMode = offlineMode
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(20.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                CircularProgressIndicator()
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(stringResource(R.string.playlist_loading_content))
                                            }
                                        }
                                    }
                                }

                                ui.error != null && ui.tracks.isEmpty() -> {
                                    item {
                                        PlaylistModernListItemSurface(
                                            coverUrl = displayCoverUrl,
                                            offlineMode = offlineMode
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(20.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(
                                                    text = stringResource(R.string.playlist_load_failed_format, ui.error),
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                                Spacer(Modifier.height(8.dp))
                                                RetryChip(onRetry)
                                            }
                                        }
                                    }
                                }

                                else -> {
                                    itemsIndexed(
                                        displayedTracks,
                                        key = { _, it -> it.stableKey() }) { index, item ->
                                        PlaylistModernListItemSurface(
                                            coverUrl = displayCoverUrl,
                                            offlineMode = offlineMode
                                        ) {
                                            val isFavoriteSong = favoriteSongs.any {
                                                it.sameIdentityAs(item)
                                            }
                                            SongRow(
                                                index = index + 1,
                                                song = item,
                                                isFavorite = isFavoriteSong,
                                                onFavoriteToggle = ::toggleSongFavorite,
                                                showCover = ui.header?.isAlbum == false,
                                                selectionMode = selectionMode,
                                                selected = selectedIds.contains(item.id),
                                                onToggleSelect = { toggleSelect(item.id) },
                                                onLongPress = {
                                                    if (!selectionMode) {
                                                        selectionMode = true
                                                        selectedIds = setOf(item.id)
                                                    } else {
                                                        toggleSelect(item.id)
                                                    }
                                                },
                                                onClick = {
                                                    NPLogger.d(
                                                        "NERI-UI",
                                                        "tap song index=$index id=${item.id}"
                                                    )
                                                    val full = ui.tracks
                                                    val itemKey = item.stableKey()
                                                    val pos = full.indexOfFirst { it.stableKey() == itemKey }
                                                    if (pos >= 0) onSongClick(full, pos)
                                                },
                                                snackbarHostState = snackbarHostState,
                                                offlineMode = offlineMode,
                                                onAddToNeteasePlaylist = if (canDeleteRemoteTracks || canAddToNetease) {
                                                    {
                                                        neteaseActionSong = item
                                                        showNeteasePlaylistPicker = true
                                                        neteaseRemotePlaylists = emptyList()
                                                        neteasePlaylistsError = null
                                                        neteasePlaylistsLoading = true
                                                        scope.launch {
                                                            runCatching {
                                                                LocalPlaylistRepository.getInstance(context)
                                                                    .fetchNeteaseRemotePlaylists(AppContainer.neteaseClient)
                                                            }.onSuccess { playlists ->
                                                                neteasePlaylistsLoading = false
                                                                if (playlists.isEmpty()) {
                                                                    neteasePlaylistsError = context.getString(
                                                                        R.string.local_playlist_sync_netease_no_playlists
                                                                    )
                                                                }
                                                                neteaseRemotePlaylists = playlists
                                                            }.onFailure { error ->
                                                                neteasePlaylistsLoading = false
                                                                neteasePlaylistsError = error.message?.takeIf(String::isNotBlank)
                                                                    ?: context.getString(R.string.local_playlist_sync_netease_load_failed)
                                                            }
                                                        }
                                                    }
                                                } else null,
                                                onDeleteFromNeteasePlaylist = if (canDeleteRemoteTracks) {
                                                    {
                                                        neteaseActionSong = item
                                                        showNeteaseDeleteSongConfirm = true
                                                    }
                                                } else null
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        }

                        if (currentIndex >= 0) {
                            HapticFloatingActionButton(
                                onClick = {
                                    scope.launch {
                                        listState.animateScrollToItem(
                                            resolvePlaylistSongItemIndex(currentIndex)
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(
                                        bottom = 16.dp + miniPlayerHeight,
                                        end = 16.dp
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.PlaylistPlay,
                                    contentDescription = stringResource(R.string.cd_locate_playing)
                                )
                            }
                        }
                    }
                }

                // 单曲：添加到网易云歌单选择器（三点菜单入口）//
                if (showNeteasePlaylistPicker) {
                    val actionSong = neteaseActionSong
                    AlertDialog(
                        onDismissRequest = { showNeteasePlaylistPicker = false },
                        title = {
                            Text(stringResource(R.string.local_playlist_sync_netease_picker_title))
                        },
                        text = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (neteasePlaylistsLoading) {
                                    Text(
                                        text = stringResource(
                                            R.string.local_playlist_sync_netease_loading_playlists
                                        ),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                neteasePlaylistsError?.let { message ->
                                    Text(
                                        text = message,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                LazyColumn(modifier = Modifier.heightIn(max = 360.dp)) {
                                    itemsIndexed(
                                        items = neteaseRemotePlaylists,
                                        key = { _, playlist -> playlist.id }
                                    ) { _, playlist ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable(enabled = !neteasePlaylistsLoading && actionSong != null) {
                                                    if (actionSong == null) return@clickable
                                                    showNeteasePlaylistPicker = false
                                                    scope.launch(Dispatchers.IO) {
                                                        val result = LocalPlaylistRepository.getInstance(context)
                                                            .syncSongsToNeteasePlaylist(
                                                                client = AppContainer.neteaseClient,
                                                                targetPlaylistId = playlist.id,
                                                                songs = listOf(actionSong)
                                                            )
                                                        withContext(Dispatchers.Main) {
                                                            snackbarHostState.showNeriSnackbar(
                                                                context.getString(
                                                                    R.string.local_playlist_sync_netease_target,
                                                                    playlist.name
                                                                ) + " " + (result.message ?: context.getString(
                                                                    R.string.netease_add_song_done
                                                                ))
                                                            )
                                                        }
                                                    }
                                                }
                                                .padding(horizontal = 4.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Outlined.PlaylistAdd,
                                                contentDescription = null
                                            )
                                            Text(
                                                text = playlist.name,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {},
                        dismissButton = {
                            HapticTextButton(onClick = { showNeteasePlaylistPicker = false }) {
                                Text(stringResource(R.string.action_cancel))
                            }
                        }
                    )
                }

                // 单曲：从网易云歌单删除确认弹窗（三点菜单入口）//
                if (showNeteaseDeleteSongConfirm) {
                    val actionSong = neteaseActionSong
                    AlertDialog(
                        onDismissRequest = {
                            if (!isDeletingRemote) showNeteaseDeleteSongConfirm = false
                        },
                        confirmButton = {
                            HapticTextButton(
                                enabled = !isDeletingRemote && actionSong != null,
                                onClick = {
                                    val song = actionSong ?: return@HapticTextButton
                                    isDeletingRemote = true
                                    scope.launch {
                                        try {
                                            onDeleteRemoteTracks?.invoke(listOf(song.id))
                                            withContext(Dispatchers.Main) {
                                                AppFeedback.showToast(
                                                    context = context,
                                                    message = context.getString(R.string.netease_delete_selected_success)
                                                )
                                            }
                                        } catch (e: Exception) {
                                            NPLogger.w(
                                                "NERI-NeteaseCollection",
                                                "delete single track failed: ${e.message}"
                                            )
                                            withContext(Dispatchers.Main) {
                                                AppFeedback.showToast(
                                                    context = context,
                                                    message = context.getString(R.string.netease_delete_selected_failed)
                                                )
                                            }
                                        } finally {
                                            isDeletingRemote = false
                                            showNeteaseDeleteSongConfirm = false
                                            neteaseActionSong = null
                                        }
                                    }
                                }
                            ) {
                                Text(
                                    stringResource(R.string.action_delete),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        dismissButton = {
                            HapticTextButton(
                                enabled = !isDeletingRemote,
                                onClick = {
                                    showNeteaseDeleteSongConfirm = false
                                    neteaseActionSong = null
                                }
                            ) {
                                Text(stringResource(R.string.action_cancel))
                            }
                        },
                        title = { Text(stringResource(R.string.netease_delete_selected_title)) },
                        text = {
                            Text(stringResource(R.string.netease_delete_single_song_message))
                        }
                    )
                }

                // 删除选中歌曲确认弹窗（仅歌单创建者可见入口）//
                if (showDeleteConfirmDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            if (!isDeletingRemote) showDeleteConfirmDialog = false
                        },
                        confirmButton = {
                            HapticTextButton(
                                enabled = !isDeletingRemote,
                                onClick = {
                                    isDeletingRemote = true
                                    val idsToRemove = ui.tracks
                                        .filter { it.id in selectedIds }
                                        .map { it.id }
                                    scope.launch {
                                        try {
                                            onDeleteRemoteTracks?.invoke(idsToRemove)
                                            AppFeedback.showToast(
                                                context = context,
                                                message = context.getString(
                                                    R.string.netease_delete_selected_success
                                                )
                                            )
                                            exitSelection()
                                        } catch (e: Exception) {
                                            NPLogger.w(
                                                "NERI-NeteaseCollection",
                                                "delete selected tracks failed: ${e.message}"
                                            )
                                            AppFeedback.showToast(
                                                context = context,
                                                message = context.getString(
                                                    R.string.netease_delete_selected_failed
                                                )
                                            )
                                        } finally {
                                            isDeletingRemote = false
                                            showDeleteConfirmDialog = false
                                        }
                                    }
                                }
                            ) {
                                Text(
                                    stringResource(R.string.action_delete),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        dismissButton = {
                            HapticTextButton(
                                enabled = !isDeletingRemote,
                                onClick = { showDeleteConfirmDialog = false }
                            ) {
                                Text(stringResource(R.string.action_cancel))
                            }
                        },
                        title = { Text(stringResource(R.string.netease_delete_selected_title)) },
                        text = {
                            Text(
                                pluralStringResource(
                                    R.plurals.netease_delete_selected_message,
                                    selectedIds.size,
                                    selectedIds.size
                                )
                            )
                        }
                    )
                }

                // 导出面板 //
                if (showExportSheet) {
                    PlaylistExportSheet(
                        title = stringResource(R.string.playlist_export_to_local),
                        playlists = allPlaylists.filterNot {
                            LocalFilesPlaylist.isSystemPlaylist(it, context)
                        },
                        selectedCount = selectedIds.size,
                        onDismissRequest = { showExportSheet = false },
                        onCreateAndExport = { name ->
                            val songs = ui.tracks
                                .filter { selectedIds.contains(it.id) }
                            scope.launchLocalPlaylistMutation(
                                operation = "createPlaylistFromNetease",
                                onResult = { result ->
                                    scope.showPlaylistBatchExportCreatedResult(
                                        context = context,
                                        snackbarHostState = snackbarHostState,
                                        repository = repo,
                                        result = result
                                    )
                                }
                            ) {
                                repo.createPlaylistWithSongs(name, songs)
                            }
                        },
                        onExportToPlaylist = { playlist ->
                            val songs = ui.tracks
                                .filter { selectedIds.contains(it.id) }
                            scope.launchLocalPlaylistMutation(
                                operation = "exportSongsFromNetease",
                                onResult = { result ->
                                    scope.showPlaylistBatchExportAddedResult(
                                        context = context,
                                        snackbarHostState = snackbarHostState,
                                        repository = repo,
                                        targetPlaylistId = playlist.id,
                                        targetPlaylistName = playlist.name,
                                        result = result
                                    )
                                }
                            ) {
                                repo.addSongsToPlaylistWithResult(playlist.id, songs)
                            }
                        }
                    )
                }
                if (showExportAllSheet) {
                    PlaylistExportSheet(
                        title = stringResource(R.string.playlist_export_to_local),
                        playlists = allPlaylists.filterNot {
                            LocalFilesPlaylist.isSystemPlaylist(it, context)
                        },
                        selectedCount = ui.tracks.size,
                        onDismissRequest = { showExportAllSheet = false },
                        onCreateAndExport = { name ->
                            val songs = ui.tracks
                            scope.launchLocalPlaylistMutation(
                                operation = "createPlaylistFromNeteaseAll",
                                onResult = { result ->
                                    scope.showPlaylistBatchExportCreatedResult(
                                        context = context,
                                        snackbarHostState = snackbarHostState,
                                        repository = repo,
                                        result = result
                                    )
                                }
                            ) {
                                repo.createPlaylistWithSongs(name, songs)
                            }
                            showExportAllSheet = false
                        },
                        onExportToPlaylist = { playlist ->
                            val songs = ui.tracks
                            scope.launchLocalPlaylistMutation(
                                operation = "exportAllSongsFromNetease",
                                onResult = { result ->
                                    scope.showPlaylistBatchExportAddedResult(
                                        context = context,
                                        snackbarHostState = snackbarHostState,
                                        repository = repo,
                                        targetPlaylistId = playlist.id,
                                        targetPlaylistName = playlist.name,
                                        result = result
                                    )
                                }
                            ) {
                                repo.addSongsToPlaylistWithResult(playlist.id, songs)
                            }
                            showExportAllSheet = false
                        }
                    )
                }
                // 允许返回键优先退出多选
                BackHandler(enabled = selectionMode) { exitSelection() }

                NeriOverlaySnackbarHost(
                    hostState = snackbarHostState,
                    bottomPadding = LocalMiniPlayerHeight.current
                )
            }
        }
    }

    // 下载管理器
    if (showDownloadManager) {
        val batchDownloadProgress by AudioDownloadManager.batchProgressFlow.collectAsState()
        val downloadTasks by GlobalDownloadManager.downloadTasks.collectAsState()
        val progress = batchDownloadProgress
        BatchDownloadManagerSheet(
            batchDownloadProgress = progress,
            downloadTasks = downloadTasks,
            progressSummaryText = if (progress != null) {
                stringResource(
                    R.string.download_progress_format,
                    progress.completedSongs,
                    progress.totalSongs
                )
            } else {
                pluralStringResource(
                    R.plurals.download_tasks_count,
                    pendingTaskCount,
                    pendingTaskCount
                )
            },
            onDismiss = { showDownloadManager = false }
        )
    }
}

/* 小组件 */
@Composable
private fun RetryChip(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Text(
            stringResource(R.string.action_retry),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SongRow(
    index: Int,
    song: SongItem,
    isFavorite: Boolean,
    onFavoriteToggle: (SongItem, Boolean) -> Unit,
    showCover: Boolean,
    selectionMode: Boolean,
    selected: Boolean,
    onToggleSelect: () -> Unit,
    onLongPress: () -> Unit,
    onClick: () -> Unit,
    indexWidth: Dp = 48.dp,
    snackbarHostState: SnackbarHostState,
    offlineMode: Boolean,
    onAddToNeteasePlaylist: (() -> Unit)? = null,
    onDeleteFromNeteasePlaylist: (() -> Unit)? = null
) {
    val current by PlayerManager.currentSongFlow.collectAsState()
    val isPlaying by PlayerManager.isPlayingFlow.collectAsState()
    val isCurrentSong = current?.sameIdentityAs(song) == true
    val context = LocalContext.current
    val composeResources = LocalResources.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    context.performHapticFeedback()
                    if (selectionMode) onToggleSelect() else onClick()
                },
                onLongClick = { onLongPress() }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(indexWidth),
            contentAlignment = Alignment.Center
        ) {
            if (selectionMode) {
                Checkbox(
                    checked = selected,
                    onCheckedChange = { onToggleSelect() }
                )
            } else {
                Text(
                    text = index.toString(),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = playlistModernListTertiaryContentColor(),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip,
                    textAlign = TextAlign.Center
                )
            }
        }

        val itemContext = LocalContext.current
        val displayCoverUrl = rememberSongDisplayCoverUrl(song)
        if (showCover && !displayCoverUrl.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                AsyncImage(
                    model = offlineCachedImageRequest(
                        context = itemContext,
                        data = displayCoverUrl,
                        offlineMode = offlineMode
                    ),
                    contentDescription = song.displayName(),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            }
            Spacer(Modifier.width(12.dp))
        }

        Column(Modifier.weight(1f)) {
            Text(
                text = song.displayName(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMedium,
                color = playlistModernListPrimaryContentColor()
            )
            Text(
                text = listOfNotNull(
                    song.displayArtist().takeIf { it.isNotBlank() },
                    (song.album.takeIf { it.isNotBlank() })?.replace("Netease", "") ?: ""
                ).joinToString(" · "),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = playlistModernListSecondaryContentColor()
            )
        }

        if (isCurrentSong) {
            PlayingIndicator(
                color = MaterialTheme.colorScheme.primary,
                animate = isPlaying
            )
        } else {
            Text(
                text = formatDuration(song.durationMs),
                style = MaterialTheme.typography.bodySmall,
                color = playlistModernListSecondaryContentColor()
            )
        }

        // 更多操作菜单
        if (!selectionMode) {
            var showMoreMenu by remember { mutableStateOf(false) }
            Box {
                IconButton(
                    onClick = { showMoreMenu = true }
                ) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = stringResource(R.string.cd_more_actions),
                        tint = playlistModernListSecondaryContentColor()
                    )
                }

                DropdownMenu(
                    expanded = showMoreMenu,
                    onDismissRequest = { showMoreMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.local_playlist_play_next)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.PlaylistPlay,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            PlayerManager.addToQueueNext(song)
                            showMoreMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.playlist_add_to_end)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.PlaylistAdd,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            PlayerManager.addToQueueEnd(song)
                            showMoreMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(
                                    if (isFavorite) {
                                        R.string.favorite_remove
                                    } else {
                                        R.string.favorite_add
                                    }
                                )
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (isFavorite) {
                                    Icons.Filled.Favorite
                                } else {
                                    Icons.Outlined.FavoriteBorder
                                },
                                contentDescription = null
                            )
                        },
                        onClick = {
                            onFavoriteToggle(song, isFavorite)
                            showMoreMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_copy_song_info)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            val songInfo = "${song.displayName()}-${song.displayArtist()}"
                            scope.launch {
                                clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("text", songInfo)))
                                snackbarHostState.showNeriSnackbar(composeResources.getString(R.string.toast_copied))
                            }
                            showMoreMenu = false
                        }
                    )
                    if (onAddToNeteasePlaylist != null) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.netease_add_song_to_playlist)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.PlaylistAdd,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                showMoreMenu = false
                                onAddToNeteasePlaylist.invoke()
                            }
                        )
                    }
                    if (onDeleteFromNeteasePlaylist != null) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(R.string.netease_delete_song_from_playlist),
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showMoreMenu = false
                                onDeleteFromNeteasePlaylist.invoke()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlayingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    animate: Boolean = true
) {
    val transition = rememberInfiniteTransition(label = "playing")
    val flatHeight = 0.35f
    val transitionSpec: FiniteAnimationSpec<Float> =
        if (animate) snap() else tween(durationMillis = 180, easing = FastOutSlowInEasing)
    val animatedValues = listOf(
        transition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 300),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar1"
        ),
        transition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 350),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar2"
        ),
        transition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 400),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar3"
        )
    )
    val barHeights = listOf(
        animateFloatAsState(
            targetValue = if (animate) animatedValues[0].value else flatHeight,
            animationSpec = transitionSpec,
            label = "bar1Hold"
        ).value,
        animateFloatAsState(
            targetValue = if (animate) animatedValues[1].value else flatHeight,
            animationSpec = transitionSpec,
            label = "bar2Hold"
        ).value,
        animateFloatAsState(
            targetValue = if (animate) animatedValues[2].value else flatHeight,
            animationSpec = transitionSpec,
            label = "bar3Hold"
        ).value
    )

    val barWidth = 3.dp
    val barMaxHeight = 12.dp

    Row(
        modifier = modifier.height(barMaxHeight),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        barHeights.forEach { barHeight ->
            Box(
                Modifier
                    .width(barWidth)
                    .height(barMaxHeight * barHeight)
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
    }
}
