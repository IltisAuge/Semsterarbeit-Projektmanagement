package de.dhbwstuttgart.semesterarbeit_projektmanagement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import de.dhbwstuttgart.semesterarbeit_projektmanagement.databinding.ActivitySwipeBinding
import de.dhbwstuttgart.semesterarbeit_projektmanagement.login_register.UserUtils
import org.json.JSONObject
import java.io.File
import kotlin.random.Random

class SwipeActivity : Fragment() {

    private var _binding: ActivitySwipeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    //private lateinit var binding: ActivityTestHomeBinding
    private lateinit var list: MutableList<String>
    private lateinit var listView: ListView
    private lateinit var listAdapter: ArrayAdapter<String>
    private lateinit var headline: MaterialToolbar
    private lateinit var profileImg: ImageView
    private var prevIdx = 0

    /*override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return keyCode != KeyEvent.KEYCODE_BACK
    }*/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivitySwipeBinding.inflate(inflater, container, false)
        getTagsFile() // Create file if not exists
        list = mutableListOf()
        listView = binding.itemlist
        listAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, list)
        listView.adapter = listAdapter
        headline = binding.homeHeadline
        profileImg = binding.profilePicture

        var prevUUID = selectNewUser(null)

        val settingsButton = binding.nextUserButton
        settingsButton.setOnClickListener {
            prevUUID = selectNewUser(prevUUID)
        }
        return binding.root
    }

    fun getTagsFile() : File {
        val tagsFile = File(requireContext().filesDir, "tags.json")
        if (!tagsFile.exists()) {
            tagsFile.createNewFile()
            tagsFile.writeText("{ }")
        }
        return tagsFile
    }

    fun selectNewUser(prevUUID: String?) : String? {
        val users = UserUtils.getAllUsers(requireContext(), false)
        for (user in users) {
            // Debug
            println(user.getString("name"))
        }
        //var randomIndex: Int = 0
        var randomUser: JSONObject? = null
        do {
            if (prevIdx > users.size-1) {
                prevIdx = 0
            }
            println("randomIndex: $prevIdx")
            randomUser = users[prevIdx]
            prevIdx++//Random.nextInt(users.size-1)
        } while (prevUUID != null && randomUser != null && randomUser.get("uuid") == prevUUID)
        if (randomUser == null) {
            println("No user found!")
            return null
        }

        println("Test " + randomUser.get("uuid"))
        println("Prev:" + prevUUID)

        headline.setTitle(randomUser.getString("name"))
        val bitmap = randomUser.getString("uuid")
            ?.let { UserUtils.getProfilePictureBitmap(requireContext(), resources, it) }
        profileImg.setImageBitmap(bitmap)
        list.clear()
        list.addAll(getUserInformation(randomUser))
        println("userInformation: $list")
        listAdapter.notifyDataSetChanged()
        return randomUser.getString("uuid")
    }

    fun getUserInformation(randomUser: JSONObject) : MutableList<String> {
        val list = mutableListOf<String>()
        list.add("Email: ${randomUser.getString("email")}")
        if (randomUser.has("phone")) {
            list.add("Telefonnummer: ${randomUser.getString("phone")}")
        }
        list.add("Fakultät: ${randomUser.getString("fakultaet")}")
        list.add("Studiengang: ${randomUser.getString("studiengang")}")
        list.add("Jahrgang: ${randomUser.getString("jahrgang")}")
        val tagsString = StringBuilder()
        val userTags = UserUtils.getUserTags(requireContext(), randomUser.getString("uuid"))
        for (i in 0 until userTags.length()) {
            tagsString.append(userTags[i])
            if (i != userTags.length() - 1) {
                tagsString.append(", ")
            }
        }
        list.add("Interessen: $tagsString")
        return list
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}