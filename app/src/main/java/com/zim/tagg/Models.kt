package com.zim.tagg

data class TorrentResult(
    val title: String,
    val detailUrl: String,
    val seeders: Int,
    val leechers: Int,
    val size: String,
    val magnet: String? = null
)
