package com.example.trackr.main

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.example.trackr.R
import com.example.trackr.adapters.DeadlineAdapter
import com.example.trackr.models.Deadline
import com.example.trackr.utils.FirebaseHelper
import kotlinx.coroutines.launch
import java.util.*

class DeadlineFragment : Fragment() {

    private lateinit var calendarView: CalendarView
    private lateinit var monthYearText: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var addFab: FloatingActionButton
    private lateinit var filterButton: ImageButton
    private lateinit var adapter: DeadlineAdapter

    private val deadlines = mutableListOf<Deadline>()
    private var selectedDate: Calendar = Calendar.getInstance()
    private var currentFilter = "All"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_deadline, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendarView = view.findViewById(R.id.calendarView)
        monthYearText = view.findViewById(R.id.monthYearText)
        recyclerView = view.findViewById(R.id.deadlineRecyclerView)
        addFab = view.findViewById(R.id.addDeadlineFab)
        filterButton = view.findViewById(R.id.filterButton)

        setupCalendar()
        setupRecyclerView()
        loadDeadlines()

        addFab.setOnClickListener {
            showAddDeadlineDialog()
        }

        filterButton.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun setupCalendar() {
        updateMonthYearText()

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate.set(year, month, dayOfMonth)
        }
    }

    private fun updateMonthYearText() {
        val months = arrayOf("January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December")
        val month = selectedDate.get(Calendar.MONTH)
        val year = selectedDate.get(Calendar.YEAR)
        monthYearText.text = "${months[month]} $year"
    }

    private fun setupRecyclerView() {
        adapter = DeadlineAdapter(
            deadlines = deadlines,
            onClick = { deadline -> openDeadlineDetails(deadline) },
            onToggleComplete = { deadline, isCompleted -> toggleComplete(deadline, isCompleted) },
            onDelete = { deadline -> deleteDeadline(deadline) }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun loadDeadlines() {
        val userId = FirebaseHelper.getCurrentUserId() ?: return

        lifecycleScope.launch {
            val result = FirebaseHelper.getDeadlines(userId, currentFilter)

            result.onSuccess { deadlineList ->
                deadlines.clear()
                deadlines.addAll(deadlineList)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun showFilterDialog() {
        val filters = arrayOf("All", "Nearest Deadline", "Farthest", "By Name")

        AlertDialog.Builder(requireContext())
            .setTitle("Filter Deadlines")
            .setSingleChoiceItems(filters, filters.indexOf(currentFilter)) { dialog, which ->
                currentFilter = filters[which]
                loadDeadlines()
                dialog.dismiss()
            }
            .show()
    }

    private fun showAddDeadlineDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_deadline, null)
        val titleInput = dialogView.findViewById<TextInputEditText>(R.id.titleInput)
        val descInput = dialogView.findViewById<TextInputEditText>(R.id.descriptionInput)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)
        val dateButton = dialogView.findViewById<Button>(R.id.selectDateButton)

        val categories = arrayOf("Work", "Personal", "School", "Other")
        categorySpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)

        var selectedDateTime = Calendar.getInstance().timeInMillis

        dateButton.setOnClickListener {
            showDateTimePicker { timeInMillis ->
                selectedDateTime = timeInMillis
                dateButton.text = "Selected: ${android.text.format.DateFormat.format("MMM dd, yyyy hh:mm a", timeInMillis)}"
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Add Deadline")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val title = titleInput.text.toString().trim()
                val desc = descInput.text.toString().trim()
                val category = categorySpinner.selectedItem.toString()

                if (title.isNotEmpty()) {
                    saveDeadline(title, desc, category, selectedDateTime)
                } else {
                    Toast.makeText(requireContext(), "Please enter a title", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDateTimePicker(onSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                TimePickerDialog(
                    requireContext(),
                    { _, hour, minute ->
                        calendar.set(year, month, day, hour, minute, 0)
                        onSelected(calendar.timeInMillis)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveDeadline(title: String, desc: String, category: String, dateTime: Long) {
        val userId = FirebaseHelper.getCurrentUserId() ?: return

        val deadline = Deadline(
            userId = userId,
            title = title,
            description = desc,
            category = category,
            dateTimeInMillis = dateTime
        )

        lifecycleScope.launch {
            val result = FirebaseHelper.saveDeadline(deadline)

            result.onSuccess {
                Toast.makeText(requireContext(), "Deadline saved!", Toast.LENGTH_SHORT).show()
                loadDeadlines() // Reload to show new deadline
            }.onFailure {
                Toast.makeText(requireContext(), "Failed to save deadline", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openDeadlineDetails(deadline: Deadline) {
        Toast.makeText(requireContext(), "Opening ${deadline.title}", Toast.LENGTH_SHORT).show()
    }

    private fun toggleComplete(deadline: Deadline, isCompleted: Boolean) {
        lifecycleScope.launch {
            FirebaseHelper.updateDeadline(deadline.id, isCompleted)
            loadDeadlines()
        }
    }

    private fun deleteDeadline(deadline: Deadline) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Deadline")
            .setMessage("Are you sure?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    FirebaseHelper.deleteDeadline(deadline.id)
                    loadDeadlines()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
