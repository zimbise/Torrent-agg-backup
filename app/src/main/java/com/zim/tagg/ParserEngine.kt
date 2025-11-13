package com.zim.tagg

import org.json.JSONObject
import org.jsoup.Jsoup

class ParserEngine(private val provider: JSONObject) {

    fun search(query: String): List<TorrentResult> {
        val results = mutableListOf<TorrentResult>()

        try {
            val searchUrl = provider.optString("searchUrl").replace("{query}", query)
            val doc = Jsoup.connect(searchUrl).get()
            val listSelector = provider.optString("listSelector")

            val rows = doc.select(listSelector)
            val fields = provider.optJSONObject("fields") ?: JSONObject()

            for (row in rows) {
                val titleSel = fields.optJSONObject("title")?.optString("selector") ?: ""
                val detailSel = fields.optJSONObject("detailUrl")?.optString("selector") ?: ""
                val detailAttr = fields.optJSONObject("detailUrl")?.optString("attr") ?: "href"
                val seedsSel = fields.optJSONObject("seeders")?.optString("selector") ?: ""
                val sizeSel = fields.optJSONObject("size")?.optString("selector") ?: ""

                val title = row.select(titleSel).text()
                val detailUrl = row.select(detailSel).attr(detailAttr)
                val seeders = row.select(seedsSel).text()
                val size = row.select(sizeSel).text()

                if (title.isNotBlank()) {
                    results.add(TorrentResult(title, detailUrl, seeders, size))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return results
    }
}
