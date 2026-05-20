package com.nuvio.tv.core.torrent

fun extractTorrentTrackers(sources: List<String>?): List<String> {
    return sources
        ?.filter { it.startsWith("tracker:") }
        ?.map { it.removePrefix("tracker:") }
        ?: emptyList()
}
