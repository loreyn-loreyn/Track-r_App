package com.example.trackr.main

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.trackr.R
import com.example.trackr.utils.FirebaseHelper
import com.example.trackr.utils.ThemeHelper
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var searchButton: ImageButton
    private var currentFragment: String = "deadline"

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before setContentView
        ThemeHelper.applyTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        showWelcomePopup()
        setupBottomNavigation()

        // Load default fragment (Deadline)
        if (savedInstanceState == null) {
            loadFragment(DeadlineFragment())
            currentFragment = "deadline"
        }
    }

    private fun initViews() {
        bottomNav = findViewById(R.id.bottomNav)
        searchButton = findViewById(R.id.searchButton)

        searchButton.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
    }

    private fun showWelcomePopup() {
        val userId = FirebaseHelper.getCurrentUserId() ?: return

        lifecycleScope.launch {
            val result = FirebaseHelper.getUserData(userId)

            result.onSuccess { user ->
                val message = "Hello, ${user.fullName}!"

                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Welcome!")
                    .setMessage(message)
                    .setPositiveButton("Continue") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setCancelable(true)
                    .show()
            }
        }
    }

    private fun setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_deadline -> {
                    loadFragment(DeadlineFragment())
                    currentFragment = "deadline"
                    true
                }
                R.id.nav_alarm -> {
                    loadFragment(AlarmFragment())
                    currentFragment = "alarm"
                    true
                }
                R.id.nav_timer -> {
                    loadFragment(TimerFragment())
                    currentFragment = "timer"
                    true
                }
                R.id.nav_menu -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    false
                }
                else -> false
            }
        }

        // Set default selected item
        bottomNav.selectedItemId = R.id.nav_deadline
    }

    private fun loadFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}