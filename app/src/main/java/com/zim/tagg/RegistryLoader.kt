package com.zim.tagg

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream

class RegistryLoader(private val context: Context) {

    fun loadProviders(): List<JSONObject> {
        val providers = mutableListOf<JSONObject>()

        // Try to load registry/provider.json first
        try {
            val registryInput: InputStream = context.assets.open("registry/provider.json")
            val registryText = registryInput.bufferedReader().use { it.readText() }
            val registryArray = JSONArray(registryText)

            for (i in 0 until registryArray.length()) {
                val entry = registryArray.getJSONObject(i)
                val fileName = entry.getString("file")
                try {
                    val providerJson = loadProvider(fileName)
                    providers.add(providerJson)
                } catch (e: Exception) {
                    Log.e("RegistryLoader", "Failed to load provider file: $fileName", e)
                }
            }
            return providers
        } catch (e: Exception) {
            Log.w("RegistryLoader", "No registry/provider.json found, falling back to providers folder", e)
        }

        // Fallback: auto-load all JSON files in assets/providers/
        try {
            val providerFiles = context.assets.list("providers") ?: emptyArray()
            for (fileName in providerFiles) {
                if (fileName.endsWith(".json")) {
                    try {
                        val providerJson = loadProvider(fileName)
                        providers.add(providerJson)
                    } catch (e: Exception) {
                        Log.e("RegistryLoader", "Failed to load provider file: $fileName", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("RegistryLoader", "Failed to scan providers folder", e)
        }

        return providers
    }

    private fun loadProvider(fileName: String): JSONObject {
        val input: InputStream = context.assets.open("providers/$fileName")
        val text = input.bufferedReader().use { it.readText() }
        return JSONObject(text)
    }
}
