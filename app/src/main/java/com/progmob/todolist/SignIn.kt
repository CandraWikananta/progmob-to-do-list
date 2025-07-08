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
import java.util.jar.Attributes.Name

class SignIn : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.sign_in_form_page)

        val signInName: EditText = findViewById(R.id.signInName)
        val signInPassword: EditText = findViewById(R.id.signInPassword)
        val coninueButton: Button = findViewById(R.id.signInButton)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signInForm)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        coninueButton.setOnClickListener {
            val enteredName = signInName.text.toString()
            val enteredPassword = signInPassword.text.toString()
            
            if (enteredName.isNotEmpty() && enteredPassword.isNotEmpty()) {
                val sharedPref = getSharedPreferences("UserInformation", MODE_PRIVATE)
                val savedName = sharedPref.getString("Name", "")
                val savedPassword = sharedPref.getString("Password", "")
                
                
                if (enteredName == savedName && enteredPassword == savedPassword) {
                    val intent = Intent(this, LandingPage::class.java)
                    startActivity(intent)
                    finish()
                } else  {
                    Toast.makeText(this, "Incorrect Name  or Password", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please Fill the Fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}