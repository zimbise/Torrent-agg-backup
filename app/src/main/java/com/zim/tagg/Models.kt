package com.zim.tagg

/**
 * Unified result model used across MainActivity, ParserEngine, and ResultsAdapter.
 * Optional fields allow providers to omit data safely.
 */
data class TorrentResult(
    val title: String,
    val magnet: String?,      // nullable for safety — adapter checks before launching
    val size: String? = null,
    val seeds: Int? = null,
    val provider: String? = null
)
