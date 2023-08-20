package net.airone01.deco.utils

import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.net.URL
import java.nio.charset.Charset

abstract class Web {
    companion object {
        @Throws(IOException::class)
        private fun readAll(rd: Reader): String {
            val sb = StringBuilder()
            var cp: Int
            while (rd.read().also { cp = it } != -1) {
                sb.append(cp.toChar())
            }
            return sb.toString()
        }

        @Throws(IOException::class, JSONException::class)
        fun readJsonFromUrl(url: String?): JSONObject {
            val `is` = URL(url).openStream()
            return try {
                val rd = BufferedReader(InputStreamReader(`is`, Charset.forName("UTF-8")))
                val jsonText = readAll(rd)
                JSONObject(jsonText)
            } finally {
                `is`.close()
            }
        }
    }
}