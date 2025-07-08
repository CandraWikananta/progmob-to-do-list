package com.progmob.todolist

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SignUp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.sign_up_form_page)

        val signUpName: EditText = findViewById(R.id.signUpName)
        val signUpEmail: EditText = findViewById(R.id.signUpEmail)
        val signUpPassword: EditText = findViewById(R.id.signUpPassword)
        val continueButton: Button = findViewById(R.id.continueButton)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        continueButton.setOnClickListener {
            val name = signUpName.text.toString()
            val email = signUpEmail.text.toString()
            val password = signUpPassword.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                val sharedPref = getSharedPreferences("UserInformation", MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putString("Name", name)
                editor.putString("Email", email)
                editor.putString("Password", password)
                editor.apply()

                val intent = Intent(this, LandingPage::class.java)
                startActivity(intent)
                finish()
            } else {
                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}