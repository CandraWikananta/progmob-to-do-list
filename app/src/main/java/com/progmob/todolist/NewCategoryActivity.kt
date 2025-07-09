package com.progmob.todolist

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.progmob.todolist.databinding.ActivityNewCategoryBinding

class NewCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewCategoryBinding
    private lateinit var db: DatabaseHelper
    private var userId: Int = -1  // untuk menyimpan ID user login

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)

        // Ambil userId dari intent
        userId = intent.getIntExtra("user_id", -1)
        if (userId == -1) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.ivBack.setOnClickListener { finish() }
        binding.tvBack.setOnClickListener { finish() }

        binding.btnAddCategory.setOnClickListener {
            val categoryName = binding.etCategory.text.toString().trim()

            if (categoryName.isEmpty()) {
                Toast.makeText(this, "Please enter a category name", Toast.LENGTH_SHORT).show()
            } else {
                val result = db.insertCategory(categoryName, userId)

                if (result != -1L) {
                    Toast.makeText(this, "Category added!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to add category", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
