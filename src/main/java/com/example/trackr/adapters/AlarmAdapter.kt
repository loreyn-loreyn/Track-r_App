package com.example.trackr.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trackr.R
import com.example.trackr.models.Alarm

class AlarmAdapter(
    private val alarms: List<Alarm>,
    private val onToggle: (Alarm, Boolean) -> Unit,
    private val onDelete: (Alarm) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    inner class AlarmViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timeText: TextView = view.findViewById(R.id.timeText)
        val labelText: TextView = view.findViewById(R.id.labelText)
        val repeatText: TextView = view.findViewById(R.id.repeatText)
        val enableSwitch: Switch = view.findViewById(R.id.enableSwitch)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alarm, parent, false)
        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = alarms[position]

        holder.timeText.text = alarm.getDisplayTime()
        holder.labelText.text = alarm.label
        holder.repeatText.text = alarm.repeat
        holder.enableSwitch.isChecked = alarm.isEnabled

        // Set text color based on enabled state
        val textColor = if (alarm.isEnabled) {
            holder.itemView.context.getColor(android.R.color.black)
        } else {
            holder.itemView.context.getColor(android.R.color.darker_gray)
        }
        holder.timeText.setTextColor(textColor)
        holder.labelText.setTextColor(textColor)

        holder.enableSwitch.setOnCheckedChangeListener { _, isChecked ->
            onToggle(alarm, isChecked)
        }

        holder.deleteButton.setOnClickListener {
            onDelete(alarm)
        }
    }

    override fun getItemCount(): Int = alarms.size
}