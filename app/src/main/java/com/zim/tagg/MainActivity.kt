package com.zim.tagg

import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: ResultsAdapter
    private lateinit var parser: ParserEngine
    private val providerList = mutableListOf<JSONObject>()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchBox = findViewById<EditText>(R.id.searchBox)
        val resultsList = findViewById<RecyclerView>(R.id.resultsList)

        resultsList.layoutManager = LinearLayoutManager(this)
        adapter = ResultsAdapter(emptyList())
        resultsList.adapter = adapter

        parser = ParserEngine()

        // Load providers from registry or fallback
        loadProviders()

        searchBox.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val query = searchBox.text.toString().trim()
                if (query.isEmpty()) {
                    Toast.makeText(this, "Enter a search term", Toast.LENGTH_SHORT).show()
                    return@setOnEditorActionListener true
                }

                if (providerList.isEmpty()) {
                    Toast.makeText(this, "No providers loaded", Toast.LENGTH_SHORT).show()
                    return@setOnEditorActionListener true
                }

                scope.launch {
                    val allResults = mutableListOf<SearchResult>()
                    providerList.forEach { provider ->
                        launch(Dispatchers.IO) {
                            try {
                                val results = parser.search(provider, query)
                                synchronized(allResults) { allResults.addAll(results) }
                                withContext(Dispatchers.Main) {
                                    adapter.updateData(allResults)
                                }
                            } catch (e: Exception) {
                                Log.e("SearchError", "Provider failed: ${provider.optString("name")}", e)
                            }
                        }
                    }
                }

                true
            } else {
                false
            }
        }
    }

    private fun loadProviders() {
        // Try registry/provider.json first
        try {
            val input: InputStream = assets.open("registry/provider.json")
            val text = input.bufferedReader().use { it.readText() }
            val registry = JSONArray(text)
            for (i in 0 until registry.length()) {
                val entry = registry.getJSONObject(i)
                val fileName = entry.getString("file")
                val providerJson = loadProvider(fileName)
                providerList.add(providerJson)
            }
            Log.i("RegistryLoad", "Loaded ${providerList.size} providers from registry")
            return
        } catch (e: Exception) {
            Log.w("RegistryLoad", "No registry/provider.json found, falling back", e)
        }

        // Fallback: scan providers folder
        try {
            val providerFiles = assets.list("providers") ?: emptyArray()
            for (fileName in providerFiles) {
                if (fileName.endsWith(".json")) {
                    val providerJson = loadProvider(fileName)
                    providerList.add(providerJson)
                }
            }
            Log.i("RegistryLoad", "Loaded ${providerList.size} providers from providers folder")
        } catch (e: Exception) {
            Log.e("RegistryLoad", "Failed to scan providers folder", e)
        }
    }

    private fun loadProvider(fileName: String): JSONObject {
        val input: InputStream = assets.open("providers/$fileName")
        val text = input.bufferedReader().use { it.readText() }
        return JSONObject(text)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
