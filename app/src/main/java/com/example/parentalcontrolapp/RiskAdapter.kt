package com.example.parentalcontrolapp

import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RiskAdapter(private val items: List<String>) :
    RecyclerView.Adapter<RiskAdapter.ViewHolder>() {

    class ViewHolder(val text: TextView) :
        RecyclerView.ViewHolder(text)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val text = TextView(parent.context)
        text.textSize = 16f
        text.setPadding(16,16,16,16)

        return ViewHolder(text)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.text.text = items[position]
    }

    override fun getItemCount(): Int {
        return items.size
    }
}