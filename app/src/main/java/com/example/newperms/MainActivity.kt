package com.example.newperms

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val appsButton: Button = findViewById(R.id.apps_button)
        val mediaButton: Button = findViewById(R.id.media_button)

        // Navigate to Apps Analysis Activity
        appsButton.setOnClickListener {
            val intent = Intent(this, AppsAnalysisActivity::class.java)
            startActivity(intent)
        }

        // Navigate to Media Analysis Activity
        mediaButton.setOnClickListener {
            val intent = Intent(this, MediaAnalysisActivity::class.java)
            startActivity(intent)
        }
    }
}
