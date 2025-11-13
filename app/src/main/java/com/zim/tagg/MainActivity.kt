package com.zim.tagg

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: ResultsAdapter
    private lateinit var parser: ParserEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchBox = findViewById<EditText>(R.id.searchBox)
        val resultsList = findViewById<RecyclerView>(R.id.resultsList)

        resultsList.layoutManager = LinearLayoutManager(this)
        adapter = ResultsAdapter(emptyList())
        resultsList.adapter = adapter

        parser = ParserEngine()

        // Example: load first provider from assets
        val providerJson = loadProvider("1337x.json")
        searchBox.setOnEditorActionListener { _, _, _ ->
            val results = parser.search(providerJson, searchBox.text.toString())
            adapter.updateData(results)
            true
        }
    }

    private fun loadProvider(fileName: String): JSONObject {
        val input: InputStream = assets.open("providers/$fileName")
        val text = input.bufferedReader().use { it.readText() }
        return JSONObject(text)
    }
}
