package de.dhbwstuttgart.semesterarbeit_projektmanagement.login_register

import android.R
import android.app.ActivityOptions
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.dhbwstuttgart.semesterarbeit_projektmanagement.FileUtil
import de.dhbwstuttgart.semesterarbeit_projektmanagement.databinding.ActivityRegisterNextBinding
import org.json.JSONObject
import java.util.UUID

class RegisterNextActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterNextBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterNextBinding.inflate(layoutInflater)
        with(window) {
            requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
            val s = android.transition.Fade()
            s.duration = 200
            exitTransition = s
        }
        setContentView(binding.root)

        val json = resources.assets.open("fakultaet-mapping.json").bufferedReader().use { it.readText() }
        val jsonObj = JSONObject(json)

        val fakultaet_list = ArrayList<String>()
        for (key in jsonObj.keys()) {
            fakultaet_list.add(key)
        }
        val mapping = HashMap<String, ArrayList<String>>()
        for (fakultaet in fakultaet_list) {
            val studiengaenge = jsonObj.getJSONArray(fakultaet)
            val list = ArrayList<String>()
            for (i in 0 until studiengaenge.length()) {
                list.add(studiengaenge.getString(i))
            }
            mapping.put(fakultaet, list)
        }
        println(mapping)

        val jahrgang_list = listOf("2020", "2021", "2022", "2023", "2024")

        val fakultaet_selection = binding.fakultaetSelection
        val fakultaet_adapter = ArrayAdapter(this, R.layout.simple_list_item_1, fakultaet_list)
        fakultaet_selection.adapter = fakultaet_adapter
        fakultaet_selection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val studiengang_adapter = ArrayAdapter(applicationContext, R.layout.simple_list_item_1, mapping.getValue(fakultaet_list[position]))
                binding.studiengangSelection.adapter = studiengang_adapter
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        val studiengang_selection = binding.studiengangSelection
        val studiengang_adapter = ArrayAdapter(this, R.layout.simple_list_item_1, mapping.getValue(fakultaet_selection.selectedItem.toString()))
        studiengang_selection.adapter = studiengang_adapter

        val jahrgang_selection = binding.jahrgangSelection
        val jahrgang_adapter = ArrayAdapter(this, R.layout.simple_list_item_1, jahrgang_list)
        jahrgang_selection.adapter = jahrgang_adapter

        val register_button = binding.registerButton
        register_button.setOnClickListener {
            val name = intent.getStringExtra("name")
            val email = intent.getStringExtra("email")
            val password = intent.getStringExtra("password")
            if (name == null || email == null || password == null) {
                println("Error while creating account: name or email or password is null")
                return@setOnClickListener
            }
            val fakultaet = fakultaet_selection.selectedItem.toString()
            val studiengang = studiengang_selection.selectedItem.toString()
            val jahrgang = jahrgang_selection.selectedItem.toString()
            val user = LoginUtil.getUserObj(UUID.randomUUID(), name, email, password, fakultaet, studiengang, jahrgang)
            createAccount(user)
            startActivity(
                LoginUtil.loginAndGetIntent(applicationContext, user),
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
            Toast.makeText(applicationContext, "Dein Konto wurde erstellte", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    companion object {
    }

    fun createAccount(user: JSONObject) {
        println("Creating account $user")
        val jsonObj = JSONObject()
        jsonObj.put("uuid", UUID.randomUUID().toString())
        FileUtil.writeJSON("local-user.json", jsonObj, applicationContext)

        // Add user to users.json
        val users = FileUtil.readJSON("users.json", applicationContext)
        users.put(user.getString("uuid"), user)
        FileUtil.writeJSON("users.json", users, applicationContext)
    }
}