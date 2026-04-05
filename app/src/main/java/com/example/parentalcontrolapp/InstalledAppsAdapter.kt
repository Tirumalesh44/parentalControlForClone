package com.example.parentalcontrolapp

import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

class InstalledAppsAdapter(
    private val apps: List<Map<String,String>>,
    private val blocked: List<String>,
    private val onBlock:(String)->Unit,
    private val onUnblock:(String)->Unit
): RecyclerView.Adapter<InstalledAppsAdapter.ViewHolder>() {

    class ViewHolder(val layout: LinearLayout):RecyclerView.ViewHolder(layout)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val layout = LinearLayout(parent.context)
        layout.orientation = LinearLayout.HORIZONTAL

        val text = TextView(parent.context)
        text.layoutParams = LinearLayout.LayoutParams(0,
            LinearLayout.LayoutParams.WRAP_CONTENT,1f)

        val btn = Button(parent.context)

        layout.addView(text)
        layout.addView(btn)

        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val text = holder.layout.getChildAt(0) as TextView
        val btn = holder.layout.getChildAt(1) as Button

        val app = apps[position]

        val name = app["name"]!!
        val pkg = app["package"]!!

        text.text = name

        if(blocked.contains(pkg)){
            btn.text = "UNBLOCK"
            btn.setOnClickListener{ onUnblock(pkg) }
        }else{
            btn.text = "BLOCK"
            btn.setOnClickListener{ onBlock(pkg) }
        }
    }

    override fun getItemCount(): Int = apps.size
}