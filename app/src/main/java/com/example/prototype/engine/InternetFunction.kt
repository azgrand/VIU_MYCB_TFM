package com.example.prototype.engine

import android.Manifest
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.prototype.engine.PermissionCode.Companion.INTERNET_PERMISSION_REQUEST_CODE

class InternetFunction(private val activity: AppCompatActivity) {

    fun isInternetPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
    }

    fun requestInternetPermission() {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.INTERNET), INTERNET_PERMISSION_REQUEST_CODE)
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        when (requestCode) {
            INTERNET_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permiso de Internet concedido
                } else {
                    // Permiso de Internet denegado
                    Toast.makeText(activity, "No hay conexión a Internet", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun openWebPage(url: String,  maliciousCount: Int, suspiciousCount: Int) {
        if (url.isNotEmpty()) {
            val intent = Uri.parse(url).let { webpage ->
                Intent(Intent.ACTION_VIEW, webpage)
            }
            if (maliciousCount == 0 && suspiciousCount == 0){
                // Abrimos la url sin problema
                activity.startActivity(intent)
            }
            else if (maliciousCount == 0 && suspiciousCount > 0){
                // Preguntamos al usuario si quiere continuar
                val builder = AlertDialog.Builder(activity)
                // Configura el título y el mensaje del diálogo
                builder.setTitle("Confirmación")
                builder.setMessage("¿No podemos confirmar la seguridad de la URL. ¿Quieres continuar?")
                // Agrega los botones y sus acciones
                builder.setPositiveButton("Sí") { _: DialogInterface, _: Int ->
                    // Aquí pones el código para continuar
                    activity.startActivity(intent)
                }
                builder.setNegativeButton("No") { _: DialogInterface, _: Int ->
                    // Aquí pones el código para detenerse
                    Toast.makeText(activity, "Apertura cancelada", Toast.LENGTH_LONG).show()
                }
                // Crea y muestra el diálogo
                val dialog = builder.create()
                dialog.show()
            }
            else{
                // Cancelamos la apertura
                Toast.makeText(activity, "La URL es maliciosa y no se se permite abrirla", Toast.LENGTH_LONG).show()
            }
        }
    }

}