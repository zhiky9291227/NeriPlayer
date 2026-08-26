package moe.ouom.neriplayer.data.settings

import moe.ouom.neriplayer.ui.viewmodel.tab.NeteaseHomeSongSource

/**
 * 首页板块排序
 *
 * 用户可在 设置→个性化→首页板块排序 里调整各歌曲板块的显示顺序。
 * 顺序以逗号分隔的 source.name 列表持久化到 DataStore（经 AutoSettingsSchema 自动生成），
 * 缺失/非法的名字回退到默认顺序，保证旧版本数据或脏数据不会让首页丢板块。
 */
enum class NeteaseHomeSectionId(val songSource: NeteaseHomeSongSource) {
    PERSONAL_RADAR(NeteaseHomeSongSource.PERSONAL_RADAR),
    DAILY_RECOMMEND(NeteaseHomeSongSource.DAILY_RECOMMEND),
    PRIVATE_FM(NeteaseHomeSongSource.PRIVATE_FM),
    TOP_SOARING(NeteaseHomeSongSource.TOP_SOARING),
    PERSONALIZED_NEW_SONGS(NeteaseHomeSongSource.PERSONALIZED_NEW_SONGS),
    TOP_HOT(NeteaseHomeSongSource.TOP_HOT),
    TOP_NEW(NeteaseHomeSongSource.TOP_NEW)
}

/** 默认顺序 = 现在的固定渲染顺序：私人雷达 → 每日推荐 → 私人FM → 飙升榜 → 新歌 → 热歌 */
val DefaultNeteaseHomeSections: List<NeteaseHomeSectionId> = listOf(
    NeteaseHomeSectionId.PERSONAL_RADAR,
    NeteaseHomeSectionId.DAILY_RECOMMEND,
    NeteaseHomeSectionId.PRIVATE_FM,
    NeteaseHomeSectionId.TOP_SOARING,
    NeteaseHomeSectionId.PERSONALIZED_NEW_SONGS,
    NeteaseHomeSectionId.TOP_HOT,
    NeteaseHomeSectionId.TOP_NEW
)

fun encodeNeteaseHomeSectionOrder(order: List<NeteaseHomeSectionId>): String {
    return order.joinToString(separator = ",") { it.name }
}

fun parseNeteaseHomeSectionOrder(raw: String?): List<NeteaseHomeSectionId> {
    if (raw.isNullOrBlank()) return DefaultNeteaseHomeSections
    val parsed = raw.split(',')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .mapNotNull { name ->
            runCatching { NeteaseHomeSectionId.valueOf(name) }.getOrNull()
        }
        .distinct()
    // 丢弃未知 id 后按默认顺序补齐缺失项，保证任何持久化值都能还原出完整列表
    val missing = DefaultNeteaseHomeSections.filterNot { id -> id in parsed }
    return parsed + missing
}

fun NeteaseHomeSongSource.toHomeSectionId(): NeteaseHomeSectionId {
    return NeteaseHomeSectionId.entries.first { it.songSource == this }
}

/**
 * 把「雷达组 + 榜单组」两组板块流合并成用户自定义顺序的单列表，
 * 只保留请求顺序里实际存在的板块（未登录时每日推荐/私人FM不在列表里）。
 *
 * @param persistedOrder 用户在设置里保存并持久化的完整顺序表
 * （经 [parseNeteaseHomeSectionOrder] 解析，保证去重且包含全部板块 id）。
 * 必须显式传入——此前这里写死默认顺序，导致首页永远按出厂顺序渲染。
 */
internal fun <T> orderNeteaseHomeSections(
    radarSongSections: List<T>,
    trendingSongSections: List<T>,
    persistedOrder: List<NeteaseHomeSectionId>,
    keyOf: (T) -> NeteaseHomeSectionId
): List<T> {
    val byKey = LinkedHashMap<NeteaseHomeSectionId, T>()
    (radarSongSections.asSequence() + trendingSongSections.asSequence()).forEach { section ->
        byKey.putIfAbsent(keyOf(section), section)
    }
    // 按用户保存的顺序取板块；若顺序表意外漏掉某些 id（脏数据防御），
    // 剩余板块按原请求顺序补尾，保证任何情况下都不丢板块
    val covered = persistedOrder.toHashSet()
    val ordered = persistedOrder.mapNotNull { id -> byKey[id] }
    val uncovered = byKey.filterKeys { it !in covered }.values
    return ordered + uncovered
}
