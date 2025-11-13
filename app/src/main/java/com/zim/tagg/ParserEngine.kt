package com.zim.tagg

import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.json.JSONObject

class ParserEngine(private val client: OkHttpClient = OkHttpClient()) {

    fun search(provider: JSONObject, query: String): List<TorrentResult> {
        val url = provider.getString("searchUrl").replace("{query}", query)
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        val html = response.body?.string() ?: return emptyList()

        val doc = Jsoup.parse(html)
        val listSelector = provider.getString("listSelector")
        val elements = doc.select(listSelector)

        val results = mutableListOf<TorrentResult>()
        for (el in elements) {
            val fields = provider.getJSONObject("fields")

            val titleSel = fields.getJSONObject("title").getString("selector")
            val title = el.select(titleSel).text()

            val detailSel = fields.getJSONObject("detailUrl").getString("selector")
            val detailAttr = fields.getJSONObject("detailUrl").getString("attr")
            val detailUrl = el.select(detailSel).attr(detailAttr)

            val seedersSel = fields.getJSONObject("seeders").getString("selector")
            val seeders = el.select(seedersSel).text().toIntOrNull() ?: 0

            val leechersSel = fields.getJSONObject("leechers").getString("selector")
            val leechers = el.select(leechersSel).text().toIntOrNull() ?: 0

            val sizeSel = fields.getJSONObject("size").getString("selector")
            val size = el.select(sizeSel).text()

            results.add(TorrentResult(title, detailUrl, seeders, leechers, size))
        }
        return results
    }
}
