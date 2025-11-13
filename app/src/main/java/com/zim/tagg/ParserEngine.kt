package com.zim.tagg

import org.json.JSONObject
import org.jsoup.Jsoup

class ParserEngine(private val provider: ProviderConfig) {

    fun search(query: String): List<TorrentResult> {
        val results = mutableListOf<TorrentResult>()

        try {
            val url = provider.searchUrl.replace("{query}", query)
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Android) Tagg/1.0")
                .timeout(20_000)
                .get()

            val rows = doc.select(provider.listSelector)
            for (row in rows) {
                val item = TorrentResult.fromElement(row, provider)
                // Minimal validity check: must have a title
                if (item.title.isNotBlank()) {
                    results.add(item)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return results
    }

    companion object {
        fun fromJson(json: JSONObject): ParserEngine {
            val cfg = ProviderConfigFactory.fromJson(json)
            return ParserEngine(cfg)
        }
    }
}
