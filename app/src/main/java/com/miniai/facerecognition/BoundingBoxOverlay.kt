package com.miniai.facerecognition;

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.fm.face.FaceBox

// Defines an overlay on which the boxes and text will be drawn.
class BoundingBoxOverlay( context: Context , attributeSet: AttributeSet )
    : SurfaceView( context , attributeSet ) , SurfaceHolder.Callback {

    companion object {
        private val TAG = BoundingBoxOverlay::class.simpleName
    }
    // Variables used to compute output2overlay transformation matrix
    // These are assigned in FrameAnalyser.kt
    var areDimsInit = false
    var frameHeight = 0
    var frameWidth = 0

    // This var is assigned in FrameAnalyser.kt
    var faceBoundingBoxes: List<FaceBox>? = null
    var livenessScore = 0.0f
    var livenessResult: Int = 0
    var isProcMode: Int = 2

    private var output2OverlayTransform: Matrix = Matrix()

    // Paint for boxes and text
    private val realBoxPaint = Paint().apply {
        strokeWidth = 5.0f
        color = Color.parseColor("#FF00FF00")
        style = Paint.Style.STROKE
        textSize = 64f
    }

    private val spoofBoxPaint = Paint().apply {
        strokeWidth = 5.0f
        color = Color.parseColor("#FFFF0000")
        style = Paint.Style.STROKE
        textSize = 64f
    }

    private val mulitpleBoxPaint = Paint().apply {
        strokeWidth = 5.0f
        color = Color.parseColor("#FFFFFF00")
        style = Paint.Style.STROKE
        textSize = 64f
    }

    private val realTextPaint = Paint().apply {
        strokeWidth = 2.0f
        color = Color.parseColor("#FF00FF00")
        textSize = 64f
    }

    private val spoofTextPaint = Paint().apply {
        strokeWidth = 2.0f
        color = Color.parseColor("#FFFF0000")
        textSize = 64f
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        TODO("Not yet implemented")
    }


    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        TODO("Not yet implemented")
    }


    override fun surfaceDestroyed(holder: SurfaceHolder) {
        TODO("Not yet implemented")
    }

    override fun onDraw(canvas: Canvas?) {
        if (faceBoundingBoxes != null) {
            if (!areDimsInit) {
                val viewWidth = canvas!!.width.toFloat()
                val viewHeight = canvas.height.toFloat()
                val xFactor: Float = viewWidth / frameWidth.toFloat()
                val yFactor: Float = viewHeight / frameHeight.toFloat()
                // Scale and mirror the coordinates ( required for front lens )
                output2OverlayTransform.preScale(xFactor, yFactor)
                output2OverlayTransform.postScale(1f, 1f, viewWidth / 2f, viewHeight / 2f)
                areDimsInit = true
            }
            else {
                for (face in faceBoundingBoxes!!) {
                    val boundingBox = RectF(face.left.toFloat(), face.top.toFloat(), face.right.toFloat(), face.bottom.toFloat())
                    output2OverlayTransform.mapRect(boundingBox)
                    val formattedScore = "%.4f".format(livenessScore)
                    if (isProcMode != 2) {
                        if(livenessResult == 0) {
                            canvas?.drawText(
                                "Fake",
                                boundingBox.left + 20,
                                boundingBox.top - 30,
                                spoofTextPaint
                            )
                            canvas?.drawRoundRect(boundingBox, 16f, 16f, spoofBoxPaint)
                        } else if(livenessResult == 1) {
                            canvas?.drawText(
                                "Real",
                                boundingBox.left + 20,
                                boundingBox.top - 30,
                                realTextPaint
                            )
                            canvas?.drawRoundRect(boundingBox, 16f, 16f, realBoxPaint)
                        } else if(livenessResult == 2) {
                            canvas?.drawRoundRect(boundingBox, 16f, 16f, mulitpleBoxPaint)
                        }
                    } else {
                        if(livenessResult == 0) {
                            canvas?.drawText(
                                "Fake $formattedScore",
                                boundingBox.left + 20,
                                boundingBox.top - 30,
                                spoofTextPaint
                            )
                            canvas?.drawRoundRect(boundingBox, 16f, 16f, spoofBoxPaint)
                        } else if(livenessResult == 1) {
                            canvas?.drawText(
                                "Real $formattedScore",
                                boundingBox.left + 20,
                                boundingBox.top - 30,
                                realTextPaint
                            )
                            canvas?.drawRoundRect(boundingBox, 16f, 16f, realBoxPaint)
                        } else if(livenessResult == 2) {
                            canvas?.drawRoundRect(boundingBox, 16f, 16f, mulitpleBoxPaint)
                        }
                    }
                }
            }
        }
    }
}
