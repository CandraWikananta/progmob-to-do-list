package com.progmob.todolist

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.progmob.todolist.databinding.ActivityDetailTaskBinding

class DetailTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailTaskBinding
    private lateinit var databaseHelper: DatabaseHelper
    private var taskId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)

        // Ambil taskId dari intent
        taskId = intent.getIntExtra("TASK_ID", -1)
        if (taskId == -1) {
            Toast.makeText(this, "Task tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadTaskDetails()

        binding.btnEditTask.setOnClickListener {
            val intent = Intent(this, EditTaskActivity::class.java)
            intent.putExtra("TASK_ID", taskId)
            startActivity(intent)
        }

        binding.btnDeleteTask.setOnClickListener {
            showDeleteConfirmation()
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadTaskDetails() {
        val cursor = databaseHelper.getTaskById(taskId)

        if (cursor != null && cursor.moveToFirst()) {
            val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
            val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
            val priority = cursor.getString(cursor.getColumnIndexOrThrow("priority"))
            val dueDate = cursor.getString(cursor.getColumnIndexOrThrow("due_date"))
            val dueTime = cursor.getString(cursor.getColumnIndexOrThrow("due_time"))
            val categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("category_id"))

            // Ambil nama kategori dari DB
            val categoryName = databaseHelper.getCategoryNameById(categoryId)

            // Tampilkan data ke UI
            binding.taskTitle.text = title
            binding.taskDescription.text = description
            binding.taskPriority.text = priority
            binding.taskCategory.text = categoryName
            binding.taskDueDate.text = dueDate
            binding.taskDueTime.text = dueTime
        } else {
            Toast.makeText(this, "Data task tidak ditemukan", Toast.LENGTH_SHORT).show()
        }

        cursor?.close()
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Hapus Task")
            .setMessage("Apakah kamu yakin ingin menghapus task ini?")
            .setPositiveButton("Ya") { _: DialogInterface, _: Int ->
                val result = databaseHelper.deleteTask(taskId)
                if (result > 0) {
                    Toast.makeText(this, "Task berhasil dihapus", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Gagal menghapus task", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
