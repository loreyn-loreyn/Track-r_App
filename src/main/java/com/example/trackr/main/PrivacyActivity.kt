package com.example.trackr.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.trackr.R

class PrivacyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)

        setupToolbar()
    }

    private fun setupToolbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Privacy Policy"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}