                                            Toast.LENGTH_SHORT
                 package com.zim.tagg

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var searchBox: EditText
    private lateinit var btnSearch: Button
    private lateinit var btnManageProviders: Button
    private lateinit var resultsList: RecyclerView
    private lateinit var adapter: ResultsAdapter
    private val providerList = mutableListOf<JSONObject>()
    private val parser = ParserEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchBox = findViewById(R.id.searchBox)
        btnSearch = findViewById(R.id.btnSearch)
        btnManageProviders = findViewById(R.id.btnManageProviders)
        resultsList = findViewById(R.id.resultsList)

        adapter = ResultsAdapter(emptyList())
        resultsList.adapter = adapter
        resultsList.layoutManager = LinearLayoutManager(this)

        loadProviders()

        // Keyboard search
        searchBox.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(searchBox.text.toString())
                true
            } else false
        }

        // Button search
        btnSearch.setOnClickListener {
            performSearch(searchBox.text.toString())
        }

        btnManageProviders.setOnClickListener {
            startActivity(Intent(this, ProviderManagerActivity::class.java))
        }
    }

    private fun performSearch(query: String) {
        val results = mutableListOf<TorrentResult>()
        for (provider in providerList) {
            results.addAll(parser.search(provider, query))
        }
        adapter.updateData(results)
    }

    private fun loadProviders() {
        providerList.clear()
        val configs = ProviderRegistry.load(this)
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
            providerList.add(providerJson)
        }
    }
}                       ).show()
                                    } else {
                                        adapter.updateData(allResults)
                                    }
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
        // 1) Try local registry first
        LocalProviderStore.load(this)?.let { registry ->
            providerList.clear()
            for (i in 0 until registry.length()) {
                providerList.add(registry.getJSONObject(i))
            }
            Log.i("RegistryLoad", "Loaded ${providerList.size} providers from local storage")
            return
        }

        // 2) Try registry/provider.json
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

        // 3) Fallback: scan providers folder
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
