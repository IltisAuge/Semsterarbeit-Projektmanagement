package de.dhbwstuttgart.semesterarbeit_projektmanagement.login_register

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.activity.ComponentActivity
import de.dhbwstuttgart.semesterarbeit_projektmanagement.FileUtil
import de.dhbwstuttgart.semesterarbeit_projektmanagement.R
import de.dhbwstuttgart.semesterarbeit_projektmanagement.databinding.ActivityTestHomeBinding
import de.dhbwstuttgart.semesterarbeit_projektmanagement.profile_settings.ProfileSettingsActivity
import org.json.JSONObject

class TestHomeActivity : ComponentActivity() {

    private lateinit var binding: ActivityTestHomeBinding
    private lateinit var list: ArrayList<String>
    private lateinit var listView: ListView
    private lateinit var listAdapter: ArrayAdapter<String>

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return keyCode != KeyEvent.KEYCODE_BACK
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val headline = binding.homeHeadline
        headline.setTitle("Willkommen ${intent.getStringExtra("name")}")

        val profileImg = binding.profilePicture
        val bitmap = UserUtils.getProfilePictureBitmap(applicationContext, resources)
        profileImg.setImageBitmap(bitmap)

        listView = findViewById(R.id.itemlist)
        list = ArrayList()
        list.add("Informationen über dein Konto:")
        list.add("Email: ${intent.getStringExtra("email")}")
        list.add("Fakultät: ${intent.getStringExtra("fakultaet")}")
        list.add("Studiengang: ${intent.getStringExtra("studiengang")}")
        list.add("Jahrgang: ${intent.getStringExtra("jahrgang")}")
        list.add("Passwort Hash: ${intent.getStringExtra("password")}")
        listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        listView.adapter = listAdapter

        val settingsButton = binding.settingsButton
        settingsButton.setOnClickListener {
            startActivity(Intent(applicationContext, ProfileSettingsActivity::class.java))
            finish()
        }

        val logoutButton = binding.logoutButton
        logoutButton.setOnClickListener {
            val jsonObj = JSONObject()
            jsonObj.put("uuid", null)
            FileUtil.writeJSON("local-user.json", jsonObj, applicationContext)
            Toast.makeText(applicationContext, "Du wurdest abgemeldet", Toast.LENGTH_SHORT).show()
            startActivity(Intent(applicationContext, LoginActivity::class.java))
            finish()
        }
    }
}