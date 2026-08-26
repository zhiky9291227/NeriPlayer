package moe.ouom.neriplayer.data.settings

import moe.ouom.neriplayer.ui.viewmodel.tab.NeteaseHomeSongSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private data class FakeSection(val source: NeteaseHomeSongSource)

class HomeSectionOrderTest {

    @Test
    fun `default order matches current fixed home rendering`() {
        assertEquals(
            listOf(
                NeteaseHomeSectionId.PERSONAL_RADAR,
                NeteaseHomeSectionId.DAILY_RECOMMEND,
                NeteaseHomeSectionId.PRIVATE_FM,
                NeteaseHomeSectionId.TOP_SOARING,
                NeteaseHomeSectionId.PERSONALIZED_NEW_SONGS,
                NeteaseHomeSectionId.TOP_HOT,
                NeteaseHomeSectionId.TOP_NEW
            ),
            DefaultNeteaseHomeSections
        )
    }

    @Test
    fun `encode and parse round trip keeps custom order`() {
        val custom = listOf(
            NeteaseHomeSectionId.DAILY_RECOMMEND,
            NeteaseHomeSectionId.PRIVATE_FM,
            NeteaseHomeSectionId.PERSONAL_RADAR,
            NeteaseHomeSectionId.TOP_NEW,
            NeteaseHomeSectionId.TOP_HOT,
            NeteaseHomeSectionId.PERSONALIZED_NEW_SONGS,
            NeteaseHomeSectionId.TOP_SOARING
        )
        assertEquals(
            custom,
            parseNeteaseHomeSectionOrder(encodeNeteaseHomeSectionOrder(custom))
        )
    }

    @Test
    fun `parse drops unknown ids and appends missing ones in default position`() {
        // TOP_SOARING 保留在前（合法 id），DAILY_RECOMMEND 缺失 → 按默认顺序补到末尾
        val restored = parseNeteaseHomeSectionOrder("TOP_SOARING,TOP_HOT,BOGUS")
        assertEquals(
            listOf(
                NeteaseHomeSectionId.TOP_SOARING,
                NeteaseHomeSectionId.TOP_HOT,
                NeteaseHomeSectionId.PERSONAL_RADAR,
                NeteaseHomeSectionId.DAILY_RECOMMEND,
                NeteaseHomeSectionId.PRIVATE_FM,
                NeteaseHomeSectionId.PERSONALIZED_NEW_SONGS,
                NeteaseHomeSectionId.TOP_NEW
            ),
            restored
        )
    }

    @Test
    fun `parse blank or garbage value falls back to defaults`() {
        assertEquals(DefaultNeteaseHomeSections, parseNeteaseHomeSectionOrder(null))
        assertEquals(DefaultNeteaseHomeSections, parseNeteaseHomeSectionOrder(""))
        assertEquals(DefaultNeteaseHomeSections, parseNeteaseHomeSectionOrder("  "))
    }

    @Test
    fun `ordering merges radar and trending sections by persisted order`() {
        val radar = listOf(
            FakeSection(NeteaseHomeSongSource.PERSONAL_RADAR),
            FakeSection(NeteaseHomeSongSource.DAILY_RECOMMEND),
            FakeSection(NeteaseHomeSongSource.PRIVATE_FM)
        )
        val trending = listOf(
            FakeSection(NeteaseHomeSongSource.TOP_SOARING),
            FakeSection(NeteaseHomeSongSource.PERSONALIZED_NEW_SONGS),
            FakeSection(NeteaseHomeSongSource.TOP_HOT),
            FakeSection(NeteaseHomeSongSource.TOP_NEW)
        )
        val merged = orderNeteaseHomeSections(radar, trending) { it.source.toHomeSectionId() }
        assertEquals(
            DefaultNeteaseHomeSections.map { it.songSource },
            merged.map { it.source }
        )

        // 未登录：radar 组只剩私人雷达，其余板块照常按默认顺序输出
        val anonymous = orderNeteaseHomeSections(
            radarSongSections = listOf(FakeSection(NeteaseHomeSongSource.PERSONAL_RADAR)),
            trendingSongSections = trending
        ) { it.source.toHomeSectionId() }
        assertEquals(5, anonymous.size)
        assertTrue(anonymous.none { it.source == NeteaseHomeSongSource.DAILY_RECOMMEND })
        assertTrue(anonymous.none { it.source == NeteaseHomeSongSource.PRIVATE_FM })
    }
}
