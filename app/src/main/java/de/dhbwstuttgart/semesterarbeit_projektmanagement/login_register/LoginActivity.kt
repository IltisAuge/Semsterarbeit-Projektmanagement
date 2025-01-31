package de.dhbwstuttgart.semesterarbeit_projektmanagement.login_register

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import androidx.activity.ComponentActivity
import de.dhbwstuttgart.semesterarbeit_projektmanagement.databinding.ActivityLoginBinding

class LoginActivity : ComponentActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        with(window) {
            requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
            val s = android.transition.Fade()
            s.duration = 200
            exitTransition = s
        }
        setContentView(binding.root)

        val emailInput = binding.emailInput
        val passwordInput = binding.passwordInput
        val loginButton = binding.loginButton
        val registerButton = binding.registerLinkButton
        registerButton.setOnClickListener {
            startActivity(Intent(applicationContext, RegisterActivity::class.java))
            finish()
        }
        loginButton.setOnClickListener {
            if (emailInput.text.isEmpty()
                || passwordInput.text.isEmpty()) {
                Toast.makeText(applicationContext, "Bitte fülle die Felder oben aus!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val user = LoginUtil.getUserbyEmail(applicationContext, emailInput.text.toString())
            if (user == null ||
                user.getString("password") != LoginUtil.hash(passwordInput.text.toString())) {
                Toast.makeText(applicationContext, "Überprüfe deine Anmeldedaten!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(
                LoginUtil.loginAndGetIntent(applicationContext, user),
                ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
            Toast.makeText(applicationContext, "Du wurdest angemeldet", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}