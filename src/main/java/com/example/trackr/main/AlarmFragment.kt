package com.example.trackr.main

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.example.trackr.R
import com.example.trackr.adapters.AlarmAdapter
import com.example.trackr.models.Alarm
import com.example.trackr.utils.FirebaseHelper
import kotlinx.coroutines.launch
import java.util.*

class AlarmFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var addFab: FloatingActionButton
    private lateinit var adapter: AlarmAdapter
    private val alarms = mutableListOf<Alarm>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_alarm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.alarmRecyclerView)
        addFab = view.findViewById(R.id.addAlarmFab)

        setupRecyclerView()
        loadAlarms()

        addFab.setOnClickListener {
            showAddAlarmDialog()
        }
    }

    private fun setupRecyclerView() {
        adapter = AlarmAdapter(
            alarms = alarms,
            onToggle = { alarm, isEnabled -> toggleAlarm(alarm, isEnabled) },
            onDelete = { alarm -> deleteAlarm(alarm) }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun loadAlarms() {
        val userId = FirebaseHelper.getCurrentUserId() ?: return

        lifecycleScope.launch {
            val result = FirebaseHelper.getAlarms(userId)

            result.onSuccess { alarmList ->
                alarms.clear()
                alarms.addAll(alarmList)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun showAddAlarmDialog() {
        val calendar = Calendar.getInstance()

        TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                showAlarmDetailsDialog(hour, minute)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun showAlarmDetailsDialog(hour: Int, minute: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_alarm, null)
        val labelInput = dialogView.findViewById<TextInputEditText>(R.id.labelInput)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Alarm")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val label = labelInput.text.toString().trim()
                saveAlarm(hour, minute, label)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveAlarm(hour: Int, minute: Int, label: String) {
        val userId = FirebaseHelper.getCurrentUserId() ?: return

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        val alarm = Alarm(
            userId = userId,
            label = label.ifEmpty { "Alarm" },
            timeInMillis = calendar.timeInMillis,
            hour = hour,
            minute = minute
        )

        lifecycleScope.launch {
            val result = FirebaseHelper.saveAlarm(alarm)

            result.onSuccess {
                Toast.makeText(requireContext(), "Alarm saved!", Toast.LENGTH_SHORT).show()
                loadAlarms()
            }.onFailure {
                Toast.makeText(requireContext(), "Failed to save alarm", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleAlarm(alarm: Alarm, isEnabled: Boolean) {
        lifecycleScope.launch {
            FirebaseHelper.updateAlarm(alarm.id, isEnabled)
            loadAlarms()
        }
    }

    private fun deleteAlarm(alarm: Alarm) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Alarm")
            .setMessage("Are you sure you want to delete this alarm?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    FirebaseHelper.deleteAlarm(alarm.id)
                    loadAlarms()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
