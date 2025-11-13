package com.zim.tagg

import org.json.JSONObject

/**
 * Core torrent result model.
 * Designed to be flexible and extensible for multiple providers.
 */
data class TorrentResult(
    val title: String,
    val magnet: String? = null,
    val detailUrl: String? = null,
    val size: String? = null,
    val seeds: Int? = null,
    val leechers: Int? = null,
    val uploader: String? = null,
    val category: String? = null,
    val provider: String? = null,
    val extra: Map<String, String> = emptyMap() // catch‑all for provider‑specific fields
)

/**
 * Provider definition model.
 * Encapsulates selectors and metadata for scraping/parsing.
 */
data class ProviderConfig(
    val name: String,
    val searchUrl: String,
    val listSelector: String,
    val fields: Map<String, FieldSelector>
)

/**
 * Field selector definition.
 * Each field can specify a CSS selector and optional attribute.
 */
data class FieldSelector(
    val selector: String,
    val attr: String? = null
)

/**
 * Utility to convert JSONObject provider configs into strongly typed ProviderConfig.
 */
object ProviderConfigFactory {
    fun fromJson(json: JSONObject): ProviderConfig {
        val fieldsJson = json.optJSONObject("fields") ?: JSONObject()
        val fields = mutableMapOf<String, FieldSelector>()
        fieldsJson.keys().forEach { key ->
            val obj = fieldsJson.optJSONObject(key)
            if (obj != null) {
                fields[key] = FieldSelector(
                    selector = obj.optString("selector"),
                    attr = obj.optString("attr", null)
                )
            }
        }
        return ProviderConfig(
            name = json.optString("name"),
            searchUrl = json.optString("searchUrl"),
            listSelector = json.optString("listSelector"),
            fields = fields
        )
    }
}
