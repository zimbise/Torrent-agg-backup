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

        val parts = mutableListOf<String>()
        item.size?.takeIf { it.isNotBlank() }?.let { parts.add("Size: $it") }
        item.seeds?.let { parts.add("Seeds: $it") }
        item.leechers?.let { parts.add("Leech: $it") }
        item.uploader?.takeIf { it.isNotBlank() }?.let { parts.add("By: $it") }
        item.category?.takeIf { it.isNotBlank() }?.let { parts.add("Cat: $it") }
        item.uploadDate?.takeIf { it.isNotBlank() }?.let { parts.add("Date: $it") }

        when {
            !item.magnet.isNullOrBlank() -> parts.add("Magnet ✓")
            !item.detailUrl.isNullOrBlank() -> parts.add("Link ✓")
        }

        holder.line2.text = parts.joinToString(" | ")
    }

    fun updateData(newResults: List<TorrentResult>) {
        results = newResults
        notifyDataSetChanged()
    }
}
