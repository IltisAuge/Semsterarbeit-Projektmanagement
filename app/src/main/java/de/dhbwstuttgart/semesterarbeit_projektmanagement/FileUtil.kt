package de.dhbwstuttgart.semesterarbeit_projektmanagement

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import org.json.JSONObject
import java.io.IOException
import java.io.OutputStreamWriter

object FileUtil {

    @JvmStatic
    fun writeJSON(file: String, json: JSONObject, context: Context) {
        try {
            val outputStreamWriter =
                OutputStreamWriter(context.openFileOutput(file, MODE_PRIVATE))
            outputStreamWriter.write(json.toString())
            outputStreamWriter.close()
        } catch (e: IOException) {
            Log.e("Exception", "File write failed: $e")
        }
    }

    @JvmStatic
    fun readJSON(path: String, context: Context): JSONObject {
        var jsonObj : JSONObject
        context.openFileInput(path).bufferedReader().useLines {
            jsonObj = JSONObject(it.fold("") { acc, text -> acc + text })
        }
        return jsonObj
    }
}