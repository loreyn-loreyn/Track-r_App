package com.example.trackr.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.trackr.R
import com.example.trackr.auth.LoginActivity
import com.example.trackr.utils.FirebaseHelper
import com.example.trackr.utils.ThemeHelper

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupToolbar()
        setupMenuItems()
    }

    private fun setupToolbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Menu"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun setupMenuItems() {
        findViewById<CardView>(R.id.accountCard).setOnClickListener {
            startActivity(Intent(this, AccountActivity::class.java))
        }

        findViewById<CardView>(R.id.themeCard).setOnClickListener {
            showThemeDialog()
        }

        findViewById<CardView>(R.id.notificationCard).setOnClickListener {
            startActivity(Intent(this, NotificationSettingsActivity::class.java))
        }

        findViewById<CardView>(R.id.helpCard).setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java))
        }

        findViewById<CardView>(R.id.aboutCard).setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        findViewById<CardView>(R.id.privacyCard).setOnClickListener {
            startActivity(Intent(this, PrivacyActivity::class.java))
        }

        findViewById<CardView>(R.id.logoutCard).setOnClickListener {
            confirmLogout()
        }
    }

    private fun showThemeDialog() {
        val themes = arrayOf("Light", "Dark", "System Default")
        val currentTheme = ThemeHelper.getCurrentTheme(this)
        val currentIndex = when (currentTheme) {
            ThemeHelper.THEME_LIGHT -> 0
            ThemeHelper.THEME_DARK -> 1
            else -> 2
        }

        AlertDialog.Builder(this)
            .setTitle("Choose Theme")
            .setSingleChoiceItems(themes, currentIndex) { dialog, which ->
                val selectedTheme = when (which) {
                    0 -> ThemeHelper.THEME_LIGHT
                    1 -> ThemeHelper.THEME_DARK
                    else -> ThemeHelper.THEME_SYSTEM
                }
                ThemeHelper.setTheme(this, selectedTheme)
                dialog.dismiss()
                recreate() // Restart activity to apply theme
            }
            .show()
    }

    private fun confirmLogout() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                FirebaseHelper.logoutUser()
                navigateToLogin()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}