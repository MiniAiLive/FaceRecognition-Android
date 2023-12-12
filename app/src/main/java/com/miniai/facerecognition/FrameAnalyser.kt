package com.miniai.facerecognition;

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.fm.face.FaceBox
import com.fm.face.FaceSDK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


// Analyser class to process frames and produce detections.
class FrameAnalyser(
    private var context: Context,
    private var boundingBoxOverlay: BoundingBoxOverlay,
    private var viewBackgroundOfMessage: View,
    private var textViewMessage: TextView,
    private var isProcMode: Int, // 0: Register, 1: Verify, 2: Liveness Test
) : ImageAnalysis.Analyzer {

    companion object {
        private val TAG = FrameAnalyser::class.simpleName

        const val VERIFY_TIMEOUT = 10000
        const val LIVENESS_THRESHOLD = 0.5f
        const val RECOGNIZE_THRESHOLD = 0.78f
    }

    enum class PROC_MODE {
        VERIFY, REGISTER,
    }

    var mode = PROC_MODE.VERIFY
    var startVerifyTime: Long = 0

    private var isRunning = false
    private var isProcessing = false
    private var isRegistering = false
    private var frameInterface: FrameInferface? = null

    fun cancelRegister() {
        mode = PROC_MODE.VERIFY
        isRegistering = false
    }

    fun setRunning(running: Boolean) {
        isRunning = running

        viewBackgroundOfMessage.alpha = 0f
        textViewMessage.alpha = 0f
        boundingBoxOverlay.faceBoundingBoxes = null
        boundingBoxOverlay.invalidate()
    }
    fun addOnFrameListener(frameInterface: FrameInferface) {
        this.frameInterface = frameInterface
    }
    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {

        if(!isRunning) {
            boundingBoxOverlay.faceBoundingBoxes = null
            boundingBoxOverlay.invalidate()
            image.close()
            return
        }

        if(isRegistering) {
            image.close()
            return
        }

        if (isProcessing) {
            image.close()
            return
        }
        else {
            isProcessing = true

            // Rotated bitmap for the FaceNet model
            val frameBitmap = BitmapUtils.imageToBitmap( image.image!! , image.imageInfo.rotationDegrees )

            // Configure frameHeight and frameWidth for output2overlay transformation matrix.
            if ( !boundingBoxOverlay.areDimsInit ) {
                boundingBoxOverlay.frameHeight = frameBitmap.height
                boundingBoxOverlay.frameWidth = frameBitmap.width
            }

            var livenessScore = 0.0f;
            var faceResult: List<FaceBox>? = FaceSDK.getInstance().detectFace(frameBitmap)
            if(!faceResult.isNullOrEmpty()) {
                if(faceResult!!.size == 1) {
                    hideMessage()
                    livenessScore = FaceSDK.getInstance().checkLiveness(frameBitmap, faceResult!!.get(0))
                    if(livenessScore > LIVENESS_THRESHOLD) {
                        boundingBoxOverlay.livenessResult = 1

                        val faceRect = Rect(
                            faceResult!!.get(0).left,
                            faceResult!!.get(0).top,
                            faceResult!!.get(0).right,
                            faceResult!!.get(0).bottom
                        )
                        val cropRect = Utils.getBestRect(
                            frameBitmap.width,
                            frameBitmap.height,
                            faceRect
                        )
                        val faceImage = Utils.crop(
                            frameBitmap,
                            cropRect.left,
                            cropRect.top,
                            cropRect.width(),
                            cropRect.height(),
                            120,
                            120
                        )

                        val featData = FaceSDK.getInstance()
                            .extractFeature(frameBitmap, faceResult!!.get(0))

                        if (isProcMode != 2) {
                            if (isProcMode == 0 && mode == PROC_MODE.REGISTER) {

                                isRegistering = true
                                faceResult = null
                                CoroutineScope(Dispatchers.Default).launch {
                                    withContext(Dispatchers.Main) {
                                        // Clear the BoundingBoxOverlay and set the new results ( boxes ) to be displayed.
                                        frameInterface?.onRegister(faceImage, featData)
                                    }
                                }
                            } else if (isProcMode == 1 && mode == PROC_MODE.VERIFY) {
                                var maxScore = 0.0f
                                var maxScoreName: String = ""
                                var maxScorefaceImage: Bitmap? = null
                                var maxScorefeatData: ByteArray
//                                val maxScoreuserId = 0
                                for (user in UserDB.userInfos) {
                                    val score = FaceSDK.getInstance()
                                        .compareFeature(user.featData, featData)
                                    if (maxScore < score) {
                                        maxScore = score
                                        maxScorefaceImage = user.faceImage
                                        maxScoreName = user.userName
                                        maxScorefeatData = user.featData
//                                        maxScoreuserId = user.userId
                                    }
                                }
                                Log.e("TestEngine", "max score: " + maxScore)
                                if (maxScore > RECOGNIZE_THRESHOLD) {
                                    boundingBoxOverlay.livenessResult = 1
                                    setRunning(false)

                                    CoroutineScope(Dispatchers.Default).launch {
                                        withContext(Dispatchers.Main) {
                                            // Clear the BoundingBoxOverlay and set the new results ( boxes ) to be displayed.
                                            frameInterface?.onVerify(context.getString(R.string.verify_succeed) + maxScoreName)
                                        }
                                    }

                                    val intent = Intent(context, ResultActivity::class.java).apply {
                                        putExtra("identified_face", faceImage)
                                        putExtra("enrolled_face", maxScorefaceImage)
                                        putExtra("identified_name", maxScoreName)
                                        putExtra("similarity", maxScore)
                                        putExtra("liveness", livenessScore)
                                    }
                                    context.startActivity(intent)

//                                    startVerifyTime = 0
                                }
                            }
                        }
                    } else {
                        boundingBoxOverlay.livenessResult = 0
                        hideMessage()
                    }

                } else {
                    boundingBoxOverlay.livenessResult = 2
                    showMessage(context.getString(R.string.multiple_face_detected))
                }
            } else {

                if(isProcMode == 0 && mode == PROC_MODE.REGISTER) {
                    boundingBoxOverlay.livenessResult = 1
                    showMessage(context.getString(R.string.no_face_detected))
                } else {
                    boundingBoxOverlay.livenessResult = 0
                    hideMessage()
                }
            }

            CoroutineScope( Dispatchers.Default ).launch {
                withContext( Dispatchers.Main ) {
                    // Clear the BoundingBoxOverlay and set the new results ( boxes ) to be displayed.
                    boundingBoxOverlay.faceBoundingBoxes = faceResult
                    boundingBoxOverlay.livenessScore = livenessScore
                    boundingBoxOverlay.isProcMode = isProcMode
                    boundingBoxOverlay.invalidate()
                }
            }

            if (isProcMode != 2) {
                if(isProcMode == 1 && mode == PROC_MODE.VERIFY) {
                    if(startVerifyTime == 0.toLong())
                        startVerifyTime = System.currentTimeMillis()

                    if(System.currentTimeMillis() - startVerifyTime > VERIFY_TIMEOUT) {

                        CoroutineScope( Dispatchers.Default ).launch {
                            withContext( Dispatchers.Main ) {
                                if(faceResult.isNullOrEmpty() || UserDB.userInfos.size == 0) {
                                    frameInterface?.onVerify(context.getString(R.string.verify_timeout))
                                } else if(boundingBoxOverlay.livenessResult == 0 ){
                                    frameInterface?.onVerify(context.getString(R.string.liveness_failed))
                                } else {
                                    frameInterface?.onVerify(context.getString(R.string.verify_failed))
                                }
                            }
                        }
                        setRunning(false)
                    }
                }
            }

            isProcessing = false
            image.close()
        }
    }

    private fun showMessage(msg: String) {
        CoroutineScope( Dispatchers.Default ).launch {
            withContext( Dispatchers.Main ) {
                textViewMessage.text = msg
                viewBackgroundOfMessage.alpha = 1.0f
                textViewMessage.alpha = 1.0f
            }
        }
    }

    private fun hideMessage() {
        CoroutineScope( Dispatchers.Default ).launch {
            withContext( Dispatchers.Main ) {
                viewBackgroundOfMessage.alpha = 0.0f
                textViewMessage.alpha = 0.0f

            }
        }
    }
}