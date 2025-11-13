package com.zim.tagg

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject

class ProviderAdapter(
    private var providers: List<JSONObject>,
    private val onClick: (JSONObject) -> Unit
) : RecyclerView.Adapter<ProviderAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(android.R.id.text1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = providers.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val provider = providers[position]
        holder.title.text = provider.optString("name")
        holder.itemView.setOnClickListener { onClick(provider) }
    }

    fun updateData(newProviders: List<JSONObject>) {
        providers = newProviders
        notifyDataSetChanged()
    }
}
