package com.zim.tagg

import android.content.Context
import org.json.JSONArray

/**
 * Simple persistence helper for saving/loading provider configurations
 * to the app's private storage.
 */
object LocalProviderStore {
    private const val FILE_NAME = "providers.json"

    fun save(context: Context, arr: JSONArray) {
        try {
            context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use { fos ->
                fos.write(arr.toString().toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun load(context: Context): JSONArray? {
        return try {
            val text = context.openFileInput(FILE_NAME).bufferedReader().use { it.readText() }
            JSONArray(text)
        } catch (e: Exception) {
            null
        }
    }
}
