package com.progmob.todolist

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.progmob.todolist.databinding.ActivityCompletedTaskBinding

class CompletedTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompletedTaskBinding
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCompletedTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.backButton.setOnClickListener {
            val intent = Intent(this, LandingPageActivity::class.java)
            startActivity(intent)
            finish()
        }

        loadCompletedTasks()
    }

    private fun loadCompletedTasks() {
        val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        if (userId == -1) return

        val cursor = databaseHelper.getCompletedTasks(userId)
        val completedTasks = mutableListOf<TaskModel>()

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
                val category = cursor.getInt(cursor.getColumnIndexOrThrow("category_id"))
                val priority = cursor.getString(cursor.getColumnIndexOrThrow("priority"))
                val dueDate = cursor.getString(cursor.getColumnIndexOrThrow("due_date"))
                val dueTime = cursor.getString(cursor.getColumnIndexOrThrow("due_time"))
                val completed = true

                val categoryName = when (category) {
                    1 -> "Personal"
                    2 -> "Kuliah"
                    3 -> "Kerja"
                    else -> "Lainnya"
                }

                completedTasks.add(TaskModel(id, title, description, categoryName, priority, dueDate, dueTime, completed))
            } while (cursor.moveToNext())
        }
        cursor.close()

        val adapter = TaskAdapter(completedTasks.toMutableList()) { task ->
            // Handle jika task di-uncheck (kembali ke halaman utama)
            databaseHelper.updateTaskCompleted(task.id, false)
            loadCompletedTasks() // refresh
        }

        binding.completedRecyclerView.adapter = adapter
        binding.completedRecyclerView.layoutManager = LinearLayoutManager(this)
    }
}
