package de.dhbwstuttgart.semesterarbeit_projektmanagement.login_register

import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.dhbwstuttgart.semesterarbeit_projektmanagement.databinding.ActivityRegisterBinding
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        with(window) {
            requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
            val s = android.transition.Fade()
            s.duration = 200
            exitTransition = s
        }
        setContentView(binding.root)

        val nextButton = binding.nextButton
        nextButton.setOnClickListener {
            if (binding.nameInput.text.isEmpty()
                || binding.emailInput.text.isEmpty()
                || binding.passwordInput.text.isEmpty()) {
                Toast.makeText(applicationContext, "Bitte fülle die Felder oben aus!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val email = binding.emailInput.text.toString()
            if (!email.let { it1 -> android.util.Patterns.EMAIL_ADDRESS.matcher(it1).matches() }) {
                Toast.makeText(applicationContext, "Ungültige Email Adresse!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (LoginUtil.getUserbyEmail(applicationContext, email) != null) {
                Toast.makeText(applicationContext, "Es existiert bereits ein Konto mit dieser Email-Adresse!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(applicationContext, RegisterNextActivity::class.java)
            intent.putExtra("name", binding.nameInput.text.toString())
            intent.putExtra("email", binding.emailInput.text.toString())
            intent.putExtra("password", LoginUtil.hash(binding.passwordInput.text.toString()))
            startActivity(intent)
            finish()
        }

        val loginButton = binding.loginLinkButton
        loginButton.setOnClickListener {
            startActivity(Intent(applicationContext, LoginActivity::class.java))
            finish()
        }
    }
}