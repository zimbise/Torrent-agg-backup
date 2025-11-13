package com.zim.tagg

import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.json.JSONObject

class ParserEngine(private val client: OkHttpClient = OkHttpClient()) {

    fun search(provider: JSONObject, query: String): List<TorrentResult> {
        val results = mutableListOf<TorrentResult>()

        try {
            // Build search URL
            val url = provider.getString("searchUrl").replace("{query}", query)
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val html = response.body?.string() ?: return emptyList()

            // Parse HTML
            val doc = Jsoup.parse(html)
            val listSelector = provider.optString("listSelector")
            val elements = doc.select(listSelector)

            val fields = provider.getJSONObject("fields")

            for (el in elements) {
                try {
                    // Title
                    val titleSel = fields.getJSONObject("title").getString("selector")
                    val title = el.select(titleSel).text().ifBlank { "Unknown Title" }

                    // Magnet or detail URL
                    val detailSel = fields.getJSONObject("detailUrl").getString("selector")
                    val detailAttr = fields.getJSONObject("detailUrl").optString("attr", "href")
                    val detailUrl = el.select(detailSel).attr(detailAttr)

                    // Seeds
                    val seedersSel = fields.optJSONObject("seeders")?.optString("selector")
                    val seeds = seedersSel?.let { el.select(it).text().toIntOrNull() } ?: 0

                    // Size
                    val sizeSel = fields.optJSONObject("size")?.optString("selector")
                    val size = sizeSel?.let { el.select(it).text() } ?: "?"

                    // Provider name
                    val providerName = provider.optString("name", "unknown")

                    results.add(
                        TorrentResult(
                            title = title,
                            magnet = detailUrl,   // treat detailUrl as magnet or detail link
                            size = size,
                            seeds = seeds,
                            provider = providerName
                        )
                    )
                } catch (inner: Exception) {
                    // Skip bad element but keep going
                    continue
                }
            }
        } catch (e: Exception) {
            // Log and return empty list on failure
            e.printStackTrace()
        }

        return results
    }
}
