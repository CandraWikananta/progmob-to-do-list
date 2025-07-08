package com.progmob.todolist

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.progmob.todolist.databinding.ActivitySignInBinding


class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)

        binding.signInButton.setOnClickListener{
            val loginUsername = binding.signInName.text.toString()
            val loginPassword = binding.signInPassword.text.toString()
            loginDatabase(loginUsername, loginPassword)
        }
        binding.goTosignUp.setOnClickListener{
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loginDatabase(username:String, password:String){
        val userId = databaseHelper.getUserId(username, password) // Ambil user_id
        if (userId != -1) {
            // Simpan user_id ke SharedPreferences
            val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putInt("user_id", userId)
            editor.apply()

            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LandingPageActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Login Failed", Toast.LENGTH_SHORT).show()
        }
    }
}