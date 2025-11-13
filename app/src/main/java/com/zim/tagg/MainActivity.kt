package com.zim.tagg

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

        // Bind views
        searchBox = findViewById(R.id.searchBox)
        btnSearch = findViewById(R.id.btnSearch)
        btnManageProviders = findViewById(R.id.btnManageProviders)
        resultsList = findViewById(R.id.resultsList)

        // Setup RecyclerView
        adapter = ResultsAdapter(emptyList())
        resultsList.adapter = adapter
        resultsList.layoutManager = LinearLayoutManager(this)

        // Load providers
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

        // Manage providers
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
        configs.forEach { config: ProviderConfig ->
            val fieldsJson = JSONObject()
            config.fields.forEach { (key: String, sel: FieldSelector) ->
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
        Log.i("MainActivity", "Loaded ${providerList.size} providers")
    }
}
