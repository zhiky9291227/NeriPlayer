package moe.ouom.neriplayer.data.local.playlist.system

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
 * File: moe.ouom.neriplayer.data.local.playlist.system/SystemPlaylistSongDeduper
 * Updated: 2026/3/23
 */

import moe.ouom.neriplayer.data.local.media.LocalSongSupport
import moe.ouom.neriplayer.data.model.SongIdentity
import moe.ouom.neriplayer.data.model.identity
import moe.ouom.neriplayer.data.model.SongItem

internal fun List<SongItem>.distinctSystemSongs(): List<SongItem> {
    if (size < 2) return this

    return SystemPlaylistSongDeduper(size)
        .apply { addAll(this@distinctSystemSongs) }
        .songs()
}

internal class SystemPlaylistSongDeduper(expectedSongCount: Int) {
    private val initialCapacity = expectedSongCount.coerceIn(0, MAX_INITIAL_CAPACITY)
    private val distinct = ArrayList<SongItem>(initialCapacity)
    private val seenIdentities = HashSet<SongIdentity>(initialCapacity)
    private val seenLocalKeys = HashSet<String>()
    /** 已保留条目的可靠文件路径(绝对路径);content:// 别名条目无路径,靠元数据合并 */
    private val seenReferences = HashSet<String>()
    /** 无可靠路径条目占用的去重 key(content:// 别名) — 路径条目与之碰撞时仍按同一首歌合并 */
    private val seenContentOnlyLocalKeys = HashSet<String>()

    fun addAll(songs: Iterable<SongItem>) {
        songs.forEach(::add)
    }

    fun songs(): List<SongItem> = distinct

    fun takeSongs(): MutableList<SongItem> = distinct

    private fun add(song: SongItem) {
        val identity = song.identity()
        if (identity in seenIdentities) {
            return
        }
        val localKeys = LocalSongSupport.localDuplicateKeys(
            song = song,
            includeMetadataFallback = true
        )
        val songReference = LocalSongSupport.primaryLocalReference(song)
        if (localKeys.none(seenLocalKeys::contains)) {
            distinct += song
            seenIdentities += identity
            seenLocalKeys += localKeys
            if (songReference == null) {
                seenContentOnlyLocalKeys += localKeys
            } else {
                seenReferences += songReference
            }
            return
        }
        // 元数据兜底碰撞:
        // - 本歌无可靠文件路径 → content:// 别名,视为已见歌曲的重复,合并(维持原行为)
        // - 本歌有路径但路径已见过 → 同一文件的重复条目,合并
        // - 本歌有路径且路径未见过 → 与已见条目(可能是 content 别名)疑似同一首但实体不同:
        //   为维持 content↔路径 合并语义,只要碰撞 key 来自无路径条目就合并;
        //   碰撞 key 全部来自有路径条目时,是两个物理文件,保留
        //   (否则每次 normalize 都确定性删歌,BUG-2:酷安用户 315 首固定被删 11 首)
        if (songReference == null) {
            return
        }
        if (songReference in seenReferences) {
            return
        }
        val collisionWithContentOnly = localKeys.any { it in seenContentOnlyLocalKeys }
        if (!collisionWithContentOnly) {
            distinct += song
            seenIdentities += identity
            seenLocalKeys += localKeys
            seenReferences += songReference
        }
    }

    private companion object {
        const val MAX_INITIAL_CAPACITY = 4_096
    }
}
