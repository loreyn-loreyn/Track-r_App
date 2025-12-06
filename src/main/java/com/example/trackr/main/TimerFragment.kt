package com.example.trackr.main

import android.app.TimePickerDialog
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.trackr.R
import com.example.trackr.adapters.TimerSetAdapter
import com.example.trackr.models.TimerSet
import com.example.trackr.utils.FirebaseHelper
import kotlinx.coroutines.launch
import java.util.*

class TimerFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var addFab: FloatingActionButton
    private lateinit var adapter: TimerSetAdapter

    private val timerSets = mutableListOf<TimerSet>()
    private val activeTimers = mutableMapOf<String, CountDownTimer>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_timer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.timerRecyclerView)
        addFab = view.findViewById(R.id.addTimerFab)

        setupRecyclerView()

        addFab.setOnClickListener {
            showAddTimerDialog()
        }
    }

    private fun setupRecyclerView() {
        adapter = TimerSetAdapter(
            timerSets = timerSets,
            onStart = { timerSet -> startTimer(timerSet) },
            onPause = { timerSet -> pauseTimer(timerSet) },
            onRestart = { timerSet -> restartTimer(timerSet) },
            onLongPress = { timerSet -> showTimerOptions(timerSet) }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun showAddTimerDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_timer, null)
        val labelInput = dialogView.findViewById<EditText>(R.id.timerLabelInput)
        val hoursPicker = dialogView.findViewById<NumberPicker>(R.id.hoursPicker)
        val minutesPicker = dialogView.findViewById<NumberPicker>(R.id.minutesPicker)
        val secondsPicker = dialogView.findViewById<NumberPicker>(R.id.secondsPicker)

        // Setup pickers
        hoursPicker.minValue = 0
        hoursPicker.maxValue = 99
        hoursPicker.value = 0

        minutesPicker.minValue = 0
        minutesPicker.maxValue = 59
        minutesPicker.value = 0

        secondsPicker.minValue = 0
        secondsPicker.maxValue = 59
        secondsPicker.value = 0

        AlertDialog.Builder(requireContext())
            .setTitle("Add Timer")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val label = labelInput.text.toString().trim()
                val hours = hoursPicker.value
                val minutes = minutesPicker.value
                val seconds = secondsPicker.value

                val totalMillis = (hours * 3600 + minutes * 60 + seconds) * 1000L

                if (totalMillis > 0) {
                    addTimerSet(label.ifEmpty { "Timer" }, totalMillis)
                } else {
                    Toast.makeText(requireContext(), "Please set a valid time", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addTimerSet(label: String, durationMillis: Long) {
        val userId = FirebaseHelper.getCurrentUserId() ?: return

        val timerSet = TimerSet(
            id = UUID.randomUUID().toString(),
            label = label,
            durationMillis = durationMillis,
            remainingMillis = durationMillis,
            userId = userId  // Add userId
        )

        // ADD THIS: Save to Firebase
        lifecycleScope.launch {
            val result = FirebaseHelper.saveTimerSet(timerSet)

            result.onSuccess {
                timerSets.add(timerSet)
                adapter.notifyItemInserted(timerSets.size - 1)
                Toast.makeText(requireContext(), "Timer added!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Load from Firebase on start
    private fun loadTimers() {
        val userId = FirebaseHelper.getCurrentUserId() ?: return

        lifecycleScope.launch {
            val result = FirebaseHelper.getTimerSets(userId)

            result.onSuccess { timerList ->
                timerSets.clear()
                timerSets.addAll(timerList)
                adapter.notifyDataSetChanged()
            }
        }
    }
    private fun startTimer(timerSet: TimerSet) {
        val index = timerSets.indexOf(timerSet)
        if (index == -1) return

        val timer = object : CountDownTimer(timerSet.remainingMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerSets[index] = timerSet.copy(
                    isActive = true,
                    isPaused = false,
                    remainingMillis = millisUntilFinished
                )
                adapter.notifyItemChanged(index)
            }

            override fun onFinish() {
                timerSets[index] = timerSet.copy(
                    isActive = false,
                    isPaused = false,
                    remainingMillis = 0
                )
                adapter.notifyItemChanged(index)
                Toast.makeText(requireContext(), "${timerSet.label} finished!", Toast.LENGTH_SHORT).show()
            }
        }.start()

        activeTimers[timerSet.id] = timer

        timerSets[index] = timerSet.copy(isActive = true, isPaused = false)
        adapter.notifyItemChanged(index)
    }

    private fun pauseTimer(timerSet: TimerSet) {
        activeTimers[timerSet.id]?.cancel()
        activeTimers.remove(timerSet.id)

        val index = timerSets.indexOf(timerSet)
        if (index != -1) {
            timerSets[index] = timerSet.copy(isActive = false, isPaused = true)
            adapter.notifyItemChanged(index)
        }
    }

    private fun restartTimer(timerSet: TimerSet) {
        activeTimers[timerSet.id]?.cancel()
        activeTimers.remove(timerSet.id)

        val index = timerSets.indexOf(timerSet)
        if (index != -1) {
            timerSets[index] = timerSet.copy(
                isActive = false,
                isPaused = false,
                remainingMillis = timerSet.durationMillis
            )
            adapter.notifyItemChanged(index)
        }
    }

    private fun showTimerOptions(timerSet: TimerSet) {
        val options = arrayOf("Edit", "Delete")

        AlertDialog.Builder(requireContext())
            .setTitle(timerSet.label)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editTimer(timerSet)
                    1 -> deleteTimer(timerSet)
                }
            }
            .show()
    }

    private fun editTimer(timerSet: TimerSet) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_timer, null)
        val labelInput = dialogView.findViewById<EditText>(R.id.timerLabelInput)
        val hoursPicker = dialogView.findViewById<NumberPicker>(R.id.hoursPicker)
        val minutesPicker = dialogView.findViewById<NumberPicker>(R.id.minutesPicker)
        val secondsPicker = dialogView.findViewById<NumberPicker>(R.id.secondsPicker)

        // Pre-fill with existing values
        labelInput.setText(timerSet.label)

        val hours = (timerSet.durationMillis / (1000 * 60 * 60)).toInt()
        val minutes = ((timerSet.durationMillis % (1000 * 60 * 60)) / (1000 * 60)).toInt()
        val seconds = ((timerSet.durationMillis % (1000 * 60)) / 1000).toInt()

        hoursPicker.minValue = 0
        hoursPicker.maxValue = 99
        hoursPicker.value = hours

        minutesPicker.minValue = 0
        minutesPicker.maxValue = 59
        minutesPicker.value = minutes

        secondsPicker.minValue = 0
        secondsPicker.maxValue = 59
        secondsPicker.value = seconds

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Timer")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newLabel = labelInput.text.toString().trim()
                val newHours = hoursPicker.value
                val newMinutes = minutesPicker.value
                val newSeconds = secondsPicker.value

                val totalMillis = (newHours * 3600 + newMinutes * 60 + newSeconds) * 1000L

                if (totalMillis > 0) {
                    val index = timerSets.indexOf(timerSet)
                    if (index != -1) {
                        timerSets[index] = timerSet.copy(
                            label = newLabel.ifEmpty { "Timer" },
                            durationMillis = totalMillis,
                            remainingMillis = totalMillis
                        )
                        adapter.notifyItemChanged(index)
                        Toast.makeText(requireContext(), "Timer updated!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteTimer(timerSet: TimerSet) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Timer")
            .setMessage("Are you sure you want to delete \"${timerSet.label}\"?")
            .setPositiveButton("Delete") { _, _ ->
                activeTimers[timerSet.id]?.cancel()
                activeTimers.remove(timerSet.id)

                val index = timerSets.indexOf(timerSet)
                if (index != -1) {
                    timerSets.removeAt(index)
                    adapter.notifyItemRemoved(index)
                    Toast.makeText(requireContext(), "Timer deleted!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        activeTimers.values.forEach { it.cancel() }
        activeTimers.clear()
    }
}