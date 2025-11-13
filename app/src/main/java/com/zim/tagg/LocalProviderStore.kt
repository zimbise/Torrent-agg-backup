package com.zim.tagg

import android.content.Context
import org.json.JSONArray
import java.io.File

/**
 * Handles persistence of provider configurations in internal storage.
 * Also bootstraps from assets/providers/providers.json on first run.
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

    /** Copy providers.json from assets into internal storage if not already present */
    fun bootstrapFromAssets(context: Context) {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) {
            try {
                val input = context.assets.open("providers/providers.json")
                val text = input.bufferedReader().use { it.readText() }
                val arr = JSONArray(text)
                save(context, arr)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
