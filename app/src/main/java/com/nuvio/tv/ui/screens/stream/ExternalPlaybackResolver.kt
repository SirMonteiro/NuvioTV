package com.nuvio.tv.ui.screens.stream

import com.nuvio.tv.core.torrent.extractTorrentTrackers

internal fun shouldResolveTorrentUrlForExternalPlayback(
    playbackInfo: StreamPlaybackInfo,
    p2pEnabled: Boolean
): Boolean {
    return playbackInfo.isTorrent &&
        p2pEnabled &&
        playbackInfo.url.isNullOrBlank()
}

internal suspend fun resolveExternalPlaybackUrl(
    playbackInfo: StreamPlaybackInfo,
    p2pEnabled: Boolean,
    startTorrentStream: suspend (
        infoHash: String,
        fileIdx: Int?,
        filename: String?,
        trackers: List<String>
    ) -> String
): String? {
    playbackInfo.url?.takeIf { it.isNotBlank() }?.let { return it }

    if (!shouldResolveTorrentUrlForExternalPlayback(playbackInfo, p2pEnabled)) {
        return null
    }

    val infoHash = playbackInfo.infoHash?.takeIf { it.isNotBlank() } ?: return null
    return startTorrentStream(
        infoHash,
        playbackInfo.fileIdx,
        playbackInfo.filename,
        extractTorrentTrackers(playbackInfo.sources)
    )
}
