package de.dhbwstuttgart.semesterarbeit_projektmanagement.login_register

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import de.dhbwstuttgart.semesterarbeit_projektmanagement.FileUtil
import de.dhbwstuttgart.semesterarbeit_projektmanagement.NavigationMainActivity
import de.dhbwstuttgart.semesterarbeit_projektmanagement.R
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.security.MessageDigest
import java.util.UUID

object UserUtils {

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

    fun saveUser(applicationContext: Context, user: JSONObject) {
        val users = FileUtil.readJSON("users.json", applicationContext)
        users.put(user.getString("uuid"), user)
        FileUtil.writeJSON("users.json", users, applicationContext)
    }

    fun getLocalUserUUID(applicationContext: Context) : String? {
        val file = File(applicationContext.filesDir, "local-user.json")
        if (!file.exists()) {
            file.createNewFile()
            file.writeText(
                "{ 'uuid': '083509e0-83f8-49da-acb7-e5850f039d3e' }"
            )
        }
        // TODO: Remove the code above after integration with login activity
        val localUser = FileUtil.readJSON("local-user.json", applicationContext)
        if (!localUser.has("uuid") || localUser.get("uuid") == "") {
            return null
        }
        return localUser.getString("uuid")
    }

    fun getProfilePictureBitmap(applicationContext: Context, resources: Resources, uuid: String) : Bitmap {
        val imgFile = File(applicationContext.filesDir, "pp-$uuid")
        if (imgFile.exists()) {
            val uri = Uri.fromFile(imgFile)
            uri.let { applicationContext.contentResolver.openInputStream(it) }.use {
                val bufferedIs = BufferedInputStream(it)
                val bitmap = BitmapFactory.decodeStream(bufferedIs)
                bufferedIs.close()
                it?.close()
                return bitmap
            }
        }
        return BitmapFactory.decodeResource(resources, R.mipmap.blank_pp)
    }

    fun loginAndGetIntent(applicationContext: Context, user: JSONObject) : Intent {
        val intent = Intent(applicationContext, NavigationMainActivity::class.java)
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

    fun getAllUserUUIDs(applicationContext: Context): List<String> {
        val users = FileUtil.readJSON("users.json", applicationContext)
        val uuidList = mutableListOf<String>()
        for (key in users.keys()) {
            uuidList.add(key)
        }
        return uuidList
    }
    fun getAllUserUUIDsExceptLocal(applicationContext: Context): List<String> {
        val localUUID = getLocalUserUUID(applicationContext)
        return getAllUserUUIDs(applicationContext).filter { it != localUUID }
    }
}