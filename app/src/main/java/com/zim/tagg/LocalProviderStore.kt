package com.zim.tagg

import android.content.Context
import org.json.JSONArray

object LocalProviderStore {
    private const val FILE_NAME = "providers.json"

    fun save(context: Context, arr: JSONArray) {
        try {
            context.openFileOutput(FILE_NAME,
