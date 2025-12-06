package com.example.trackr.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.example.trackr.R

class NotificationSettingsActivity : AppCompatActivity() {

    private lateinit var alarmNotifSwitch: SwitchCompat
    private lateinit var deadlineNotifSwitch: SwitchCompat
    private lateinit var timerNotifSwitch: SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_settings)

        setupToolbar()
        initViews()
    }

    private fun setupToolbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Notifications"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun initViews() {
        alarmNotifSwitch = findViewById(R.id.alarmNotifSwitch)
        deadlineNotifSwitch = findViewById(R.id.deadlineNotifSwitch)
        timerNotifSwitch = findViewById(R.id.timerNotifSwitch)

        // Load saved preferences
        val prefs = getSharedPreferences("notification_prefs", MODE_PRIVATE)
        alarmNotifSwitch.isChecked = prefs.getBoolean("alarm_notif", true)
        deadlineNotifSwitch.isChecked = prefs.getBoolean("deadline_notif", true)
        timerNotifSwitch.isChecked = prefs.getBoolean("timer_notif", true)

        // Save on change
        alarmNotifSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("alarm_notif", isChecked).apply()
        }

        deadlineNotifSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("deadline_notif", isChecked).apply()
        }

        timerNotifSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("timer_notif", isChecked).apply()
        }
    }
}
