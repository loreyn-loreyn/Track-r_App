package com.example.trackr.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.trackr.R
import com.example.trackr.models.Alarm
import com.example.trackr.models.Deadline
import com.example.trackr.utils.TimeFormatter

class SearchResultAdapter(
    private val results: List<Any>
) : RecyclerView.Adapter<SearchResultAdapter.ResultViewHolder>() {

    class ResultViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.resultTitle)
        val subtitleText: TextView = view.findViewById(R.id.resultSubtitle)
        val typeText: TextView = view.findViewById(R.id.resultType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_result, parent, false)
        return ResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        when (val item = results[position]) {
            is Alarm -> {
                holder.titleText.text = item.label
                holder.subtitleText.text = item.getDisplayTime()
                holder.typeText.text = "ALARM"
            }
            is Deadline -> {
                holder.titleText.text = item.title
                holder.subtitleText.text = TimeFormatter.formatDateTime(item.dateTimeInMillis)
                holder.typeText.text = "DEADLINE"
            }
        }
    }

    override fun getItemCount(): Int = results.size
}