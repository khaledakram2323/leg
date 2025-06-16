package com.example.legcurvaturedetection

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class HistoryAdapter(
    private val items: List<HistoryItem>,
    private val onItemClick: (HistoryItem) -> Unit,
    private val onDeleteClick: (HistoryItem) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.nameText)
        val dateText: TextView = view.findViewById(R.id.dateText)
        val resultText: TextView = view.findViewById(R.id.testKeyText)
        val imageView: ImageView = view.findViewById(R.id.historyImage)
        val deleteIcon: ImageView = view.findViewById(R.id.deleteicon)

        init {
            itemView.setOnClickListener {
                onItemClick(items[adapterPosition])
            }
            deleteIcon.setOnClickListener {
                onDeleteClick(items[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_item, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = items[position]

        holder.nameText.text = item.name
        holder.dateText.text = "Date: ${item.date}"
        holder.resultText.text = "Result: ${item.result}"
        item.imageUri?.let { uriString ->
            android.util.Log.d("GlideDebug", "Loading image: $uriString")
            Glide.with(holder.itemView.context)
                .load(uriString)
                .placeholder(R.drawable.images)
                .error(R.drawable.images)
                .centerCrop()
                .into(holder.imageView)
        } ?: run {
            android.util.Log.d("GlideDebug", "No image URI for item: ${item.name}")
            holder.imageView.setImageResource(R.drawable.images)
        }
    }

    override fun getItemCount() = items.size
}