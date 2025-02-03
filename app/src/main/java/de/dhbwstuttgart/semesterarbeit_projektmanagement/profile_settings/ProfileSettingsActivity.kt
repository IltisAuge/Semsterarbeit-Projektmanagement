package de.dhbwstuttgart.semesterarbeit_projektmanagement.profile_settings

import android.content.Intent
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.fragment.app.DialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.dhbwstuttgart.semesterarbeit_projektmanagement.FileUtil
import de.dhbwstuttgart.semesterarbeit_projektmanagement.R
import de.dhbwstuttgart.semesterarbeit_projektmanagement.databinding.ActivityProfileSettingsBinding
import de.dhbwstuttgart.semesterarbeit_projektmanagement.login_register.LoginUtil
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.net.URLConnection

class ProfileSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileSettingsBinding
    private lateinit var tagsListView: ListView
    private lateinit var addTagBtn: FloatingActionButton
    private lateinit var activeTagsList: ArrayList<String>
    private lateinit var tagListAdapter: ArrayAdapter<String>
    private lateinit var chooseImgBtn: Button
    private lateinit var profileImg: ImageView

    private var hobbys: ArrayList<String> = arrayListOf(
        "Acroyoga", "Apnoetauchen", "Badminton", "Baseball", "Basketball", "Bauchtanz", "Bergsteigen", "BMX", "Bodybuilding", "Boxen", "Cheerleading", "Darts",
        "Eishockey", "Eiskunstlaufen", "Skateboard", "Einrad", "Fahrrad", "Fallschirmspringen", "Fechten", "Fitness", "Football", "Fußball", "Golf", "Hobby Horsing", "Hula Hoop", "Inline Skates", "Joggen", "Kite Surfen",
        "Longboard", "Paintball", "Parkour", "Motorrad", "Pilates", "Reiten", "Rudern", "Gaming", "Reisen", "Kochen", "Backen", "Ski", "Snowboard", "Segeln", "Schwimmen", "Tanzen", "Tennis", "Tauchen",
        "Triathlon", "Leichtathletik"
    )

    private var companies: ArrayList<String> = arrayListOf("Bosch", "Allianz", "Telekom", "Porsche", "Daimler")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chooseImgBtn = binding.chooseProfilePictureBtn
        profileImg = binding.profilePicture
        setupProfilePictureBtn()
        loadProfilePicture()

        tagsListView = findViewById(R.id.tags_list)
        addTagBtn = findViewById(R.id.TagAdd)
        activeTagsList = loadSavedTags()

        activeTagsList.add("DHBW Student")

        tagListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, activeTagsList)
        tagsListView.adapter = tagListAdapter

        addTagBtn.setOnClickListener {
            openAvailableTagsDialog()
        }

        // Long click listener to delete items
        tagsListView.setOnItemLongClickListener { parent, view, position, id ->
            val tagToRemove = activeTagsList[position]
            // Prevent the deletion of the "DHBW Student" tag
            if (tagToRemove == "DHBW Student") {
                Toast.makeText(applicationContext, "Du kannst diesen Tag nicht löschen!", Toast.LENGTH_SHORT).show()
                return@setOnItemLongClickListener true
            }
            openDeleteConfirmationDialog(tagToRemove, position)
        }
    }

    fun setupProfilePictureBtn() {
        val resultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri == null) return@registerForActivityResult

            val file = File(applicationContext.filesDir, "profile-picture")
            file.createNewFile()
            uri.let { applicationContext.contentResolver.openInputStream(it) }.use { input ->
                file.outputStream().use { output ->
                    input?.copyTo(output)
                }
            }
            profileImg.setImageURI(uri)
        }
        chooseImgBtn.setOnClickListener {
            resultLauncher.launch("image/*")
        }
    }

    fun loadProfilePicture() {// Load existing profile picture if available
        val imgFile = File(applicationContext.filesDir, "profile-picture")
        if (imgFile.exists()) {
            val uri = Uri.fromFile(imgFile)
            uri.let { applicationContext.contentResolver.openInputStream(it) }.use {
                val bufferedIs = BufferedInputStream(it)
                val bitmap = BitmapFactory.decodeStream(bufferedIs)
                profileImg.setImageBitmap(bitmap)
                bufferedIs.close()
                it?.close()
            }
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
                tagListAdapter.notifyDataSetChanged()
                if (companies.contains(tag)) {
                    Toast.makeText(applicationContext, "Unternehmens-Tag '$tag' wurde hinzugefügt", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, "Tag '$tag' wurde hinzugefügt", Toast.LENGTH_SHORT).show()
                }
                availableTagsDialog.dismiss()
                saveTag(tag)
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
        val uuid = LoginUtil.getLocalUserUUID(applicationContext)
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
        val uuid = LoginUtil.getLocalUserUUID(applicationContext)
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

    fun deleteSavedTag(tag: String) {
        val tagsFile = getTagsFile()
        val uuid = LoginUtil.getLocalUserUUID(applicationContext)
        if (uuid == null) {
            return
        }
        val fileObj = JSONObject(tagsFile.readText())
        if (!fileObj.has(uuid)) {
            return
        }
        val activeTags = fileObj.getJSONArray(uuid)
        for (i in 0 until activeTags.length() - 1) {
            if (activeTags.get(i) == tag) {
                activeTags.remove(i)
            }
        }
        fileObj.put(uuid, activeTags)
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

    fun openDeleteConfirmationDialog(tagToRemove: String, position: Int) : Boolean {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Tag entfernen")
        builder.setMessage("Willst du den Tag '$tagToRemove' entfernen?")
        builder.setPositiveButton("Ja") { dialog, which ->
            activeTagsList.removeAt(position)
            tagListAdapter.notifyDataSetChanged()  // Update the ListView
            deleteSavedTag(tagToRemove)
            Toast.makeText(applicationContext, "Tag '$tagToRemove' wurde entfernt", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("Nein") { dialog, which -> }
        builder.show()
        return true  // Return true to indicate that the long click event was handled
    }
}