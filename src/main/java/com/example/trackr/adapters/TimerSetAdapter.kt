package com.example.trackr.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trackr.R
import com.example.trackr.models.TimerSet
import com.example.trackr.utils.TimeFormatter

class TimerSetAdapter(
    private val timerSets: List<TimerSet>,
    private val onStart: (TimerSet) -> Unit,
    private val onPause: (TimerSet) -> Unit,
    private val onRestart: (TimerSet) -> Unit,
    private val onLongPress: (TimerSet) -> Unit
) : RecyclerView.Adapter<TimerSetAdapter.TimerViewHolder>() {

    class TimerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val labelText: TextView = view.findViewById(R.id.timerLabel)
        val timeText: TextView = view.findViewById(R.id.timerTime)
        val presetTimeText: TextView = view.findViewById(R.id.presetTimeText)
        val startButton: Button = view.findViewById(R.id.startButton)
        val pauseButton: Button = view.findViewById(R.id.pauseButton)
        val restartButton: Button = view.findViewById(R.id.restartButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_timer_set, parent, false)
        return TimerViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimerViewHolder, position: Int) {
        val timerSet = timerSets[position]

        holder.labelText.text = timerSet.label
        holder.timeText.text = TimeFormatter.formatTimerDisplay(timerSet.remainingMillis)
        holder.presetTimeText.text = "Preset: ${TimeFormatter.formatTimerDisplay(timerSet.durationMillis)}"

        // Long press listener
        holder.itemView.setOnLongClickListener {
            onLongPress(timerSet)
            true
        }

        // Button visibility based on timer state
        when {
            !timerSet.isActive && !timerSet.isPaused -> {
                holder.startButton.visibility = View.VISIBLE
                holder.pauseButton.visibility = View.GONE
                holder.restartButton.visibility = View.GONE
            }
            timerSet.isActive -> {
                holder.startButton.visibility = View.GONE
                holder.pauseButton.visibility = View.VISIBLE
                holder.restartButton.visibility = View.VISIBLE
            }
            timerSet.isPaused -> {
                holder.startButton.visibility = View.VISIBLE
                holder.pauseButton.visibility = View.GONE
                holder.restartButton.visibility = View.VISIBLE
            }
        }

        holder.startButton.setOnClickListener { onStart(timerSet) }
        holder.pauseButton.setOnClickListener { onPause(timerSet) }
        holder.restartButton.setOnClickListener { onRestart(timerSet) }
    }

    override fun getItemCount(): Int = timerSets.size
}