package com.zim.tagg

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream

object ProviderRegistry {

    /**
     * Load providers from local store first, then assets/providers folder.
     * Returns a list of ProviderConfig objects.
     */
    fun load(context: Context): List<ProviderConfig> {
        val configs = mutableListOf<ProviderConfig>()

        // 1) Local store (user‑saved providers)
        LocalProviderStore.load(context)?.let { registry ->
            for (i in 0 until registry.length()) {
                val json = registry.getJSONObject(i)
                configs.add(ProviderConfigFactory.fromJson(json))
            }
            Log.i("ProviderRegistry", "Loaded ${configs.size} providers from local store")
            return configs
        }

        // 2) Assets/providers folder
        try {
            val providerFiles = context.assets.list("providers") ?: emptyArray()
            for (fileName in providerFiles) {
                if (fileName.endsWith(".json")) {
                    val input: InputStream = context.assets.open("providers/$fileName")
                    val text = input.bufferedReader().use { it.readText() }
                    val json = JSONObject(text)
                    configs.add(ProviderConfigFactory.fromJson(json))
                }
            }
            Log.i("ProviderRegistry", "Loaded ${configs.size} providers from assets/providers")
        } catch (e: Exception) {
            Log.e("ProviderRegistry", "Failed to load providers from assets", e)
        }

        return configs
    }

    /**
     * Save providers back to local store (optional).
     */
    fun save(context: Context, providers: List<ProviderConfig>) {
        val arr = JSONArray()
        providers.forEach { config ->
            val fieldsJson = JSONObject()
            config.fields.forEach { (key, sel) ->
                val obj = JSONObject()
                obj.put("selector", sel.selector)
                sel.attr?.let { obj.put("attr", it) }
                fieldsJson.put(key, obj)
            }
            val providerJson = JSONObject()
            providerJson.put("name", config.name)
            providerJson.put("searchUrl", config.searchUrl)
            providerJson.put("listSelector", config.listSelector)
            providerJson.put("fields", fieldsJson)
            arr.put(providerJson)
        }
        LocalProviderStore.save(context, arr)
    }
}
