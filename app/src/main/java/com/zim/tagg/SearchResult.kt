package com.zim.tagg

data class SearchResult(
    val title: String,
    val magnet: String,
    val size: String? = null,
    val seeds: Int? = null,
    val provider: String? = null
)
