package de.dhbwstuttgart.semesterarbeit_projektmanagement.profile_settings

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.dhbwstuttgart.semesterarbeit_projektmanagement.FileUtil
import de.dhbwstuttgart.semesterarbeit_projektmanagement.R
import de.dhbwstuttgart.semesterarbeit_projektmanagement.databinding.ActivityProfileSettingsBinding
import de.dhbwstuttgart.semesterarbeit_projektmanagement.login_register.UserUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.File


class ProfileSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileSettingsBinding
    private lateinit var tagsListView: LinearLayout
    private lateinit var addTagBtn: FloatingActionButton
    private lateinit var activeTagsList: ArrayList<String>
    //private lateinit var tagListAdapter: ArrayAdapter<String>
    private lateinit var chooseImgBtn: Button
    private lateinit var deleteImgBtn: Button
    private lateinit var profileImg: ImageView
    private lateinit var emailInput: EditText
    private lateinit var phoneInput: EditText

    private var hobbys: ArrayList<String> = arrayListOf(
        "Acroyoga", "Apnoetauchen", "Badminton", "Baseball", "Basketball", "Bauchtanz", "Bergsteigen", "BMX", "Bodybuilding", "Boxen", "Cheerleading", "Darts",
        "Eishockey", "Eiskunstlaufen", "Skateboard", "Einrad", "Fahrrad", "Fallschirmspringen", "Fechten", "Fitness", "Football", "Fußball", "Golf", "Hobby Horsing", "Hula Hoop", "Inline Skates", "Joggen", "Kite Surfen",
        "Longboard", "Paintball", "Parkour", "Motorrad", "Pilates", "Reiten", "Rudern", "Gaming", "Reisen", "Kochen", "Backen", "Ski", "Snowboard", "Segeln", "Schwimmen", "Tanzen", "Tennis", "Tauchen",
        "Triathlon", "Leichtathletik"
    )

    private var companies: ArrayList<String> = arrayListOf("Bosch", "Allianz", "Telekom", "Porsche", "Daimler")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uuid = UserUtils.getLocalUserUUID(applicationContext)
        if (uuid == null) {
            println("An error occurred! Local users uuid is null!")
            return
        }
        val user = UserUtils.getUserbyUUID(applicationContext, uuid)
        if (user == null) {
            println("An error occurred! Local user is null!")
            return
        }
        binding = ActivityProfileSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chooseImgBtn = binding.chooseProfilePictureBtn
        deleteImgBtn = binding.deleteProfilePictureBtn
        profileImg = binding.profilePicture
        setupProfilePictureBtn(uuid)
        setupDeleteProfilePictureBtn(uuid)

        val bitmap = UserUtils.getProfilePictureBitmap(applicationContext, resources, uuid)
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
        addTagBtn = findViewById(R.id.TagAdd)
        activeTagsList = loadSavedTags()

        //tagListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, activeTagsList)
        //tagsListView.setAda = tagListAdapter
        activeTagsList.add("DHBW Student")
        setupTagsListView()

        addTagBtn.setOnClickListener {
            openAvailableTagsDialog()
        }
    }

    fun setupTagsListView() {
        println("setup $activeTagsList")
        tagsListView.removeAllViews()
        for ((idx, item) in activeTagsList.withIndex()) {
            // Item layout
            val itemView = TextView(applicationContext)
            // Long click listener to delete items
            itemView.setOnLongClickListener {
                // Prevent the deletion of the "DHBW Student" tag
                if (item == "DHBW Student") {
                    Toast.makeText(applicationContext, "Du kannst diesen Tag nicht löschen!", Toast.LENGTH_SHORT).show()
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
            println("actionId=$actionId")
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {
                val s = v.text
                println("s=$s")
                // Save changed email data to file
                user.put(field, s.toString())
                println("user: $user")
                UserUtils.saveUser(applicationContext, user)
                Toast.makeText(applicationContext, "Gespeichert!", Toast.LENGTH_SHORT).show()
            }
            true
        }
    }

    fun setupProfilePictureBtn(uuid: String) {
        val resultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri == null) return@registerForActivityResult
            val file = File(applicationContext.filesDir, "pp-$uuid")
            file.createNewFile()
            uri.let { applicationContext.contentResolver.openInputStream(it) }.use { input ->
                file.outputStream().use { output ->
                    input?.copyTo(output)
                }
            }
            profileImg.setImageURI(uri)
            Toast.makeText(applicationContext, "Profilbild wurde geändert", Toast.LENGTH_SHORT).show()
        }
        chooseImgBtn.setOnClickListener {
            resultLauncher.launch("image/*")
        }
    }

    fun setupDeleteProfilePictureBtn(uuid: String) {
        deleteImgBtn.setOnClickListener {
            val file = File(applicationContext.filesDir, "pp-$uuid")
            if (file.exists()) {
                file.delete()
            }
            val confirmDialog = AlertDialog.Builder(this)
            confirmDialog.setTitle("Löschen")
            confirmDialog.setMessage("Möchtest du dein Profilbild wirklich löschen?")
            confirmDialog.setPositiveButton("Ja") { dialog, which ->
                profileImg.setImageResource(R.mipmap.blank_pp)
                Toast.makeText(applicationContext, "Dein Profilbild wurde entfernt", Toast.LENGTH_SHORT).show()
            }
            confirmDialog.setNegativeButton("Nein") { dialog, which -> }
            confirmDialog.show()
        }
    }

    fun openAvailableTagsDialog() {
        var availableTags = filterAvailableTags()
        println("Available: $availableTags")

        val availableTagsDialogBuilder = AlertDialog.Builder(this)
        val availableTagsView = ListView(this)
        val availableTagsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, availableTags)

        availableTagsView.adapter = availableTagsAdapter
        availableTagsDialogBuilder.setView(availableTagsView)
        availableTagsDialogBuilder.setTitle("Verfügbare Tags")
        lateinit var availableTagsDialog: AlertDialog

        availableTagsView.setOnItemClickListener { parent, view, position, id ->
            val confirmDialog = AlertDialog.Builder(this)
            val tag = availableTags.get(position)
            confirmDialog.setTitle("Tag hinzufügen")
            confirmDialog.setMessage("Möchtest du den Tag '$tag' hinzufügen?")
            confirmDialog.setPositiveButton("Ja") { dialog, which ->
                activeTagsList.add(tag)
                saveTag(tag)
                setupTagsListView()
                if (companies.contains(tag)) {
                    Toast.makeText(applicationContext, "Unternehmens-Tag '$tag' wurde hinzugefügt", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, "Tag '$tag' wurde hinzugefügt", Toast.LENGTH_SHORT).show()
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
        val tagsFile = File(applicationContext.filesDir, "tags.json")
        if (!tagsFile.exists()) {
            tagsFile.createNewFile()
            tagsFile.writeText("{ }")
        }
        return tagsFile
    }

    fun loadSavedTags() : ArrayList<String> {
        val list = ArrayList<String>()
        val tagsFile = getTagsFile()
        val uuid = UserUtils.getLocalUserUUID(applicationContext)
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
        val uuid = UserUtils.getLocalUserUUID(applicationContext)
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
        FileUtil.writeJSON("tags.json", fileObj, applicationContext)
    }

    fun deleteTag(tag: String) {
        val tagsFile = getTagsFile()
        val uuid = UserUtils.getLocalUserUUID(applicationContext)
        if (uuid == null) {
            return
        }
        val fileObj = JSONObject(tagsFile.readText())
        if (!fileObj.has(uuid)) {
            return
        }
        println("Deleting tag $tag...")
        val listCpy = ArrayList<String>()
        val newTagsObj = JSONArray()
        for (i in 0 until activeTagsList.size) {
            if (activeTagsList[i] == tag) {
                println("Set tag ${activeTagsList[i]} to space")
                activeTagsList[i] = ""
            } else if (activeTagsList[i] != "DHBW Student") {
                println("Copy tag ${activeTagsList[i]}")
                listCpy.add(activeTagsList[i])
                newTagsObj.put(activeTagsList[i])
            }
        }
        activeTagsList = listCpy
        activeTagsList.add("DHBW Student")
        fileObj.put(uuid, newTagsObj)
        FileUtil.writeJSON("tags.json", fileObj, applicationContext)
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
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Tag entfernen")
        builder.setMessage("Willst du den Tag '$tagToRemove' entfernen?")
        builder.setPositiveButton("Ja") { dialog, which ->
            deleteTag(tagToRemove)
            setupTagsListView()
            Toast.makeText(applicationContext, "Tag '$tagToRemove' wurde entfernt", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("Nein") { dialog, which -> }
        builder.show()
        return true  // Return true to indicate that the long click event was handled
    }
}