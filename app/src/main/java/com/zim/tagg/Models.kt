package com.zim.tagg

import org.json.JSONObject
import org.jsoup.nodes.Element

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
    val hash: String? = null,
    val uploadDate: String? = null,
    val extra: Map<String, String> = emptyMap()
) {
    companion object {
        fun fromElement(el: Element, provider: ProviderConfig): TorrentResult {
            val fields = provider.fields
            fun fieldText(key: String): String? =
                fields[key]?.let { sel ->
                    val nodes = el.select(sel.selector)
                    if (sel.attr != null) nodes.attr(sel.attr) else nodes.text()
                }?.takeIf { it.isNotBlank() }

            return TorrentResult(
                title = fieldText("title") ?: "Unknown Title",
                magnet = fieldText("magnet"),
                detailUrl = fieldText("detailUrl"),
                size = fieldText("size"),
                seeds = fieldText("seeders")?.toIntOrNull(),
                leechers = fieldText("leechers")?.toIntOrNull(),
                uploader = fieldText("uploader"),
                category = fieldText("category"),
                provider = provider.name,
                hash = fieldText("hash"),
                uploadDate = fieldText("uploadDate"),
                extra = fields.keys
                    .filterNot { it in setOf("title","magnet","detailUrl","size","seeders","leechers","uploader","category","hash","uploadDate") }
                    .associateWith { fieldText(it) ?: "" }
            )
        }
    }
}

data class ProviderConfig(
    val name: String,
    val searchUrl: String,
    val listSelector: String,
    val fields: Map<String, FieldSelector>
)

data class FieldSelector(
    val selector: String,
    val attr: String? = null
)

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
