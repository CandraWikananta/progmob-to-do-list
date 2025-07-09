package com.progmob.todolist

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.progmob.todolist.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set selected item ke Profile agar terlihat aktif
        binding.bottomNav.selectedItemId = R.id.nav_profile

        // Navigation logic
        binding.bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_tasks -> {
                    startActivity(Intent(this, LandingPageActivity::class.java))
                    finish()
                    true
                }
//              R.id.nav_calendar -> {
//                  startActivity(Intent(this, CalendarActivity::class.java))
//                  finish()
//                  true
//              }
                R.id.nav_profile -> {
                    // Sudah di halaman Profile, tidak perlu pindah
                    true
                }
                else -> false
            }
        }

        // Logout logic
        binding.ivLogout.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Konfirmasi Logout")
                .setMessage("Apakah Anda yakin ingin logout?")
                .setPositiveButton("Ya") { _, _ ->
                    val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
                    sharedPref.edit().clear().apply()

                    val intent = Intent(this, CreateAccountActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        // mengambil nama user dan menampilkan nya di profile page
        val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        if (userId != -1) {
            val db = DatabaseHelper(this)
            val username = db.getUsernameById(userId)
            binding.tvName.text = username
        }


    }
}
