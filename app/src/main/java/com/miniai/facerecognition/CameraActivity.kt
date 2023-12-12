package com.miniai.facerecognition;

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity(), FrameInferface {

    companion object {
        private const val FRAME_WIDTH = 720
        private const val FRAME_HEIGHT = 1280
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1
        private val TAG = CameraActivity::class.simpleName
    }

    private lateinit var previewView: PreviewView
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var frameAnalyser: FrameAnalyser
    private lateinit var boundingBoxOverlay: BoundingBoxOverlay

    private lateinit var viewBackgroundOfMessage: View
    private lateinit var textViewMessage: TextView

    private lateinit var userDb: UserDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        val isProcMode = 0 // 0: Register, 1: Verify, 2: Liveness Test

        previewView = findViewById<PreviewView>(R.id.preview_view)
        viewBackgroundOfMessage = findViewById<View>(R.id.viewBackgroundOfMessage)
        textViewMessage = findViewById<TextView>(R.id.textViewMessage)

        userDb = UserDB(this)
        userDb.loadUsers()
        
        boundingBoxOverlay = findViewById<BoundingBoxOverlay>(R.id.bbox_overlay)
        boundingBoxOverlay.setWillNotDraw(false)
        boundingBoxOverlay.setZOrderOnTop(true)
        frameAnalyser =
            FrameAnalyser(this, boundingBoxOverlay, viewBackgroundOfMessage, textViewMessage, isProcMode)
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        // Remove the status bar to have a full screen experience
        // See this answer on SO -> https://stackoverflow.com/a/68152688/10878733
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController!!
                .hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }

        findViewById<FloatingActionButton>(R.id.buttonAdd).setOnClickListener {
            frameAnalyser.mode = FrameAnalyser.PROC_MODE.REGISTER
            frameAnalyser.startVerifyTime = 0L
        }

        frameAnalyser.addOnFrameListener(this)
    }

    override fun onResume() {
        super.onResume()

        frameAnalyser.setRunning(true)
        // We'll only require the CAMERA permission from the user.
        // For scoped storage, particularly for accessing documents, we won't require WRITE_EXTERNAL_STORAGE or
        // READ_EXTERNAL_STORAGE permissions. See https://developer.android.com/training/data-storage
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestCameraPermission()
        } else {
            startCameraPreview()
        }
    }

    override fun onPause() {
        super.onPause()

        frameAnalyser.setRunning(false)
        stopCameraPreview()
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf<String>(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        );
    }

    private fun startCameraPreview() {
        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()
                bindPreview(cameraProvider)
            },
            ContextCompat.getMainExecutor(this)
        )
    }

    private fun stopCameraPreview() {
        cameraProviderFuture.get().unbindAll()
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview: Preview = Preview.Builder().build()
        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build()
        preview.setSurfaceProvider(previewView.surfaceProvider)
        val imageFrameAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(FRAME_WIDTH, FRAME_HEIGHT))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageFrameAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), frameAnalyser)
        cameraProvider.bindToLifecycle(
            this as LifecycleOwner,
            cameraSelector,
            preview,
            imageFrameAnalysis
        )
    }

    override fun onRegister(faceImage: Bitmap, featData: ByteArray?) {
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
                getString(R.string.ok), null
            )
            .setNegativeButton(
                getString(R.string.cancel), null
            )
            .setOnDismissListener {
                frameAnalyser.cancelRegister()
            }
            .create()
        confirmUpdateDialog.show()
        confirmUpdateDialog.getButton(AlertDialog.BUTTON_POSITIVE)
            .setOnClickListener { v: View? ->
                val s = editText.text.toString()
                if (TextUtils.isEmpty(s)) {
                    editText.error = application.getString(R.string.name_should_not_be_empty)
                    return@setOnClickListener
                }

                var exists: Boolean = false
                for (user in UserDB.userInfos) {
                    if (TextUtils.equals(user.userName, s)) {
                        exists = true
                        break
                    }
                }

                if (exists) {
                    editText.error = application.getString(R.string.duplicated_name)
                    return@setOnClickListener
                }

                val userId = userDb.insertUser(s, faceImage, featData)
                val face = UserInfo(userId, s, faceImage, featData)
                UserDB.userInfos.add(face)

                confirmUpdateDialog.cancel()

                Toast.makeText(this, getString(R.string.register_successed), Toast.LENGTH_SHORT)
                    .show()

                finish()
            }
    }

    override fun onVerify(msg: String) {

    }

}
