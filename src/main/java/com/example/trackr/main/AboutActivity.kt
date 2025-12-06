package com.example.trackr.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.trackr.R

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        setupToolbar()
    }

    private fun setupToolbar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "About Track'r"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
