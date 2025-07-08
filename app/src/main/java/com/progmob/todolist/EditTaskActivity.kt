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

    private val categories = listOf("Personal", "Kuliah", "Kerja")
    private val priorities = listOf("Low", "Medium", "High")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)
        taskId = intent.getIntExtra("TASK_ID", -1)

        if (taskId == -1) {
            Toast.makeText(this, "Task ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Spinner: Category
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
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

            val categoryName = when (categoryId) {
                1 -> "Personal"
                2 -> "Kuliah"
                3 -> "Kerja"
                else -> "Lainnya"
            }

            binding.inputTitle.setText(title)
            binding.inputDescription.setText(description)
            binding.inputDueDate.setText(dueDate)
            binding.inputTime.setText(dueTime)

            val priorityIndex = priorities.indexOf(priority)
            if (priorityIndex >= 0) binding.spinnerPriority.setSelection(priorityIndex)

            val categoryIndex = categories.indexOf(categoryName)
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
        val category = binding.spinnerCategory.selectedItem.toString()

        val categoryId = when (category) {
            "Personal" -> 1
            "Kuliah" -> 2
            "Kerja" -> 3
            else -> 0
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
