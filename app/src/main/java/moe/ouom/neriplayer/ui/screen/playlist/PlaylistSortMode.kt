package moe.ouom.neriplayer.ui.screen.playlist

import androidx.annotation.StringRes
import moe.ouom.neriplayer.R
import moe.ouom.neriplayer.data.model.SongItem
import moe.ouom.neriplayer.data.model.displayName
import java.text.Collator
import java.util.Locale

/**
 * 歌单内临时排序模式（仅作用于列表展示，不改动存储顺序；拖拽排序仍写回真实顺序）。
 */
enum class PlaylistSortMode(@StringRes val labelRes: Int) {
    /** 默认：保持歌单自定义顺序（含拖拽排序结果） */
    DEFAULT(R.string.playlist_sort_default),

    /** 按添加时间从旧到新 */
    OLDEST_FIRST(R.string.playlist_sort_oldest_first),

    /** 按添加时间从新到旧 */
    NEWEST_FIRST(R.string.playlist_sort_newest_first),

    /** 按歌曲名（本地化拼音/笔画感知排序） */
    BY_NAME(R.string.playlist_sort_by_name),

    /** 按专辑名 */
    BY_ALBUM(R.string.playlist_sort_by_album),

    /** 按歌手名 */
    BY_ARTIST(R.string.playlist_sort_by_artist)
}

private val playlistNameCollator: Collator by lazy {
    Collator.getInstance(Locale.getDefault())
}

/**
 * 对歌单内歌曲应用排序。稳定排序：同 key 的歌曲保持原相对顺序。
 * addedAt=0（老数据无时间戳）的歌排最后，组内再按原顺序，避免与默认序冲突时乱跳。
 */
internal fun List<SongItem>.applyPlaylistSort(mode: PlaylistSortMode): List<SongItem> {
    if (size < 2 || mode == PlaylistSortMode.DEFAULT) return this
    return when (mode) {
        PlaylistSortMode.DEFAULT -> this

        PlaylistSortMode.OLDEST_FIRST -> sortedWith(
            compareBy<SongItem> { it.addedAt <= 0L }
                .thenBy { it.addedAt }
        )

        PlaylistSortMode.NEWEST_FIRST -> sortedWith(
            compareBy<SongItem> { it.addedAt <= 0L }
                .thenByDescending { it.addedAt }
        )

        PlaylistSortMode.BY_NAME -> sortedWith(
            compareBy<SongItem, String>(playlistNameCollator) {
                it.displayName().lowercase(Locale.getDefault())
            }
        )

        PlaylistSortMode.BY_ALBUM -> sortedWith(
            compareBy<SongItem, String>(playlistNameCollator) {
                it.album.lowercase(Locale.getDefault())
            }.thenBy(playlistNameCollator) {
                it.displayName().lowercase(Locale.getDefault())
            }
        )

        PlaylistSortMode.BY_ARTIST -> sortedWith(
            compareBy<SongItem, String>(playlistNameCollator) {
                it.artist.lowercase(Locale.getDefault())
            }.thenBy(playlistNameCollator) {
                it.displayName().lowercase(Locale.getDefault())
            }
        )
    }
}
