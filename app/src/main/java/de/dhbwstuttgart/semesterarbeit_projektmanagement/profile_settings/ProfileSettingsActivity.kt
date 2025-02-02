package de.dhbwstuttgart.semesterarbeit_projektmanagement.profile_settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.dhbwstuttgart.semesterarbeit_projektmanagement.R
import de.dhbwstuttgart.semesterarbeit_projektmanagement.databinding.ActivityProfileSettingsBinding
import java.io.File

class ProfileSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileSettingsBinding
    private lateinit var tagsListView: ListView
    private lateinit var addTagBtn: FloatingActionButton
    private lateinit var activeTagsList: ArrayList<String>
    private lateinit var tagsAdapter: ArrayAdapter<String>
    private lateinit var chooseImgBtn: Button

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
        val profileImg = binding.profilePicture
        val resultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri == null) return@registerForActivityResult

            val file = File(applicationContext.filesDir, "profile-picture")
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

        tagsListView = findViewById(R.id.tags_list)
        addTagBtn = findViewById(R.id.TagAdd)
        activeTagsList = ArrayList()

        activeTagsList.add("DHBW Student")

        tagsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, activeTagsList)
        tagsListView.adapter = tagsAdapter
        addTagBtn.setOnClickListener {
            var builder = AlertDialog.Builder(this)
            builder.setTitle("Suche")

            // Ersetze EditText durch AutoCompleteTextView
            val input = AutoCompleteTextView(this)
            input.hint = "Tag hinzufügen"
            input.inputType = InputType.TYPE_CLASS_TEXT

            // Adapter für AutoCompleteTextView erstellen
            val allTags = hobbys + companies  // Kombiniere Hobbys und Fächer
            val autoCompleteAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, allTags)
            input.setAdapter(autoCompleteAdapter)

            // Setze den Threshold auf 1, damit Vorschläge beim ersten Buchstaben angezeigt werden
            input.threshold = 1

            builder.setView(input)

            // Add the Show Tags button
            builder.setNeutralButton("Verfügbare Tags anzeigen") { dialog, which ->
                // Zeige alle Tags (Hobbys und Subjects) in einem neuen Dialog
                val allTagsDialog = AlertDialog.Builder(this)
                allTagsDialog.setTitle("Verfügbare Tags")

                // Alle Tags zusammenführen und alphabetisch sortieren
                val combinedTags = (hobbys + companies).sorted()  // Tags sortieren

                val tagsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, combinedTags)
                val tagsListView = ListView(this)
                tagsListView.adapter = tagsAdapter
                allTagsDialog.setView(tagsListView)

                allTagsDialog.setPositiveButton("Ok") { dialog, which -> }
                allTagsDialog.show()
            }

            builder.setPositiveButton("Ok") { dialog, which ->
                val tag = input.text.toString()

                // Überprüfen, ob der Tag in den Hobbys oder den Fächern ist
                if (hobbys.contains(tag)) {
                    if (!activeTagsList.contains(tag)) {
                        activeTagsList.add(tag)
                        tagsAdapter.notifyDataSetChanged()
                        Toast.makeText(applicationContext, "Tag '$tag' wurde hinzugefügt", Toast.LENGTH_SHORT).show()
                        // Save tag in file
                    } else {
                        Toast.makeText(applicationContext, "Du hast diesen Tag bereits!", Toast.LENGTH_SHORT).show()
                    }
                } else if (companies.contains(tag)) {
                    // Überprüfen, ob bereits ein Fach vorhanden ist
                    if (activeTagsList.any { companies.contains(it) }) {
                        Toast.makeText(applicationContext, "Du kannst nur in einem Unternehmen sein!", Toast.LENGTH_SHORT).show()
                    } else {
                        if (!activeTagsList.contains(tag)) {
                            activeTagsList.add(tag)
                            tagsAdapter.notifyDataSetChanged()
                            // Save tag in file
                            Toast.makeText(applicationContext, "Unternehmens-Tag wurde hinzugefügt", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(applicationContext, "Du bist bereits in diesem Unternehmen!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(applicationContext, "Diesen Tag gibt es nicht!", Toast.LENGTH_SHORT).show()
                }
            }

            builder.setNegativeButton("Abbrechen") { dialog, which ->
            }

            builder.show()
        }

        // Long click listener to delete items
        tagsListView.setOnItemLongClickListener { parent, view, position, id ->

            val tagToRemove = activeTagsList[position]

            // Prevent the deletion of the "DHBW Student" tag
            if (tagToRemove == "DHBW Student") {
                Toast.makeText(applicationContext, "Du kannst diesen Tag nicht löschen!", Toast.LENGTH_SHORT).show()
                return@setOnItemLongClickListener true
            }

            activeTagsList.removeAt(position)
            tagsAdapter.notifyDataSetChanged()  // Update the ListView

            Toast.makeText(applicationContext, "Tag '$tagToRemove' wurde entfernt", Toast.LENGTH_SHORT).show()
            true  // Return true to indicate that the long click event was handled
        }
    }
}