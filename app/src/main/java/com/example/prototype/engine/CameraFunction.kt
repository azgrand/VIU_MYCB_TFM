package com.example.prototype.engine

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import java.util.Hashtable

class CameraFunction(private val activity: AppCompatActivity) {

    fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    fun requestCameraPermission() {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), PermissionCode.CAMERA_PERMISSION_REQUEST_CODE)
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        when (requestCode) {
            PermissionCode.CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permiso de cámara concedido
                } else {
                    // Permiso de cámara denegado
                    Toast.makeText(activity, "No hay acceso a la camara", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun generateAndDisplayQRBitmap(qrContent: String, imageView: ImageView) {
        val qrBitmap: Bitmap? = generateBitmapFromQRContent(qrContent)
        activity.runOnUiThread {
            if (qrBitmap != null) {
                // Asegúrate de que la imagen se ajuste correctamente al ImageView
                imageView.setImageBitmap(Bitmap.createScaledBitmap(qrBitmap, 500, 500, false))
            } else {
                imageView.setImageDrawable(null)
            }
        }
    }

    private fun generateBitmapFromQRContent(qrContent: String): Bitmap? {
        val qrCodeBitmap: Bitmap? = generateQRCodeBitmap(qrContent, 500, 500) // Usamos 500x500
        return qrCodeBitmap?.let { applyGrayScale(it) }
    }

    private fun generateQRCodeBitmap(qrContent: String, width: Int, height: Int): Bitmap? {
        val hints: Hashtable<EncodeHintType, String> = Hashtable()
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        try {
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(qrContent, BarcodeFormat.QR_CODE, width, height, hints)
            val pixels = IntArray(width * height)

            for (y in 0 until height) {
                for (x in 0 until width) {
                    if (bitMatrix[x, y]) {
                        pixels[y * width + x] = 0xff000000.toInt() // Color negro
                    } else {
                        pixels[y * width + x] = 0xffffffff.toInt() // Color blanco
                    }
                }
            }
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            return bitmap
        } catch (e: WriterException) {
            e.printStackTrace()
        }
        return null
    }

    private fun applyGrayScale(bitmap: Bitmap): Bitmap {
        val bmpGrayscale = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmpGrayscale)
        val paint = Paint()
        val cm = ColorMatrix()
        cm.setSaturation(0f)
        val f = ColorMatrixColorFilter(cm)
        paint.colorFilter = f
        c.drawBitmap(bitmap, 0f, 0f, paint)
        return bmpGrayscale
    }

}