package com.example.trackr.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trackr.R
import com.example.trackr.adapters.SearchResultAdapter
import com.example.trackr.models.Alarm
import com.example.trackr.models.Deadline
import com.example.trackr.utils.FirebaseHelper
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {

    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SearchResultAdapter

    private val allAlarms = mutableListOf<Alarm>()
    private val allDeadlines = mutableListOf<Deadline>()
    private val searchResults = mutableListOf<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        setupToolbar()
        initViews()
        loadData()
    }

    private fun setupToolbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Search"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun initViews() {
        searchView = findViewById(R.id.searchView)
        recyclerView = findViewById(R.id.searchRecyclerView)

        adapter = SearchResultAdapter(searchResults)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        setupSearchView()
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                performSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                performSearch(newText)
                return true
            }
        })

        searchView.isIconified = false
        searchView.requestFocus()
    }

    private fun loadData() {
        val userId = FirebaseHelper.getCurrentUserId() ?: return

        lifecycleScope.launch {
            FirebaseHelper.getAlarms(userId).onSuccess { alarms ->
                allAlarms.clear()
                allAlarms.addAll(alarms)
            }

            FirebaseHelper.getDeadlines(userId).onSuccess { deadlines ->
                allDeadlines.clear()
                allDeadlines.addAll(deadlines)
            }
        }
    }

    private fun performSearch(query: String?) {
        if (query.isNullOrBlank()) {
            searchResults.clear()
            adapter.notifyDataSetChanged()
            return
        }

        val lowerQuery = query.lowercase()
        searchResults.clear()

        allAlarms.filter { alarm ->
            alarm.label.lowercase().contains(lowerQuery)
        }.forEach { searchResults.add(it) }

        allDeadlines.filter { deadline ->
            deadline.title.lowercase().contains(lowerQuery) ||
                    deadline.description.lowercase().contains(lowerQuery) ||
                    deadline.category.lowercase().contains(lowerQuery)
        }.forEach { searchResults.add(it) }

        adapter.notifyDataSetChanged()
    }
}