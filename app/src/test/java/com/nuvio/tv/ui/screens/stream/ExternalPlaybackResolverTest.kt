package com.nuvio.tv.ui.screens.stream

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExternalPlaybackResolverTest {

    @Test
    fun `should resolve torrent URL only when torrent, p2p enabled, and url missing`() {
        val info = playbackInfo(url = null, isTorrent = true)
        assertTrue(needsTorrentUrlResolution(info, p2pEnabled = true))
        assertFalse(needsTorrentUrlResolution(info.copy(url = "https://video"), p2pEnabled = true))
        assertFalse(needsTorrentUrlResolution(info, p2pEnabled = false))
        assertFalse(needsTorrentUrlResolution(info.copy(isTorrent = false), p2pEnabled = true))
    }

    @Test
    fun `resolve external playback URL prefers existing URL`() = runTest {
        var called = false
        val result = resolveExternalPlaybackUrl(
            playbackInfo = playbackInfo(url = "https://existing"),
            p2pEnabled = true,
            startTorrentStream = { _, _, _, _ ->
                called = true
                "http://127.0.0.1:8090/stream"
            }
        )

        assertEquals("https://existing", result)
        assertFalse(called)
    }

    @Test
    fun `resolve external playback URL starts torrent and returns local URL`() = runTest {
        var receivedTrackers: List<String>? = null
        val result = resolveExternalPlaybackUrl(
            playbackInfo = playbackInfo(
                url = null,
                isTorrent = true,
                trackerSources = listOf("tracker:udp://a", "tracker:udp://b", "http://not-tracker")
            ),
            p2pEnabled = true,
            startTorrentStream = { _, _, _, trackers ->
                receivedTrackers = trackers
                "http://127.0.0.1:8090/stream"
            }
        )

        assertEquals("http://127.0.0.1:8090/stream", result)
        assertEquals(listOf("udp://a", "udp://b"), receivedTrackers)
    }

    @Test
    fun `resolve external playback URL returns null when not resolvable`() = runTest {
        val result = resolveExternalPlaybackUrl(
            playbackInfo = playbackInfo(url = null, isTorrent = false),
            p2pEnabled = true,
            startTorrentStream = { _, _, _, _ -> "http://127.0.0.1:8090/stream" }
        )

        assertNull(result)
    }

    private fun playbackInfo(
        url: String?,
        isTorrent: Boolean,
        trackerSources: List<String>? = null
    ) = StreamPlaybackInfo(
        url = url,
        title = "title",
        streamName = "stream",
        year = "2024",
        isExternal = false,
        isTorrent = isTorrent,
        infoHash = "abc123",
        ytId = null,
        headers = null,
        contentId = "id",
        contentType = "movie",
        contentName = "content",
        poster = null,
        backdrop = null,
        logo = null,
        videoId = "video",
        season = null,
        episode = null,
        episodeTitle = null,
        bingeGroup = null,
        filename = "file.mkv",
        videoHash = null,
        videoSize = null,
        addonName = null,
        addonLogo = null,
        streamDescription = null,
        fileIdx = 0,
        sources = trackerSources,
        contentLanguage = null
    )
}
