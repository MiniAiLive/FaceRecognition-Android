package com.miniai.facerecognition

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.fm.face.FaceSDK
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

class MainActivity : AppCompatActivity() {

    companion object {
        private val SELECT_PHOTO_REQUEST_CODE = 1
    }

//    private lateinit var userDb: UserDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.button_learn).setOnClickListener {
            startActivity(Intent(this, LearnMoreActivity::class.java))
        }

//        userDb = UserDB(this)
//        userDb.loadUsers()

        FaceSDK.createInstance(this)
        val ret = FaceSDK.getInstance().init(assets)
        Log.i("Ret Number : ", ret.toString())
        if(ret != FaceSDK.SDK_SUCCESS) {
            if(ret == FaceSDK.SDK_ACTIVATE_APPID_ERROR) {
                showAlertDialog(getString(R.string.appid_error))
            } else if(ret == FaceSDK.SDK_ACTIVATE_INVALID_LICENSE) {
                showAlertDialog(getString(R.string.invalid_license))
            } else if(ret == FaceSDK.SDK_ACTIVATE_LICENSE_EXPIRED) {
                showAlertDialog(getString(R.string.license_expired))
            } else if(ret == FaceSDK.SDK_NO_ACTIVATED) {
                showAlertDialog(getString(R.string.no_activated))
            } else if(ret == FaceSDK.SDK_INIT_ERROR) {
                showAlertDialog(getString(R.string.init_error))
            }
        } else {
            findViewById<Button>(R.id.button_enroll).setOnClickListener {
                val intent = Intent(this, UserActivity::class.java)
                startActivity(intent)
            }

            findViewById<Button>(R.id.button_verify).setOnClickListener {
                val intent = Intent(this, IdentifyActivity::class.java)
                startActivity(intent)
            }

            findViewById<Button>(R.id.button_liveness).setOnClickListener {
                val intent = Intent(this, LivenessTestActivity::class.java)
                startActivity(intent)
            }
        }

        findViewById<Button>(R.id.button_about).setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }

    private fun showAlertDialog(message: String) {
        val builder = AlertDialog.Builder(this)

        // Set the dialog title and message
        builder.setTitle("Warning!")
        builder.setMessage(message + "\nYou may not able to test our SDK!\nContact US and Purchase License and Enjoy!")

        // Set positive button and its click listener
        builder.setPositiveButton("OK") { dialog, which ->
            // Handle positive button click, if needed
            // You can perform some action or dismiss the dialog
            dialog.dismiss()
        }

        // Set negative button and its click listener, if needed
        builder.setNegativeButton("Cancel") { dialog, which ->
            // Handle negative button click, if needed
            // You can perform some action or dismiss the dialog
            dialog.dismiss()
        }

        // Create and show the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }
}