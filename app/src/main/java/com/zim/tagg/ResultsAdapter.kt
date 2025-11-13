package com.zim.tagg

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ResultsAdapter(
    private var results: List<TorrentResult>
) : RecyclerView.Adapter<ResultsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val line1: TextView = view.findViewById(android.R.id.text1)
        val line2: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = results.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = results[position]

        holder.line1.text = item.title

        val details = mutableListOf<String>()
        if (item.size.isNotBlank()) details.add("Size: ${item.size}")
        if (item.seeders.isNotBlank()) details.add("Seeds: ${item.seeders}")
        if (item.detailUrl.isNotBlank()) details.add("Link: ${item.detailUrl}")

        holder.line2.text = details.joinToString(" | ")
    }

    fun updateData(newResults: List<TorrentResult>) {
        results = newResults
        notifyDataSetChanged()
    }
}
