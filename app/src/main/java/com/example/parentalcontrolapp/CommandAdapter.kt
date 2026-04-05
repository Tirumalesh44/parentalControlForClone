package com.example.parentalcontrolapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView

class CommandAdapter(private val apps: List<String>) :
    RecyclerView.Adapter<CommandAdapter.ViewHolder>() {

    class ViewHolder(val text: TextView) : RecyclerView.ViewHolder(text)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val text = TextView(parent.context)
        text.textSize = 16f
        text.setPadding(16,16,16,16)

        return ViewHolder(text)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.text.text = apps[position]
    }

    override fun getItemCount(): Int {
        return apps.size
    }
}