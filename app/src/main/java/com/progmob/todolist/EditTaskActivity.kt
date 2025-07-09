package com.progmob.todolist

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.progmob.todolist.databinding.ActivityEditTaskBinding
import java.util.*

class EditTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditTaskBinding
    private lateinit var databaseHelper: DatabaseHelper

    private var taskId: Int = -1
    private var userId: Int = -1
    private var categoryList: List<Pair<Int, String>> = emptyList()  // (id, name)
    private val priorities = listOf("Low", "Medium", "High")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)
        taskId = intent.getIntExtra("TASK_ID", -1)

        // Ambil userId dari SharedPreferences
        val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
        userId = sharedPref.getInt("user_id", -1)

        if (taskId == -1 || userId == -1) {
            Toast.makeText(this, "Task ID atau User ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Ambil kategori user dari database
        categoryList = databaseHelper.getCategoriesByUser(userId)
        val categoryNames = categoryList.map { it.second }

        // Spinner: Category
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryNames)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = categoryAdapter

        // Spinner: Priority
        val priorityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPriority.adapter = priorityAdapter

        // Load data dari DB
        loadTaskData(taskId)

        // Date & Time Picker
        binding.inputDueDate.setOnClickListener { showDatePicker() }
        binding.inputTime.setOnClickListener { showTimePicker() }

        // Button: Update
        binding.btnUpdateTask.setOnClickListener { updateTask() }

        // Button: Back
        binding.backButton.setOnClickListener { finish() }
    }

    private fun loadTaskData(id: Int) {
        val cursor = databaseHelper.getTaskById(id)
        if (cursor.moveToFirst()) {
            val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
            val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
            val priority = cursor.getString(cursor.getColumnIndexOrThrow("priority"))
            val dueDate = cursor.getString(cursor.getColumnIndexOrThrow("due_date"))
            val dueTime = cursor.getString(cursor.getColumnIndexOrThrow("due_time"))
            val categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("category_id"))

            binding.inputTitle.setText(title)
            binding.inputDescription.setText(description)
            binding.inputDueDate.setText(dueDate)
            binding.inputTime.setText(dueTime)

            // Set selection spinner priority
            val priorityIndex = priorities.indexOf(priority)
            if (priorityIndex >= 0) binding.spinnerPriority.setSelection(priorityIndex)

            // Set selection spinner category berdasarkan ID
            val categoryIndex = categoryList.indexOfFirst { it.first == categoryId }
            if (categoryIndex >= 0) binding.spinnerCategory.setSelection(categoryIndex)
        }
        cursor.close()
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val dateString = String.format("%02d-%02d-%04d", selectedDay, selectedMonth + 1, selectedYear)
            binding.inputDueDate.setText(dateString)
        }, year, month, day)

        datePicker.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePicker = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val timeString = String.format("%02d:%02d", selectedHour, selectedMinute)
            binding.inputTime.setText(timeString)
        }, hour, minute, true)

        timePicker.show()
    }

    private fun updateTask() {
        val title = binding.inputTitle.text.toString()
        val description = binding.inputDescription.text.toString()
        val priority = binding.spinnerPriority.selectedItem.toString()
        val dueDate = binding.inputDueDate.text.toString()
        val dueTime = binding.inputTime.text.toString()

        // Ambil ID kategori dari posisi Spinner
        val categoryIndex = binding.spinnerCategory.selectedItemPosition
        val categoryId = if (categoryIndex in categoryList.indices) {
            categoryList[categoryIndex].first
        } else {
            0
        }

        val result = databaseHelper.updateTask(
            taskId,
            title,
            description,
            priority,
            dueDate,
            dueTime,
            categoryId
        )

        if (result > 0) {
            Toast.makeText(this, "Task berhasil diperbarui", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LandingPageActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Gagal memperbarui task", Toast.LENGTH_SHORT).show()
        }
    }
}
