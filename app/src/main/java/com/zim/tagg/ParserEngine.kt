package com.zim.tagg

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.json.JSONObject

class ParserEngine(private val client: OkHttpClient = OkHttpClient()) {

    fun search(providerJson: JSONObject, query: String): List<TorrentResult> {
        val results = mutableListOf<TorrentResult>()
        val providerConfig = ProviderConfigFactory.fromJson(providerJson)
        val name = providerConfig.name

        try {
            val url = providerConfig.searchUrl.replace("{query}", query)
            Log.d("ParserEngine", "[$name] URL: $url")

            val response = client.newCall(Request.Builder().url(url).build()).execute()
            val html = response.body?.string().orEmpty()
            if (html.isEmpty()) return emptyList()

            val doc = Jsoup.parse(html)
            val elements = doc.select(providerConfig.listSelector)
            Log.d("ParserEngine", "[$name] Elements found: ${elements.size}")

            for (el in elements) {
                try {
                    results.add(TorrentResult.fromElement(el, providerConfig))
                } catch (inner: Exception) {
                    Log.w("ParserEngine", "[$name] Element parse failed: ${inner.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("ParserEngine", "[$name] Search failed: ${e.message}")
        }

        return results
    }
}
