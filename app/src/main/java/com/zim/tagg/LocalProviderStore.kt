package com.zim.tagg

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * LocalProviderStore
 *
 * Handles persistence of provider configurations in the app's private storage.
 * This allows users to add/edit/remove providers without touching bundled assets.
 *
 * File format: JSON array of provider objects, each matching ProviderConfig schema.
 */
object LocalProviderStore {
    private const val FILE_NAME = "providers.json"

    /**
     * Load providers from local storage.
     * Returns a JSONArray of provider JSON objects, or null if none exist.
     */
    fun load(context: Context): JSONArray? {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) {
            Log.i("LocalProviderStore", "No local provider file found")
            return null
        }
        return try {
            val text = file.readText()
            JSONArray(text)
        } catch (e: Exception) {
            Log.e("LocalProviderStore", "Failed to parse providers.json", e)
            null
        }
    }

    /**
     * Save providers to local storage.
     * Accepts a JSONArray of provider JSON objects.
     * Returns true if successful, false otherwise.
     */
    fun save(context: Context, providers: JSONArray): Boolean {
        return try {
            val file = File(context.filesDir, FILE_NAME)
            file.writeText(providers.toString(2)) // pretty print with indentation
            Log.i("LocalProviderStore", "Saved ${providers.length()} providers to local storage")
            true
        } catch (e: Exception) {
            Log.e("LocalProviderStore", "Failed to save providers.json", e)
            false
        }
    }

    /**
     * Utility: Convert a list of ProviderConfig into a JSONArray for saving.
     */
    fun toJsonArray(configs: List<ProviderConfig>): JSONArray {
        val arr = JSONArray()
        configs.forEach { config ->
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
        return arr
    }
}
