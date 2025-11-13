package com.zim.tagg

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object LocalProviderStore {
    private const val FILE_NAME = "providers.json"

    fun load(context: Context): JSONArray? {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return null
        return try {
            JSONArray(file.readText())
        } catch (_: Exception) {
            null
        }
    }

    fun save(context: Context, providers: JSONArray): Boolean {
        return try {
            val file = File(context.filesDir, FILE_NAME)
            file.writeText(providers.toString())
            true
        } catch (_: Exception) {
            false
        }
    }

    fun upsert(context: Context, provider: JSONObject): Boolean {
        val current = load(context) ?: JSONArray()
        val name = provider.optString("name", "")
        var replaced = false
        for (i in 0 until current.length()) {
            val item = current.getJSONObject(i)
            if (item.optString("name") == name) {
                current.put(i, provider)
                replaced = true
                break
            }
        }
        if (!replaced) current.put(provider)
        return save(context, current)
    }

    fun remove(context: Context, name: String): Boolean {
        val current = load(context) ?: return false
        val next = JSONArray()
        for (i in 0 until current.length()) {
            val item = current.getJSONObject(i)
            if (item.optString("name") != name) next.put(item)
        }
        return save(context, next)
    }
}
