package com.zim.tagg

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray

class MainActivity : AppCompatActivity() {

    private lateinit var resultsRecycler: RecyclerView
    private lateinit var resultsAdapter: ResultsAdapter
    private val resultsList = mutableListOf<TorrentResult>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bootstrap providers.json from assets if needed
        LocalProviderStore.bootstrapFromAssets(this)

        resultsRecycler = findViewById(R.id.resultsRecycler)
        resultsRecycler.layoutManager = LinearLayoutManager(this)
        resultsAdapter = ResultsAdapter(resultsList)
        resultsRecycler.adapter = resultsAdapter

        val searchBox = findViewById<EditText>(R.id.searchBox)
        val searchButton = findViewById<Button>(R.id.searchButton)

        searchButton.setOnClickListener {
            val query = searchBox.text.toString().trim()
            if (query.isNotEmpty()) {
                runSearch(query)
            }
        }
    }

    private fun runSearch(query: String) {
        resultsList.clear()

        val arr: JSONArray = LocalProviderStore.load(this) ?: JSONArray()

        for (i in 0 until arr.length()) {
            val providerJson = arr.getJSONObject(i)
            val providerCfg = ProviderConfigFactory.fromJson(providerJson)
            val engine = ParserEngine(providerCfg)
            val providerResults = engine.search(query)
            resultsList.addAll(providerResults)
        }

        resultsAdapter.updateData(resultsList)
    }
}
