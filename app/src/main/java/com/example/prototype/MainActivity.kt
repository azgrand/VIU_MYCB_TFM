package com.example.prototype

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.prototype.engine.CameraFunction
import com.example.prototype.engine.InternetFunction
import com.example.prototype.engine.PermissionCode.Companion.CAMERA_PERMISSION_REQUEST_CODE
import com.example.prototype.engine.PermissionCode.Companion.INTERNET_PERMISSION_REQUEST_CODE
import com.example.prototype.engine.UrlCheker
import com.google.zxing.integration.android.*

class MainActivity : AppCompatActivity() {

    private lateinit var internetFunction: InternetFunction
    private lateinit var cameraFunction: CameraFunction
    private var iHarmless: Int = 0
    private var iMalicious: Int = 0
    private var iSuspicious: Int = 0
    private var iUndetected: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        internetFunction = InternetFunction(this)
        cameraFunction = CameraFunction(this)
        // Bloquea la orientación en modo retrato
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        // Ocultar la barra de título
        supportActionBar?.hide()
        // Ocultar la barra de estado
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        setContentView(R.layout.activity_main)
        // Verifica el permiso de la cámara
        if (!cameraFunction.isCameraPermissionGranted()){
            cameraFunction.requestCameraPermission()
        }
        // Verifica el permiso de Internet
        if (!internetFunction.isInternetPermissionGranted()) {
            internetFunction.requestInternetPermission()
        }
        // Inicia el escaneo de códigos QR
        val scanQRButton: Button = findViewById(R.id.scanQRButton)
        scanQRButton.setOnClickListener {
            IntentIntegrator(this).initiateScan()
        }
        // Agrega un clic para abrir la página web desde el TextView
        val labelUrl: TextView = findViewById(R.id.labelWeblUrl)
        labelUrl.setOnClickListener {
            internetFunction.openWebPage(labelUrl.text.toString(),iMalicious, iSuspicious)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        iHarmless = 0
        iMalicious = 0
        iSuspicious = 0
        iUndetected = 0
        // Recupera el resultado del escaneo
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents != null) {
                // Código QR escaneado con éxito
                val urlChecker = UrlCheker()
                val labelUrl: TextView = findViewById(R.id.labelWeblUrl)
                val labelTittle: TextView = findViewById(R.id.labelWeblTitle)
                val labelAnalysisHarmless: TextView = findViewById(R.id.labelWebAnalysisHarmless)
                val labelAnalysisMalicious: TextView = findViewById(R.id.labelWebAnalysisMalicious)
                val labelAnalysisSuspicious: TextView = findViewById(R.id.labelWebAnalysisSuspicious)
                val labelAnalysisUndetected: TextView = findViewById(R.id.labelWebAnalysisUndetected)
                // Realiza comprobaciones y validaciones
                if (urlChecker.isValidUrl(result.contents)) {
                    urlChecker.checkURLForMalware(result.contents) { isMalware, harmlessCount, maliciousCount, suspiciousCount, undetectedCount, title ->
                        iHarmless = harmlessCount
                        iMalicious = maliciousCount
                        iSuspicious = suspiciousCount
                        iUndetected = undetectedCount
                        if (isMalware) {
                            if (maliciousCount == 0 ){
                                // La URL no contiene malware
                                labelTittle.text = title
                                labelAnalysisHarmless.text = "Inofensivo: $harmlessCount"
                                labelAnalysisMalicious.text = "Maliciosos: $maliciousCount"
                                labelAnalysisSuspicious.text = "Sospechosos: $suspiciousCount"
                                labelAnalysisUndetected.text = "Ocultos: $undetectedCount "
                                labelUrl.text = result.contents
                                if (suspiciousCount == 0){
                                    labelUrl.setTextColor(ContextCompat.getColor(this, R.color.colorHarmless))
                                }
                                else{
                                    labelUrl.setTextColor(ContextCompat.getColor(this, R.color.colorSuspicious))
                                }
                            }
                            else {
                                // La URL contiene malware
                                labelTittle.text = title
                                labelAnalysisHarmless.text = "Inofensivo: $harmlessCount"
                                labelAnalysisMalicious.text = "Maliciosos: $maliciousCount"
                                labelAnalysisSuspicious.text = "Sospechosos: $suspiciousCount"
                                labelAnalysisUndetected.text = "Ocultos: $undetectedCount "
                                labelUrl.text = result.contents
                                labelUrl.setTextColor(ContextCompat.getColor(this, R.color.colorMalicious))
                            }
                        } else {
                            // La URL no ha sido encontrada
                            labelTittle.text = "URL no encontrada"
                            labelAnalysisHarmless.text = ""
                            labelAnalysisMalicious.text = ""
                            labelAnalysisSuspicious.text = ""
                            labelAnalysisUndetected.text = ""
                            labelUrl.text ="La busqueda no ha dado resultados"
                            labelUrl.setTextColor(ContextCompat.getColor(this, R.color.colorInvalidUrl))
                        }
                    }
                } else {
                    // La URL no es valida
                    labelTittle.text = "URL no válida"
                    labelAnalysisHarmless.text = ""
                    labelAnalysisMalicious.text = ""
                    labelAnalysisSuspicious.text = ""
                    labelAnalysisUndetected.text = ""
                    labelUrl.text ="No se ha escaneado una URL o la URL no es validada"
                    labelUrl.setTextColor(ContextCompat.getColor(this, R.color.colorInvalidUrl))
                }
                // Genera y muestra un bitmap a partir del contenido del código QR
                val imageView: ImageView = findViewById(R.id.qrImageView)
                cameraFunction.generateAndDisplayQRBitmap(result.contents, imageView)
            } else {
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_LONG).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == INTERNET_PERMISSION_REQUEST_CODE) {
            internetFunction.onRequestPermissionsResult(requestCode, grantResults)
        } else if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            cameraFunction.onRequestPermissionsResult(requestCode, grantResults)
        }
    }

}