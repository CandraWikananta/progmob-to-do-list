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
        val continueButton: Button = findViewById(R.id.signUpButton)

    }
}