package com.progmob.todolist

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.progmob.todolist.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var editProfileLauncher: ActivityResultLauncher<Intent>

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
                R.id.nav_calendar -> {
                    Toast.makeText(this, "Calendar belum tersedia", Toast.LENGTH_SHORT).show()
                    true
                }
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

        editProfileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val updatedName = result.data?.getStringExtra("UPDATED_NAME")
                if (!updatedName.isNullOrEmpty()) {
                    findViewById<TextView>(R.id.tvName).text = updatedName
                }
            }
        }

        // pergi ke halaman edit profile
        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfilActivity::class.java)
            editProfileLauncher.launch(intent)
        }

        // mengambil nama user dan menampilkan nya di profile page
        val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        if (userId != -1) {
            val db = DatabaseHelper(this)
            val username = db.getUsernameById(userId)
            binding.tvName.text = username
        }

        if (userId != -1) {
            val db = DatabaseHelper(this)
            val completedCount = db.getCompletedTaskCount(userId)
            val remainingCount = db.getRemainingTaskCount(userId)

            binding.tvCompletedCount.text = completedCount.toString()
            binding.tvTaskLeft.text = remainingCount.toString()
        }

        // Delete account logic
        binding.btnDeleteAccount.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Hapus Akun")
                .setMessage("Akun Anda akan dihapus secara permanen. Lanjutkan?")
                .setPositiveButton("Hapus") { _, _ ->
                    val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
                    val userId = sharedPref.getInt("user_id", -1)

                    if (userId != -1) {
                        val db = DatabaseHelper(this)
                        db.deleteAllTasksByUserId(userId)
                        db.deleteAllCategoriesByUserId(userId)
                        db.deleteUser(userId)

                        sharedPref.edit().clear().apply()

                        Toast.makeText(this, "Akun berhasil dihapus", Toast.LENGTH_SHORT).show()

                        // Redirect ke halaman CreateAccountActivity
                        val intent = Intent(this, CreateAccountActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Gagal menghapus akun", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Batal", null)
                .show()
        }

    }
}
