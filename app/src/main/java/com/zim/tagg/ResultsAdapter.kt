package com.zim.tagg

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ResultsAdapter(private var items: List<TorrentResult>) :
    RecyclerView.Adapter<ResultsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(android.R.id.text1)
        val meta: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.meta.text = buildMeta(item)

        holder.itemView.setOnClickListener {
            item.magnet?.takeIf { it.startsWith("magnet:") }?.let { magnet ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(magnet))
                holder.itemView.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<TorrentResult>) {
        items = newItems
        notifyDataSetChanged()
    }

    private fun buildMeta(item: TorrentResult): String {
        val size = item.size ?: "?"
        val seeds = item.seeds?.toString() ?: "?"
        val provider = item.provider ?: "?"
        return "Size: $size | Seeds: $seeds | Source: $provider"
    }
    }
