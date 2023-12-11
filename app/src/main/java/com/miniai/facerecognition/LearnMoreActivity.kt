package com.miniai.facerecognition

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class LearnMoreActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learn)

        findViewById<Button>(R.id.button_back).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

    }
}
