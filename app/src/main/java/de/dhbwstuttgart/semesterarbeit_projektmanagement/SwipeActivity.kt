package de.dhbwstuttgart.semesterarbeit_projektmanagement

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import de.dhbwstuttgart.semesterarbeit_projektmanagement.databinding.ActivitySwipeBinding
import de.dhbwstuttgart.semesterarbeit_projektmanagement.login_register.LoginActivity
import de.dhbwstuttgart.semesterarbeit_projektmanagement.login_register.UserUtils
import de.dhbwstuttgart.semesterarbeit_projektmanagement.profile_settings.ProfileSettingsActivity
import org.json.JSONObject
import kotlin.random.Random

class SwipeActivity : Fragment() {

    private var _binding: ActivitySwipeBinding? = null

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
        _binding = ActivitySwipeBinding.inflate(inflater, container, false)
        val uuid = UserUtils.getLocalUserUUID(requireContext())
        if (uuid == null) {
            return binding.root
        }
        val user = UserUtils.getUserbyUUID(requireContext(), uuid)
        if (user == null) {
            return binding.root
        }

        val headline = binding.homeHeadline
        headline.setTitle(user.getString("name"))

        val profileImg = binding.profilePicture
        val bitmap = user.getString("uuid")
            ?.let { UserUtils.getProfilePictureBitmap(requireContext(), resources, it) }
        profileImg.setImageBitmap(bitmap)

        listView = binding.itemlist
        list = ArrayList()
        list.add("Email: ${user.getString("email")}")
        list.add("Fakultät: ${user.getString("fakultaet")}")
        list.add("Studiengang: ${user.getString("studiengang")}")
        list.add("Jahrgang: ${user.getString("jahrgang")}")
        list.add("Tags: ${user.getString("tags")}")
        listAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, list)
        listView.adapter = listAdapter

        var prevUUID = ""
        val settingsButton = binding.nextUserButton
        settingsButton.setOnClickListener {
            val users = UserUtils.getAllUserUUIDsExceptLocal(requireContext())
            for (uID in users){
                UserUtils.getUserbyUUID(requireContext(), uID)
                    ?.let { it1 ->println((it1.getString("name"))) }
            }
            var randomIndex: Int
            var randomUUID: String
            do {
                randomIndex = Random.nextInt(users.size-1)
                randomUUID = users[randomIndex+1]
            } while (randomUUID == prevUUID)

            println("Test " + randomUUID)
            println("Prev:" + prevUUID)

            list.clear()
            val user = UserUtils.getUserbyUUID(requireContext(), randomUUID)

            if (user != null)  {
                val headline = binding.homeHeadline
                headline.setTitle(user.getString("name"))
                val profileImg = binding.profilePicture
                val bitmap = user.getString("uuid")
                    ?.let { UserUtils.getProfilePictureBitmap(requireContext(), resources, it) }
                profileImg.setImageBitmap(bitmap)
                listView = binding.itemlist
                list = ArrayList()
                list.add("Email: ${user.getString("email")}")
                list.add("Fakultät: ${user.getString("fakultaet")}")
                list.add("Studiengang: ${user.getString("studiengang")}")
                list.add("Jahrgang: ${user.getString("jahrgang")}")
                listAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, list)
                listView.adapter = listAdapter
                prevUUID = randomUUID
            }

        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}