package com.miniai.facerecognition

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val identifyedFace = intent.getParcelableExtra("identified_face") as? Bitmap
        val enrolledFace = intent.getParcelableExtra("enrolled_face") as? Bitmap
        val identifiedName = intent.getStringExtra("identified_name")
        val similarity = intent.getFloatExtra("similarity", 0f)
        val livenessScore = intent.getFloatExtra("liveness", 0f)
//        val yaw = intent.getFloatExtra("yaw", 0f)
//        val roll = intent.getFloatExtra("roll", 0f)
//        val pitch = intent.getFloatExtra("pitch", 0f)

        findViewById<ImageView>(R.id.imageEnrolled).setImageBitmap(enrolledFace)
        findViewById<ImageView>(R.id.imageIdentified).setImageBitmap(identifyedFace)
        findViewById<TextView>(R.id.textPerson).text = "User Name : " + identifiedName
        findViewById<TextView>(R.id.textSimilarity).text = "Similarity Score: " + similarity
        findViewById<TextView>(R.id.textLiveness).text = "Liveness score: " + livenessScore
//        findViewById<TextView>(R.id.textYaw).text = "Yaw: " + yaw
//        findViewById<TextView>(R.id.textRoll).text = "Roll: " + roll
//        findViewById<TextView>(R.id.textPitch).text = "Pitch: " + pitch
    }
}
