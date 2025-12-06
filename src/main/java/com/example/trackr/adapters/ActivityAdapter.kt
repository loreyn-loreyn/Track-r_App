package com.example.trackr.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trackr.R
import com.example.trackr.models.Activity

class ActivityAdapter(
    private val activities: List<Activity>,
    private val onToggleComplete: (Activity, Boolean) -> Unit
) : RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder>() {

    inner class ActivityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkbox: CheckBox = view.findViewById(R.id.activityCheckbox)
        val titleText: TextView = view.findViewById(R.id.activityTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity, parent, false)
        return ActivityViewHolder(view)
    }

    override fun onBindViewHolder(holder: ActivityViewHolder, position: Int) {
        val activity = activities[position]

        holder.checkbox.isChecked = activity.isCompleted
        holder.titleText.text = activity.title

        // Set strikethrough if completed
        if (activity.isCompleted) {
            holder.titleText.paintFlags = holder.titleText.paintFlags or
                    android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.titleText.paintFlags = holder.titleText.paintFlags and
                    android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            onToggleComplete(activity, isChecked)
        }
    }

    override fun getItemCount(): Int = activities.size
}