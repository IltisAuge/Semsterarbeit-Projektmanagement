package de.dhbwstuttgart.semesterarbeit_projektmanagement

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import de.dhbwstuttgart.semesterarbeit_projektmanagement.databinding.ActivityTestHomeBinding
import de.dhbwstuttgart.semesterarbeit_projektmanagement.login_register.LoginActivity
import de.dhbwstuttgart.semesterarbeit_projektmanagement.login_register.UserUtils
import org.json.JSONObject
import kotlin.random.Random

class HomeActivity : Fragment() {

    private var _binding: ActivityTestHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    //private lateinit var binding: ActivityTestHomeBinding
    private lateinit var list: ArrayList<String>
    private lateinit var listView: ListView
    private lateinit var listAdapter: ArrayAdapter<String>

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
        list.add("")
        list.add("Du hast 7 neue Nachrichten!")
        list.add("")
        list.add("Informationen über dein Konto:")
        list.add("Email: ${user.getString("email")}")
        list.add("Fakultät: ${user.getString("fakultaet")}")
        list.add("Studiengang: ${user.getString("studiengang")}")
        list.add("Jahrgang: ${user.getString("jahrgang")}")
        listAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, list)
        listView.adapter = listAdapter

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
}