package de.dhbwstuttgart.semesterarbeit_projektmanagement.login_register

import android.content.Context
import android.content.Intent
import de.dhbwstuttgart.semesterarbeit_projektmanagement.FileUtil
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID

object LoginUtil {

    fun getUserbyEmail(applicationContext: Context, email: String): JSONObject? {
        return getUserbyField(applicationContext, "email", email)
    }

    fun getUserbyUUID(applicationContext: Context, uuid: String): JSONObject? {
        return getUserbyField(applicationContext, "uuid", uuid)
    }

    fun getUserbyField(applicationContext: Context, field: String, email: String): JSONObject? {
        val users = FileUtil.readJSON("users.json", applicationContext)
        for (key in users.keys()) {
            val user = users.getJSONObject(key)
            if (user.has(field) && user.get(field) == email) {
                return user
            }
        }
        return null
    }

    fun loginAndGetIntent(applicationContext: Context, user: JSONObject) : Intent {
        val intent = Intent(applicationContext, TestHomeActivity::class.java)
        // Add all user data to the intent for passing to the next activity
        for (key in user.keys()) {
            intent.putExtra(key, user.getString(key))
        }
        // Put uuid into local-user.json file to recognize this user in later logins
        val jsonObj = JSONObject()
        jsonObj.put("uuid", user.getString("uuid"))
        FileUtil.writeJSON("local-user.json", jsonObj, applicationContext)
        return intent
    }

    fun hash(input: String): String {
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }

    fun getUserObj(
        uuid: UUID,
        name: String,
        email: String,
        password: String,
        fakultaet: String,
        studiengang: String,
        jahrgang: String
    ): JSONObject {
        val user = JSONObject()
        user.put("uuid", uuid.toString())
        user.put("name", name)
        user.put("email", email)
        user.put("password", password)
        user.put("fakultaet", fakultaet)
        user.put("studiengang", studiengang)
        user.put("jahrgang", jahrgang)
        return user
    }
}