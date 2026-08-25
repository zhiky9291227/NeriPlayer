package moe.ouom.neriplayer.data.settings

/**
 * 播放页「更多操作」菜单项可见性
 * 每一项对应个性化设置里的一个开关, 关闭即在播放页三点菜单中隐藏该选项
 */
data class NowPlayingMenuVisibility(
    val songInfo: Boolean = true,
    val addToNetease: Boolean = true,
    val editInfo: Boolean = true,
    val qualitySwitch: Boolean = true,
    val audioEffects: Boolean = true,
    val download: Boolean = true,
    val lyricBehavior: Boolean = true,
    val lyricFontSize: Boolean = true,
    val viewAlbum: Boolean = true,
    val share: Boolean = true,
    val playbackStats: Boolean = true,
    val listenTogether: Boolean = true,
    val deleteFromPlaylist: Boolean = false
)
