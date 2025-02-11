package de.dhbwstuttgart.semesterarbeit_projektmanagement.login_register

import android.content.Intent
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.dhbwstuttgart.semesterarbeit_projektmanagement.FileUtil
import de.dhbwstuttgart.semesterarbeit_projektmanagement.R
import de.dhbwstuttgart.semesterarbeit_projektmanagement.databinding.ActivityMainBinding
import de.dhbwstuttgart.semesterarbeit_projektmanagement.databinding.ActivityNavigationMainBinding
import org.json.JSONObject
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(window) {
            requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
            exitTransition = android.transition.Slide()
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Create file to store all users
        // This file should be migrated to a database in a real-world application
        println("Start Main")
        val usersFile = File(applicationContext.filesDir, "users.json")
        println("users exists? ${usersFile.exists()}")
        usersFile.delete() // TODO: Remove
        if (!usersFile.exists()) {
            usersFile.createNewFile()
            usersFile.writeText("{ }")
            println("Created users.json")
            UserUtils.createRandomUsers(applicationContext)
        }

        // Fix users.json
        // TODO: Remove in production
        val usersObj = FileUtil.readJSON("users.json", applicationContext)
        usersObj.remove("users")
        FileUtil.writeJSON("users.json", usersObj, applicationContext)
        File(applicationContext.filesDir, "tags.json").delete()

        // Check if local-user.json file exists and contains "uuid"
        val file = File(applicationContext.filesDir, "local-user.json")
        file.delete() // TODO: Remove
        if (file.exists()) {
            val jsonObj = JSONObject(file.readText())
            println(jsonObj)
            if (jsonObj.has("uuid")) {
                println("user is in local file: uuid ${jsonObj.getString("uuid")}")
                // User has an account/is logged in: Start HomeActivity with "uuid" extra
                val user = UserUtils.getUserbyUUID(applicationContext, jsonObj.getString("uuid"))
                if (user == null) {
                    return
                }
                val intent = UserUtils.loginAndGetIntent(applicationContext, user)
                intent.putExtra("uuid", jsonObj.getString("uuid"))
                startActivity(intent)
                return
            }
        }
        file.createNewFile()
        file.writeText("{ }")
        // User has no account/is not logged in: Start LoginActivity
        println("Start LoginActivity")
        startActivity(Intent(applicationContext, LoginActivity::class.java))
        finish()
    }
}