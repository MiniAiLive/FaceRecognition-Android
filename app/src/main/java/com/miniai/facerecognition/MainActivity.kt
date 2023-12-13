package com.miniai.facerecognition

//import android.R
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.fm.face.FaceSDK


class MainActivity : AppCompatActivity() {

    companion object {
        private val SELECT_PHOTO_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

            findViewById<CardView>(R.id.button_enroll).setOnClickListener { // Handle the card view click here
                val intent = Intent(this, UserActivity::class.java)
                startActivity(intent)
            }

            findViewById<CardView>(R.id.button_verify).setOnClickListener { // Handle the card view click here
                val intent = Intent(this, IdentifyActivity::class.java)
                startActivity(intent)
            }

            findViewById<CardView>(R.id.button_liveness).setOnClickListener { // Handle the card view click here
                val intent = Intent(this, LivenessTestActivity::class.java)
                startActivity(intent)
            }
        }

        findViewById<CardView>(R.id.button_about).setOnClickListener { // Handle the card view click here
            startActivity(Intent(this, AboutActivity::class.java))
        }
//        findViewById<Button>(R.id.button_about).setOnClickListener {
//            startActivity(Intent(this, AboutActivity::class.java))
//        }
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