package com.progmob.todolist

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.progmob.todolist.databinding.ActivityCreateTaskBinding
import java.util.Calendar

class CreateTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateTaskBinding
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCreateTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)

        // Tombol back
        binding.backButton.setOnClickListener {
            startActivity(Intent(this, LandingPageActivity::class.java))
            finish()
        }

        // Spinner data
        val priorities = arrayOf("Low", "Medium", "High")

        // Ambil user ID
        val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(this, "User belum login!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }

        // Ambil kategori dari database
        val categoryList = databaseHelper.getCategoriesByUser(userId)
        val categoryNames = categoryList.map { it.second }  // Ambil nama saja untuk spinner

        // Set adapter category
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = categoryAdapter

        // Set adapter priority
        val priorityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPriority.adapter = priorityAdapter

        val calendar = Calendar.getInstance()
        // Date Picker
        binding.inputDueDate.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                R.style.RedDatePickerDialogTheme, // ← gunakan custom style merah
                { _, y, m, d ->
                    val formatted = String.format("%02d-%02d-%04d", d, m + 1, y)
                    binding.inputDueDate.setText(formatted)
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }

        // Time Picker
        binding.inputTime.setOnClickListener {
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(
                this,
                R.style.RedDatePickerDialogTheme, // ← gunakan custom style merah juga
                { _, h, m ->
                    val formatted = String.format("%02d:%02d", h, m)
                    binding.inputTime.setText(formatted)
                },
                hour,
                minute,
                true
            )
            timePickerDialog.show()
        }


        // Tombol Add Task
        binding.btnAddTask.setOnClickListener {
            val title = binding.inputTitle.text.toString()
            val description = binding.inputDescription.text.toString()
            val category = binding.spinnerCategory.selectedItem.toString()
            val priority = binding.spinnerPriority.selectedItem.toString()
            val dueDate = binding.inputDueDate.text.toString()
            val dueTime = binding.inputTime.text.toString()

            // Ambil user ID dari SharedPreferences
            val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPref.getInt("user_id", -1)

            if (userId == -1) {
                Toast.makeText(this, "User belum login!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, SignInActivity::class.java))
                finish()
                return@setOnClickListener
            }

            // Ambil kembali ID dari category yang dipilih berdasarkan urutan spinner
            val selectedIndex = binding.spinnerCategory.selectedItemPosition
            val categoryId = if (selectedIndex in categoryList.indices) {
                categoryList[selectedIndex].first
            } else {
                0
            }


            val result = databaseHelper.insertTask(
                userId, categoryId, title, description, priority, dueDate, dueTime
            )

            if (result != -1L) {
                Toast.makeText(this, "Task berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LandingPageActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Gagal menambahkan task", Toast.LENGTH_SHORT).show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
