package com.miniai.facerecognition;

import android.content.ContentResolver
import android.content.Context
import android.graphics.*
import android.media.Image
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream

// Helper class for operations on Bitmaps
class BitmapUtils {
    companion object {

        // Crop the given bitmap with the given rect.
        fun cropRectFromBitmap(source: Bitmap, rect: Rect ): Bitmap {
            var width = rect.width()
            var height = rect.height()
            if ( (rect.left + width) > source.width ){
                width = source.width - rect.left
            }
            if ( (rect.top + height ) > source.height ){
                height = source.height - rect.top
            }
            val croppedBitmap = Bitmap.createBitmap( source , rect.left , rect.top , width , height )
            // Uncomment the below line if you want to save the input image.
            // BitmapUtils.saveBitmap( context , croppedBitmap , "source" )
            return croppedBitmap
        }


        // Get the image as a Bitmap from given Uri
        // Source -> https://developer.android.com/training/data-storage/shared/documents-files#bitmap
        fun getBitmapFromUri( contentResolver : ContentResolver , uri: Uri): Bitmap {
            val parcelFileDescriptor: ParcelFileDescriptor? = contentResolver.openFileDescriptor(uri, "r")
            val fileDescriptor: FileDescriptor = parcelFileDescriptor!!.fileDescriptor
            val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor.close()
            return image
        }


        // Rotate the given `source` by `degrees`.
        // See this SO answer -> https://stackoverflow.com/a/16219591/10878733
        fun rotateBitmap( source: Bitmap , degrees : Float ): Bitmap {
            val matrix = Matrix()
            matrix.postRotate( degrees )
            return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix , false )
        }


        // Flip the given `Bitmap` horizontally.
        // See this SO answer -> https://stackoverflow.com/a/36494192/10878733
        fun flipBitmap( source: Bitmap ): Bitmap {
            val matrix = Matrix()
            matrix.postScale(-1f, 1f, source.width / 2f, source.height / 2f)
            return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        }


        // Use this method to save a Bitmap to the internal storage ( app-specific storage ) of your device.
        // To see the image, go to "Device File Explorer" -> "data" -> "data" -> "com.ml.quaterion.facenetdetection" -> "files"
        fun saveBitmap(context: Context, image: Bitmap, name: String) {
            val fileOutputStream = FileOutputStream(File( context.filesDir.absolutePath + "/$name.png"))
            image.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
        }


        // Convert android.media.Image to android.graphics.Bitmap
        // See the SO answer -> https://stackoverflow.com/a/44486294/10878733
        fun imageToBitmap( image : Image , rotationDegrees : Int ): Bitmap {
            val yBuffer = image.planes[0].buffer
            val uBuffer = image.planes[1].buffer
            val vBuffer = image.planes[2].buffer
            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()
            val nv21 = ByteArray(ySize + uSize + vSize)
            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)
            val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
            val yuv = out.toByteArray()
            var output = BitmapFactory.decodeByteArray(yuv, 0, yuv.size)
            output = rotateBitmap( output , rotationDegrees.toFloat() )
            return flipBitmap( output )
        }


        // Convert the given Bitmap to NV21 ByteArray
        // See this comment -> https://github.com/firebase/quickstart-android/issues/932#issuecomment-531204396
        suspend fun bitmapToNV21ByteArray(bitmap: Bitmap): ByteArray = withContext(Dispatchers.Default) {
            val argb = IntArray(bitmap.width * bitmap.height )
            bitmap.getPixels(argb, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            val yuv = ByteArray(bitmap.height * bitmap.width + 2 * Math.ceil(bitmap.height / 2.0).toInt()
                    * Math.ceil(bitmap.width / 2.0).toInt())
            encodeYUV420SP( yuv, argb, bitmap.width, bitmap.height)
            return@withContext yuv
        }

        private fun encodeYUV420SP(yuv420sp: ByteArray, argb: IntArray, width: Int, height: Int) {
            val frameSize = width * height
            var yIndex = 0
            var uvIndex = frameSize
            var R: Int
            var G: Int
            var B: Int
            var Y: Int
            var U: Int
            var V: Int
            var index = 0
            for (j in 0 until height) {
                for (i in 0 until width) {
                    R = argb[index] and 0xff0000 shr 16
                    G = argb[index] and 0xff00 shr 8
                    B = argb[index] and 0xff shr 0
                    Y = (66 * R + 129 * G + 25 * B + 128 shr 8) + 16
                    U = (-38 * R - 74 * G + 112 * B + 128 shr 8) + 128
                    V = (112 * R - 94 * G - 18 * B + 128 shr 8) + 128
                    yuv420sp[yIndex++] = (if (Y < 0) 0 else if (Y > 255) 255 else Y).toByte()
                    if (j % 2 == 0 && index % 2 == 0) {
                        yuv420sp[uvIndex++] = (if (V < 0) 0 else if (V > 255) 255 else V).toByte()
                        yuv420sp[uvIndex++] = (if (U < 0) 0 else if (U > 255) 255 else U).toByte()
                    }
                    index++
                }
            }
        }

    }

}
