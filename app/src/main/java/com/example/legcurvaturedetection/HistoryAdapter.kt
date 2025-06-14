
package com.example.legcurvaturedetection

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class HistoryAdapter(
    private val items: List<HistoryItem>,
    private val onItemClick: (HistoryItem) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.nameText)
        val dateText: TextView = view.findViewById(R.id.dateText)
        val placeText: TextView = view.findViewById(R.id.placeText)
        val resultText: TextView = view.findViewById(R.id.testKeyText)
        val imageView: ImageView = view.findViewById(R.id.historyImage)

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
        holder.placeText.text = "Place: ${item.place}"
        holder.resultText.text = "Result: ${item.result}"
        item.imageUri?.let { uriString ->
            val uri = Uri.parse(uriString)
            Glide.with(holder.itemView.context)
                .load(uri)
                .placeholder(R.drawable.images) // 👈 Use a drawable you have in your res/drawable folder
                .error(R.drawable.images)
                .into(holder.imageView)
        } ?: run {
            // If there's no imageUri at all, show placeholder
            holder.imageView.setImageResource(R.drawable.images)
        }




    }


    override fun getItemCount() = items.size
}