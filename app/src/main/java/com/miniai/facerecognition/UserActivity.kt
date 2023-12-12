package com.miniai.facerecognition

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.fm.face.*
import com.google.android.material.floatingactionbutton.FloatingActionButton

class UserActivity : AppCompatActivity() {

    companion object {
        private val TAG = UserActivity::class.simpleName
        private val ADD_USER_REQUEST_CODE = 1
        private val CAMERA_REQUEST_CODE = 2
    }

    private lateinit var userDb: UserDB
    private lateinit var adapter: UsersAdapter
    private lateinit var txtWarning: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        userDb = UserDB(this)
        userDb.loadUsers()

        adapter = UsersAdapter(this, UserDB.userInfos)
        val listView: ListView = findViewById<View>(R.id.userList) as ListView
        listView.setAdapter(adapter)

        listView.setOnItemClickListener { adapterView, view, i, l ->
            val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this@UserActivity)
            alertDialog.setTitle(getString(R.string.delete_user))

            val inflater = layoutInflater
            val dialogView = inflater.inflate(R.layout.activity_dialog, null)
            alertDialog.setView(dialogView)

            // Access the views in the custom dialog layout
            val imageView = dialogView.findViewById<ImageView>(R.id.dialogFaceView)
            val textView = dialogView.findViewById<TextView>(R.id.dialogTextView)

            // Customize the views
            // Get the data item for this position
            imageView.setImageBitmap(UserDB.userInfos.get(i).faceImage)
            textView.text = UserDB.userInfos.get(i).userName

            // Set positive button and its click listener
            alertDialog.setPositiveButton(getString(R.string.delete)) { dialogView, which ->
                // Handle positive button click, if needed
                userDb.deleteUser(UserDB.userInfos.get(i).userName)
                UserDB.userInfos.removeAt(i)

                adapter.notifyDataSetChanged()
                dialogView.dismiss()
            }

            // Set positive button and its click listener
            alertDialog.setNegativeButton(getString(R.string.delete_all)) { dialogView, which ->
                // Handle positive button click, if needed
                userDb.deleteAllUser()
                UserDB.userInfos.clear()

                adapter.notifyDataSetChanged()
                dialogView.dismiss()
            }

            // Create and show the AlertDialo
            val alert: AlertDialog = alertDialog.create()
            alert.show()
        }

        findViewById<FloatingActionButton>(R.id.buttonAdd).setOnClickListener {
            val intent = Intent()
            intent.setType("image/*")
            intent.setAction(Intent.ACTION_PICK)
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), ADD_USER_REQUEST_CODE)
        }

        findViewById<FloatingActionButton>(R.id.buttonCamera).setOnClickListener {
            startActivityForResult(Intent(this, CameraActivity::class.java), CAMERA_REQUEST_CODE)
        }

    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ADD_USER_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                var bitmap: Bitmap = ImageRotator.getCorrectlyOrientedImage(this, data?.data!!)
                var faceResults: List<FaceBox>? = FaceSDK.getInstance().detectFace(bitmap)
                if(faceResults.isNullOrEmpty()) {
                    Toast.makeText(this, getString(R.string.no_face_detected), Toast.LENGTH_SHORT).show()
                } else if(faceResults.size == 1) {
                    val livenessScore = FaceSDK.getInstance().checkLiveness(bitmap, faceResults!!.get(0))
                    if(livenessScore > FrameAnalyser.LIVENESS_THRESHOLD) {
                        val faceRect = Rect(faceResults!!.get(0).left, faceResults!!.get(0).top, faceResults!!.get(0).right, faceResults!!.get(0).bottom)
                        val cropRect = Utils.getBestRect(bitmap.width, bitmap.height, faceRect)
                        val faceImage = Utils.crop(bitmap, cropRect.left, cropRect.top, cropRect.width(), cropRect.height(), 250, 250)
                        val featData = FaceSDK.getInstance().extractFeature(bitmap, faceResults!!.get(0))

                        val userName = String.format("User%03d", userDb.getLastUserId() + 1)

                        val inputView = LayoutInflater.from(this)
                            .inflate(R.layout.dialog_input_view, null, false)
                        val editText = inputView.findViewById<EditText>(R.id.et_user_name)
                        val ivHead = inputView.findViewById<ImageView>(R.id.iv_head)
                        ivHead.setImageBitmap(faceImage)
                        editText.setText(userName)
                        val confirmUpdateDialog: AlertDialog = AlertDialog.Builder(this)
                            .setView(inputView)
                            .setPositiveButton(
                                "OK", null
                            )
                            .setNegativeButton(
                                "Cancel", null
                            )
                            .create()
                        confirmUpdateDialog.show()
                        confirmUpdateDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setOnClickListener { v: View? ->
                                val s = editText.text.toString()
                                if (TextUtils.isEmpty(s)) {
                                    editText.error = application.getString(R.string.name_should_not_be_empty)
                                    return@setOnClickListener
                                }

                                var exists:Boolean = false
                                for(user in UserDB.userInfos) {
                                    if(TextUtils.equals(user.userName, s)) {
                                        exists = true
                                        break
                                    }
                                }

                                if(exists) {
                                    editText.error = application.getString(R.string.duplicated_name)
                                    return@setOnClickListener
                                }

                                val userId = userDb.insertUser(s, faceImage, featData)
                                val face = UserInfo(userId, s, faceImage, featData)
                                UserDB.userInfos.add(face)

                                confirmUpdateDialog.cancel()

                                adapter.notifyDataSetChanged()
                                Toast.makeText(this, getString(R.string.register_successed), Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, getString(R.string.liveness_check_failed), Toast.LENGTH_SHORT).show()
                    }

                } else if (faceResults.size > 1) {
                    Toast.makeText(this, getString(R.string.multiple_face_detected), Toast.LENGTH_SHORT).show()
                }
            } catch (e: java.lang.Exception) {
                //handle exception
                e.printStackTrace()
            }
        }
    }
}