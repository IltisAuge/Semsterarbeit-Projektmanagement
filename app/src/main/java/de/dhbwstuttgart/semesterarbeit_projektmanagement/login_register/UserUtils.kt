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
import de.dhbwstuttgart.semesterarbeit_projektmanagement.profile_settings.ProfileSettingsActivity
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.net.URL
import java.security.MessageDigest
import java.util.UUID
import kotlin.random.Random


object UserUtils {

    val random = Random

    // returns Pair<hobbys, companies>
    fun getAvailableTags(applicationContext: Context) : Pair<List<String>, List<String>> {
        val json = applicationContext.resources.assets.open("available_tags.json").bufferedReader().use { it.readText() }
        val tagsFileObj = JSONObject(json)
        val hobbysObj = tagsFileObj.getJSONArray("hobbys")
        val companiesObj = tagsFileObj.getJSONArray("companies")
        val hobbys = mutableListOf<String>()
        val companies = mutableListOf<String>()
        for (i in 0 until hobbysObj.length()) {
            hobbys.add(hobbysObj.getString(i))
        }
        for (i in 0 until companiesObj.length()) {
            companies.add(companiesObj.getString(i))
        }
        return Pair(hobbys, companies)
    }

    fun createRandomUsers(applicationContext: Context, count: Int) {
        val randomNames = mutableListOf<String>()
        applicationContext.resources.assets.open("random-names.txt").bufferedReader().use {
            randomNames.addAll(it.readText().split("\n"))
        }
        if (count > randomNames.size) {
            println("Can only create a maximum of ${randomNames.size} users!")
            return
        }
        val fakultaetJson = applicationContext.resources.assets.open("fakultaet-mapping.json").bufferedReader().use { it.readText() }
        val fakultaetJsonObj = JSONObject(fakultaetJson)
        val fakultaet_list = ArrayList<String>()
        for (key in fakultaetJsonObj.keys()) {
            fakultaet_list.add(key)
        }
        val mapping = HashMap<String, ArrayList<String>>()
        for (fakultaet in fakultaet_list) {
            val studiengaenge = fakultaetJsonObj.getJSONArray(fakultaet)
            val list = ArrayList<String>()
            for (i in 0 until studiengaenge.length()) {
                list.add(studiengaenge.getString(i))
            }
            mapping.put(fakultaet, list)
        }
        val availableTags = getAvailableTags(applicationContext)
        val allTags = availableTags.first + availableTags.second
        val json = applicationContext.resources.assets.open("available_tags.json").bufferedReader().use { it.readText() }
        val tagsFileObj = JSONObject(json)
        Thread {
            for (i in 0 until count) {
                var name = randomNames[i]
                name = name.substring(0, name.length - 1)
                val email = "$name@lehre.dhbw-stuttgart.de"
                var rndmIdx = random.nextInt(fakultaet_list.size)
                val fakultaet = fakultaet_list[rndmIdx]
                if (!mapping.containsKey(fakultaet)) {
                    continue
                }
                rndmIdx = mapping.get(fakultaet)?.size?.let { random.nextInt(it) }!!
                val studiengang = mapping.get(fakultaet)!!.get(rndmIdx)
                val jahrgang = random.nextInt(8) + 2018 // 2018 - 2025
                val uuid = UUID.randomUUID()
                val user = getUserObj(uuid, name, email, hash("pw"), fakultaet, studiengang, jahrgang)
                user.put("phone", random.nextInt(10000000)+10000000)
                saveUser(applicationContext, user)
                // Set random tags
                val activeTags: JSONArray
                if (tagsFileObj.has(uuid.toString())) {
                    activeTags = tagsFileObj.getJSONArray(uuid.toString())
                } else {
                    activeTags = JSONArray()
                }
                val selectedTags = mutableListOf<String>()
                for (i in 0 until activeTags.length()) {
                    selectedTags.add(activeTags.getString(i))
                }
                val rndTagsCount = random.nextInt(allTags.size / 4) + 1
                for (i in 0 until rndTagsCount) {
                    var currentTag: String
                    do {
                        currentTag =  allTags[random.nextInt(allTags.size)]
                    } while (selectedTags.contains(currentTag))
                    selectedTags.add(currentTag)
                    activeTags.put(currentTag)
                }
                tagsFileObj.put(uuid.toString(), activeTags)
                // Set random profile picture
                val imgURL = "https://thispersondoesnotexist.com/"
                val file = File(applicationContext.filesDir, "pp-$uuid")
                file.createNewFile()
                try {
                    val input = URL(imgURL).openStream()
                    file.outputStream().use { output ->
                        input?.copyTo(output)
                    }
                } catch (e: Exception) {
                    println("Could not load image from URL!")
                }
                Thread.sleep(100)
            }
            FileUtil.writeJSON("tags.json", tagsFileObj, applicationContext)
        }.start()
    }

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
        println("Create user $user")
    }

    fun getLocalUserUUID(applicationContext: Context) : String? {
        val localUser = FileUtil.readJSON("local-user.json", applicationContext)
        if (!localUser.has("uuid") || localUser.get("uuid") == "") {
            return null
        }
        return localUser.getString("uuid")
    }

    fun getUserTags(applicationContext: Context, uuid: String) : JSONArray {
        val tagsFileObj = FileUtil.readJSON("tags.json", applicationContext)
        if (!tagsFileObj.has(uuid)) {
            return JSONArray()
        }
        return tagsFileObj.getJSONArray(uuid)
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
                if (bitmap != null) {
                    return bitmap
                }
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
        jahrgang: Int
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

    fun getAllUsers(applicationContext: Context, includeLocalUser: Boolean): List<JSONObject> {
        val users = FileUtil.readJSON("users.json", applicationContext)
        val userList = mutableListOf<JSONObject>()
        val localUUID = getLocalUserUUID(applicationContext)
        println("users: $users")
        for (key in users.keys()) {
            val user = users.get(key) as JSONObject
            println("key: $key user: $user")
            if (user.getString("uuid").equals(localUUID) && !includeLocalUser) {
                continue
            }
            userList.add(user)
        }
        return userList
    }

    /*fun getAllUserUUIDs(applicationContext: Context): List<String> {
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
    }*/
}