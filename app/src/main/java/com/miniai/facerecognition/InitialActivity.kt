package com.miniai.facerecognition

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class InitialActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial)
    }

    fun onButtonClick(view: View?){
        val intent = Intent(this@InitialActivity, MainActivity::class.java)
        startActivities(arrayOf(intent))
    }
}
