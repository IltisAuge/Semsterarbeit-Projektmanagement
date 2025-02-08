package de.dhbwstuttgart.semesterarbeit_projektmanagement.login_register

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import de.dhbwstuttgart.semesterarbeit_projektmanagement.FileUtil
import de.dhbwstuttgart.semesterarbeit_projektmanagement.R
import de.dhbwstuttgart.semesterarbeit_projektmanagement.databinding.ActivityTestHomeBinding
import de.dhbwstuttgart.semesterarbeit_projektmanagement.profile_settings.ProfileSettingsActivity
import org.json.JSONObject

class TestHomeActivity : Fragment() {

    private var _binding: ActivityTestHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    //private lateinit var binding: ActivityTestHomeBinding
    private lateinit var list: ArrayList<String>
    private lateinit var listView: ListView
    private lateinit var listAdapter: ArrayAdapter<String>

    /*override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return keyCode != KeyEvent.KEYCODE_BACK
    }*/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityTestHomeBinding.inflate(inflater, container, false)
        val uuid = UserUtils.getLocalUserUUID(requireContext())
        if (uuid == null) {
            return binding.root
        }
        val user = UserUtils.getUserbyUUID(requireContext(), uuid)
        if (user == null) {
            return binding.root
        }

        val headline = binding.homeHeadline
        headline.setTitle("Willkommen ${user.getString("name")}")

        val profileImg = binding.profilePicture
        val bitmap = user.getString("uuid")
            ?.let { UserUtils.getProfilePictureBitmap(requireContext(), resources, it) }
        profileImg.setImageBitmap(bitmap)

        listView = binding.itemlist
        list = ArrayList()
        list.add("Informationen über dein Konto:")
        list.add("Email: ${user.getString("email")}")
        list.add("Fakultät: ${user.getString("fakultaet")}")
        list.add("Studiengang: ${user.getString("studiengang")}")
        list.add("Jahrgang: ${user.getString("jahrgang")}")
        list.add("Passwort Hash: ${user.getString("password")}")
        listAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, list)
        listView.adapter = listAdapter

        val settingsButton = binding.settingsButton
        settingsButton.setOnClickListener {
            startActivity(Intent(requireContext(), ProfileSettingsActivity::class.java))
        }

        val logoutButton = binding.logoutButton
        logoutButton.setOnClickListener {
            val jsonObj = JSONObject()
            jsonObj.put("uuid", null)
            FileUtil.writeJSON("local-user.json", jsonObj, requireContext())
            Toast.makeText(requireContext(), "Du wurdest abgemeldet", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uuid = UserUtils.getLocalUserUUID(applicationContext)
        if (uuid == null) {
            return
        }
        val user = UserUtils.getUserbyUUID(applicationContext, uuid)
        if (user == null) {
            return
        }

        val headline = binding.homeHeadline
        headline.setTitle("Willkommen ${user.getString("name")}")

        val profileImg = binding.profilePicture
        val bitmap = user.getString("uuid")
            ?.let { UserUtils.getProfilePictureBitmap(applicationContext, resources, it) }
        profileImg.setImageBitmap(bitmap)

        listView = findViewById(R.id.itemlist)
        list = ArrayList()
        list.add("Informationen über dein Konto:")
        list.add("Email: ${user.getString("email")}")
        list.add("Fakultät: ${user.getString("fakultaet")}")
        list.add("Studiengang: ${user.getString("studiengang")}")
        list.add("Jahrgang: ${user.getString("jahrgang")}")
        list.add("Passwort Hash: ${user.getString("password")}")
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
    }*/
}