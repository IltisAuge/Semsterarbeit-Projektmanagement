package de.dhbwstuttgart.semesterarbeit_projektmanagement.profile_settings

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.dhbwstuttgart.semesterarbeit_projektmanagement.FileUtil
import de.dhbwstuttgart.semesterarbeit_projektmanagement.R
import de.dhbwstuttgart.semesterarbeit_projektmanagement.databinding.ActivityProfileSettingsBinding
import de.dhbwstuttgart.semesterarbeit_projektmanagement.login_register.UserUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.File


class ProfileSettingsActivity : Fragment() {

    private lateinit var binding: ActivityProfileSettingsBinding
    private lateinit var tagsListView: LinearLayout
    private lateinit var addTagBtn: FloatingActionButton
    private lateinit var activeTagsList: ArrayList<String>
    private lateinit var chooseImgBtn: Button
    private lateinit var deleteImgBtn: Button
    private lateinit var profileImg: ImageView
    private lateinit var emailInput: EditText
    private lateinit var phoneInput: EditText

    private lateinit var hobbys: List<String> /*= arrayListOf(
        "Acroyoga", "Apnoetauchen", "Badminton", "Baseball", "Basketball", "Bauchtanz", "Bergsteigen", "BMX", "Bodybuilding", "Boxen", "Cheerleading", "Darts",
        "Eishockey", "Eiskunstlaufen", "Skateboard", "Einrad", "Fahrrad", "Fallschirmspringen", "Fechten", "Fitness", "Football", "Fußball", "Golf", "Hobby Horsing", "Hula Hoop", "Inline Skates", "Joggen", "Kite Surfen",
        "Longboard", "Paintball", "Parkour", "Motorrad", "Pilates", "Reiten", "Rudern", "Gaming", "Reisen", "Kochen", "Backen", "Ski", "Snowboard", "Segeln", "Schwimmen", "Tanzen", "Tennis", "Tauchen",
        "Triathlon", "Leichtathletik"
    )*/

    private lateinit var companies: List<String> /*= arrayListOf("Bosch", "Allianz", "Telekom", "Porsche", "Daimler")*/

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) : View {
        binding = ActivityProfileSettingsBinding.inflate(inflater)
        val uuid = UserUtils.getLocalUserUUID(requireContext())
        if (uuid == null) {
            println("An error occurred! Local users uuid is null!")
            return binding.root
        }
        val user = UserUtils.getUserbyUUID(requireContext(), uuid)
        if (user == null) {
            println("An error occurred! Local user is null!")
            return binding.root
        }
        val availableTags = UserUtils.getAvailableTags(requireContext())
        hobbys = availableTags.first
        companies = availableTags.second
        chooseImgBtn = binding.chooseProfilePictureBtn
        deleteImgBtn = binding.deleteProfilePictureBtn
        profileImg = binding.profilePicture
        setupProfilePictureBtn(uuid)
        setupDeleteProfilePictureBtn(uuid)

        val bitmap = UserUtils.getProfilePictureBitmap(requireContext(), resources, uuid)
        profileImg.setImageBitmap(bitmap)

        emailInput = binding.editTextEmail
        phoneInput = binding.editTextPhone
        emailInput.setText(user.getString("email"))
        if (user.has("phone")) {
            phoneInput.setText(user.getString("phone"))
        }
        addEmailPhoneInputListener(emailInput, user, "email")
        addEmailPhoneInputListener(phoneInput, user, "phone")

        tagsListView = binding.tagsList
        addTagBtn = binding.TagAdd
        activeTagsList = loadSavedTags()
        activeTagsList.add("DHBW Student")
        setupTagsListView()

        addTagBtn.setOnClickListener {
            openAvailableTagsDialog()
        }
        return binding.root
    }

    fun setupTagsListView() {
        tagsListView.removeAllViews()
        for ((idx, item) in activeTagsList.withIndex()) {
            println("Added tag item $item")
            // Item layout
            val itemView = TextView(requireContext())
            // Long click listener to delete items
            itemView.setOnLongClickListener {
                // Prevent the deletion of the "DHBW Student" tag
                if (item == "DHBW Student") {
                    Toast.makeText(requireContext(), "Du kannst diesen Tag nicht löschen!", Toast.LENGTH_SHORT).show()
                    return@setOnLongClickListener true
                }
                openDeleteConfirmationDialog(item)
                return@setOnLongClickListener true
            }
            itemView.setPadding(30, if (idx == 0) 60 else 0, 20, if (idx == activeTagsList.size-1) 120 else 60)
            itemView.setTextSize(15f)
            itemView.setText(item)
            tagsListView.addView(itemView)
        }
    }

    fun addEmailPhoneInputListener(view: EditText, user: JSONObject, field: String) {
        view.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                val s = v.text
                // Save changed email data to file
                user.put(field, s.toString())
                UserUtils.saveUser(requireContext(), user)
                Toast.makeText(requireContext(), "Gespeichert!", Toast.LENGTH_SHORT).show()
            }
            true
        }
    }

    fun setupProfilePictureBtn(uuid: String) {
        val resultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri == null) return@registerForActivityResult
            val file = File(requireContext().filesDir, "pp-$uuid")
            file.createNewFile()
            uri.let { requireContext().contentResolver.openInputStream(it) }.use { input ->
                file.outputStream().use { output ->
                    input?.copyTo(output)
                }
            }
            profileImg.setImageURI(uri)
            Toast.makeText(requireContext(), "Profilbild wurde geändert", Toast.LENGTH_SHORT).show()
        }
        chooseImgBtn.setOnClickListener {
            resultLauncher.launch("image/*")
        }
    }

    fun setupDeleteProfilePictureBtn(uuid: String) {
        deleteImgBtn.setOnClickListener {
            val file = File(requireContext().filesDir, "pp-$uuid")
            if (file.exists()) {
                file.delete()
            }
            val confirmDialog = AlertDialog.Builder(requireContext())
            confirmDialog.setTitle("Löschen")
            confirmDialog.setMessage("Möchtest du dein Profilbild wirklich löschen?")
            confirmDialog.setPositiveButton("Ja") { dialog, which ->
                profileImg.setImageResource(R.mipmap.blank_pp)
                Toast.makeText(requireContext(), "Dein Profilbild wurde entfernt", Toast.LENGTH_SHORT).show()
            }
            confirmDialog.setNegativeButton("Nein") { dialog, which -> }
            confirmDialog.show()
        }
    }

    fun openAvailableTagsDialog() {
        var availableTags = filterAvailableTags()
        val availableTagsDialogBuilder = AlertDialog.Builder(requireContext())
        val availableTagsView = ListView(requireContext())
        val availableTagsAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, availableTags)

        availableTagsView.adapter = availableTagsAdapter
        availableTagsDialogBuilder.setView(availableTagsView)
        availableTagsDialogBuilder.setTitle("Verfügbare Tags")
        lateinit var availableTagsDialog: AlertDialog

        availableTagsView.setOnItemClickListener { parent, view, position, id ->
            val confirmDialog = AlertDialog.Builder(requireContext())
            val tag = availableTags.get(position)
            confirmDialog.setTitle("Tag hinzufügen")
            confirmDialog.setMessage("Möchtest du den Tag '$tag' hinzufügen?")
            confirmDialog.setPositiveButton("Ja") { dialog, which ->
                activeTagsList.add(tag)
                saveTag(tag)
                setupTagsListView()
                if (companies.contains(tag)) {
                    Toast.makeText(requireContext(), "Unternehmens-Tag '$tag' wurde hinzugefügt", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Tag '$tag' wurde hinzugefügt", Toast.LENGTH_SHORT).show()
                }
                availableTagsDialog.dismiss()
            }
            confirmDialog.setNegativeButton("Nein") { dialog, which -> }
            confirmDialog.show()
        }
        availableTagsDialogBuilder.setPositiveButton("Schließen") { dialog, which -> }
        availableTagsDialog = availableTagsDialogBuilder.create()
        availableTagsDialog.show()
    }

    fun getTagsFile() : File {
        val tagsFile = File(requireContext().filesDir, "tags.json")
        if (!tagsFile.exists()) {
            tagsFile.createNewFile()
            tagsFile.writeText("{ }")
        }
        return tagsFile
    }

    fun loadSavedTags() : ArrayList<String> {
        val list = ArrayList<String>()
        val tagsFile = getTagsFile()
        val uuid = UserUtils.getLocalUserUUID(requireContext())
        if (uuid == null) {
            return list
        }
        val fileObj = JSONObject(tagsFile.readText())
        if (!fileObj.has(uuid)) {
            return list
        }
        val activeTags = fileObj.getJSONArray(uuid)
        for (tag in 0 until activeTags.length()) {
            list.add(activeTags[tag].toString())
        }
        return list
    }

    fun saveTag(tag: String) {
        val tagsFile = getTagsFile()
        val uuid = UserUtils.getLocalUserUUID(requireContext())
        if (uuid == null) {
            return
        }
        val fileObj = JSONObject(tagsFile.readText())
        val activeTags: JSONArray
        if (fileObj.has(uuid)) {
            activeTags = fileObj.getJSONArray(uuid)
        } else {
            activeTags = JSONArray()
        }
        activeTags.put(tag)
        fileObj.put(uuid, activeTags)
        FileUtil.writeJSON("tags.json", fileObj, requireContext())
    }

    fun deleteTag(tag: String) {
        val tagsFile = getTagsFile()
        val uuid = UserUtils.getLocalUserUUID(requireContext())
        if (uuid == null) {
            return
        }
        val fileObj = JSONObject(tagsFile.readText())
        if (!fileObj.has(uuid)) {
            return
        }
        val listCpy = ArrayList<String>()
        val newTagsObj = JSONArray()
        for (i in 0 until activeTagsList.size) {
            if (activeTagsList[i] == tag) {
                activeTagsList[i] = ""
            } else if (activeTagsList[i] != "DHBW Student") {
                listCpy.add(activeTagsList[i])
                newTagsObj.put(activeTagsList[i])
            }
        }
        activeTagsList = listCpy
        activeTagsList.add("DHBW Student")
        fileObj.put(uuid, newTagsObj)
        FileUtil.writeJSON("tags.json", fileObj, requireContext())
    }

    fun filterAvailableTags() : List<String> {
        val hasCompanyTag = activeTagsList.any { companies.contains(it) }
        var availableTags = (hobbys + companies).toMutableList().filter { tag -> !activeTagsList.contains(tag) }.sorted()
        var size = availableTags.size
        var newList = mutableListOf<String>()
        for (i in 0 until size) {
            val tag = availableTags.get(i)
            if (!(companies.contains(tag) && hasCompanyTag)) {
                newList.add(tag)
            }
        }
        return newList
    }

    fun openDeleteConfirmationDialog(tagToRemove: String) : Boolean {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Tag entfernen")
        builder.setMessage("Willst du den Tag '$tagToRemove' entfernen?")
        builder.setPositiveButton("Ja") { dialog, which ->
            deleteTag(tagToRemove)
            setupTagsListView()
            Toast.makeText(requireContext(), "Tag '$tagToRemove' wurde entfernt", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("Nein") { dialog, which -> }
        builder.show()
        return true  // Return true to indicate that the long click event was handled
    }
}