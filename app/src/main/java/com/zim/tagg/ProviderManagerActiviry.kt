package com.zim.tagg

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject

class ProviderManagerActivity : AppCompatActivity() {

    private lateinit var providerList: MutableList<JSONObject>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProviderAdapter // assume you have a simple adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recyclerView = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@ProviderManagerActivity)
        }
        setContentView(recyclerView)

        providerList = mutableListOf()
        loadProviders()

        adapter = ProviderAdapter(providerList) { showEditProviderDialog(it) }
        recyclerView.adapter = adapter
    }

    private fun loadProviders() {
        val arr = LocalProviderStore.load(this) ?: JSONArray()
        providerList.clear()
        for (i in 0 until arr.length()) {
            providerList.add(arr.getJSONObject(i))
        }
    }

    private fun showEditProviderDialog(existing: JSONObject? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_provider, null)

        val edName = dialogView.findViewById<EditText>(R.id.edName)
        val edSearchUrl = dialogView.findViewById<EditText>(R.id.edSearchUrl)
        val edListSelector = dialogView.findViewById<EditText>(R.id.edListSelector)
        val edTitleSelector = dialogView.findViewById<EditText>(R.id.edTitleSelector)
        val edDetailSelector = dialogView.findViewById<EditText>(R.id.edDetailSelector)
        val edDetailAttr = dialogView.findViewById<EditText>(R.id.edDetailAttr)
        val edSeedsSelector = dialogView.findViewById<EditText>(R.id.edSeedsSelector)
        val edSizeSelector = dialogView.findViewById<EditText>(R.id.edSizeSelector)

        // Pre-fill if editing
        existing?.let {
            edName.setText(it.optString("name"))
            edSearchUrl.setText(it.optString("searchUrl"))
            edListSelector.setText(it.optString("listSelector"))
            val fields = it.optJSONObject("fields") ?: JSONObject()
            edTitleSelector.setText(fields.optJSONObject("title")?.optString("selector"))
            edDetailSelector.setText(fields.optJSONObject("detailUrl")?.optString("selector"))
            edDetailAttr.setText(fields.optJSONObject("detailUrl")?.optString("attr"))
            edSeedsSelector.setText(fields.optJSONObject("seeders")?.optString("selector"))
            edSizeSelector.setText(fields.optJSONObject("size")?.optString("selector"))
        }

        AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Add Provider" else "Edit Provider")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val fields = JSONObject().apply {
                    put("title", JSONObject().put("selector", edTitleSelector.text.toString()))
                    put("detailUrl", JSONObject()
                        .put("selector", edDetailSelector.text.toString())
                        .put("attr", edDetailAttr.text.toString()))
                    put("seeders", JSONObject().put("selector", edSeedsSelector.text.toString()))
                    put("size", JSONObject().put("selector", edSizeSelector.text.toString()))
                }
                val providerJson = JSONObject().apply {
                    put("name", edName.text.toString())
                    put("searchUrl", edSearchUrl.text.toString())
                    put("listSelector", edListSelector.text.toString())
                    put("fields", fields)
                }

                if (existing != null) {
                    val idx = providerList.indexOf(existing)
                    if (idx >= 0) providerList[idx] = providerJson
                } else {
                    providerList.add(providerJson)
                }

                LocalProviderStore.save(this, JSONArray(providerList))
                adapter.updateData(providerList)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
