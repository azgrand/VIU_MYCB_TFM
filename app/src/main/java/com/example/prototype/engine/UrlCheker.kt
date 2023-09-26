package com.example.prototype.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.IOException
import android.util.Base64
import com.example.prototype.engine.PermissionCode.Companion.API_KEY
import com.example.prototype.engine.PermissionCode.Companion.API_URL
import org.json.JSONObject
import java.io.UnsupportedEncodingException

class UrlCheker {

    fun checkURLForMalware(url: String, onResult: (Boolean, Int, Int, Int, Int, String) -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(API_URL + "/" + generateUrlId(url))
                .get()
                .addHeader("accept", "application/json")
                .addHeader("x-apikey", API_KEY)
                .build()

            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(response.body?.string())
                    val data = jsonResponse.getJSONObject("data")
                    val attributes = data.getJSONObject("attributes")
                    val harmlessCount = attributes.getJSONObject("last_analysis_stats").getInt("harmless")
                    val maliciousCount = attributes.getJSONObject("last_analysis_stats").getInt("malicious")
                    val suspiciousCount = attributes.getJSONObject("last_analysis_stats").getInt("suspicious")
                    val undetectedCount = attributes.getJSONObject("last_analysis_stats").getInt("undetected")
                    val title = if (attributes.has("title")) {
                        attributes.getString("title")
                    } else {
                        "URL Maliciosa"
                    }

                    withContext(Dispatchers.Main) {
                        onResult(true, harmlessCount, maliciousCount, suspiciousCount, undetectedCount, title)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onResult(false, 0, 0, 0, 0, "")
                    }
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    onResult(false, 0, 0, 0, 0, "")
                }
            }
        }
    }

    private fun generateUrlId(url: String): String {
        return try {
            val urlBytes = url.toByteArray(charset("UTF-8"))
            val base64Url = Base64.encodeToString(urlBytes, Base64.URL_SAFE or Base64.NO_WRAP)
            // Eliminar los caracteres '=' al final del resultado
            base64Url.trimEnd('=')
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            "" // Manejar el error de codificación
        }
    }

    fun isValidUrl(url: String): Boolean {
        val urlPattern = Regex(
            "^(?:http|ftp)s?://"  // http:// or https://
                    + "(?:(?:[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?\\.)+(?:[A-Z]{2,6}\\.?|[A-Z0-9-]{2,}\\.?)|"  // domain...
                    + "localhost|"  // localhost...
                    + "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|"  // ...or ipv4
                    + "\\[?[A-F0-9]*:[A-F0-9:]+\\]?)"
                    + "(?::\\d+)?"
                    + "(?:/?|[/?]\\S+)$", RegexOption.IGNORE_CASE
        )
        return url.matches(urlPattern)
    }

}