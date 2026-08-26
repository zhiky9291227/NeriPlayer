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
 *
 * 时间序（旧→新 / 新→旧）分三种情况：
 * - 所有歌都有 addedAt：直接按 addedAt 排
 * - 部分有：有时间的按 addedAt 排在前段，无时间戳（=0）的排最后，组内保原顺序
 * - 全部没有（典型：网易云远程歌单，接口不给添加时间）：回退用 albumId 单调性近似
 *   上架先后（网易云专辑 ID 随时间递增），保证"从旧到新"至少方向正确、非恒等排序。
 */
internal fun List<SongItem>.applyPlaylistSort(mode: PlaylistSortMode): List<SongItem> {
    if (size < 2 || mode == PlaylistSortMode.DEFAULT) return this
    return when (mode) {
        PlaylistSortMode.DEFAULT -> this

        PlaylistSortMode.OLDEST_FIRST -> sortedWith(timeOrderComparator(descending = false))

        PlaylistSortMode.NEWEST_FIRST -> sortedWith(timeOrderComparator(descending = true))

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

private fun List<SongItem>.timeOrderComparator(descending: Boolean): Comparator<SongItem> {
    val hasAnyTimestamp = any { it.addedAt > 0L }
    return when {
        // 正常：存在真实添加时间戳
        hasAnyTimestamp -> compareBy { if (descending) -it.addedAt else it.addedAt }

        // 全部无时间戳（网易云远程歌单等）：用 albumId 作为上架时间的单调代理，
        // 再以曲目 ID 兜底保证结果稳定且有意义。
        else -> {
            val base = compareBy<SongItem> { it.albumId }
                .thenBy { it.id }
            if (descending) base.reversed() else base
        }
    }
}
