package com.zim.tagg

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject

class ProviderManagerActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private var providers = JSONArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_provider_manager)

        listView = findViewById(R.id.providerList)
        findViewById<Button>(R.id.btnAddProvider).setOnClickListener { showEditDialog(null) }
        findViewById<Button>(R.id.btnReloadIntoApp).setOnClickListener {
            if (LocalProviderStore.save(this, providers)) {
                Toast.makeText(this, "Saved to local registry", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show()
            }
        }

        loadProviders()
        refreshList()
        listView.setOnItemClickListener { _, _, position, _ ->
            val p = providers.getJSONObject(position)
            showEditDialog(p)
        }
        listView.setOnItemLongClickListener { _, _, position, _ ->
            val p = providers.getJSONObject(position)
            val name = p.optString("name", "")
            AlertDialog.Builder(this)
                .setTitle("Remove provider")
                .setMessage("Delete $name?")
                .setPositiveButton("Delete") { _, _ ->
                    providers = removeIndex(providers, position)
                    refreshList()
                }.setNegativeButton("Cancel", null).show()
            true
        }
    }

    private fun loadProviders() {
        providers = LocalProviderStore.load(this) ?: JSONArray()
    }

    private fun refreshList() {
        val names = (0 until providers.length()).map { i ->
            providers.getJSONObject(i).optString("name", "unnamed")
        }
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, names)
        listView.adapter = adapter
    }

    private fun removeIndex(arr: JSONArray, idx: Int): JSONArray {
        val next = JSONArray()
        for (i in 0 until arr.length()) if (i != idx) next.put(arr.get(i))
        return next
    }

    private fun showEditDialog(existing: JSONObject?) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_provider, null)
        val name = view.findViewById<EditText>(R.id.edName)
        val searchUrl = view.findViewById<EditText>(R.id.edSearchUrl)
        val listSelector = view.findViewById<EditText>(R.id.edListSelector)
        val titleSelector = view.findViewById<EditText>(R.id.edTitleSelector)
        val detailSelector = view.findViewById<EditText>(R.id.edDetailSelector)
        val detailAttr = view.findViewById<EditText>(R.id.edDetailAttr)
        val seedsSelector = view.findViewById<EditText>(R.id.edSeedsSelector)
        val sizeSelector = view.findViewById<EditText>(R.id.edSizeSelector)

        existing?.let {
            name.setText(it.optString("name"))
            searchUrl.setText(it.optString("searchUrl"))
            listSelector.setText(it.optString("listSelector"))
            val fields = it.optJSONObject("fields") ?: JSONObject()
            titleSelector.setText(fields.optJSONObject("title")?.optString("selector"))
            detailSelector.setText(fields.optJSONObject("detailUrl")?.optString("selector"))
            detailAttr.setText(fields.optJSONObject("detailUrl")?.optString("attr"))
            seedsSelector.setText(fields.optJSONObject("seeders")?.optString("selector"))
            sizeSelector.setText(fields.optJSONObject("size")?.optString("selector"))
        }

        AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Add provider" else "Edit provider")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val fields = JSONObject().apply {
                    put("title", JSONObject().put("selector", titleSelector.text.toString().trim()))
                    put("detailUrl", JSONObject()
                        .put("selector", detailSelector.text.toString().trim())
                        .put("attr", detailAttr.text.toString().trim().ifBlank { "href" }))
                    put("seeders", JSONObject().put("selector", seedsSelector.text.toString().trim()))
                    put("size", JSONObject().put("selector", sizeSelector.text.toString().trim()))
                }
                val obj = JSONObject().apply {
                    put("name", name.text.toString().trim())
                    put("searchUrl", searchUrl.text.toString().trim())
                    put("listSelector", listSelector.text.toString().trim())
                    put("fields", fields)
                }

                if (existing == null) {
                    providers.put(obj)
                } else {
                    val newArr = JSONArray()
                    val target = existing.optString("name")
                    for (i in 0 until providers.length()) {
                        val item = providers.getJSONObject(i)
                        if (item.optString("name") == target) newArr.put(obj) else newArr.put(item)
                    }
                    providers = newArr
                }
                refreshList()
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Test") { _, _ ->
                val obj = JSONObject().apply {
                    put("name", name.text.toString().trim())
                    put("searchUrl", searchUrl.text.toString().trim())
                    put("listSelector", listSelector.text.toString().trim())
                    put("fields", JSONObject().apply {
                        put("title", JSONObject().put("selector", titleSelector.text.toString().trim()))
                        put("detailUrl", JSONObject()
                            .put("selector", detailSelector.text.toString().trim())
                            .put("attr", detailAttr.text.toString().trim().ifBlank { "href" }))
                        put("seeders", JSONObject().put("selector", seedsSelector.text.toString().trim()))
                        put("size", JSONObject().put("selector", sizeSelector.text.toString().trim()))
                    })
                }
                testProvider(obj)
            }
            .show()
    }

    private fun testProvider(provider: JSONObject) {
        val engine = ParserEngine()
        try {
            val results = engine.search(provider, "ubuntu")
            val msg = "Provider ${provider.optString("name")} -> results: ${results.size}"
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Test failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
