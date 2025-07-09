package com.progmob.todolist

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.progmob.todolist.databinding.ActivityLandingPageBinding

class LandingPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLandingPageBinding
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLandingPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)

        loadTasks()

        binding.addTask.setOnClickListener {
            val intent = Intent(this, CreateTaskActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.viewCompletedText.setOnClickListener {
            val intent = Intent(this, CompletedTaskActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        loadTasks()
    }

    private fun loadTasks() {
        val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        if (userId == -1) {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        val cursor = databaseHelper.getIncompleteTasks(userId)
        val taskList = mutableListOf<TaskModel>()

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
                val category = cursor.getInt(cursor.getColumnIndexOrThrow("category_id"))
                val priority = cursor.getString(cursor.getColumnIndexOrThrow("priority"))
                val dueDate = cursor.getString(cursor.getColumnIndexOrThrow("due_date"))
                val dueTime = cursor.getString(cursor.getColumnIndexOrThrow("due_time"))
                val completed = cursor.getInt(cursor.getColumnIndexOrThrow("completed")) == 1

                val categoryName = when (category) {
                    1 -> "Personal"
                    2 -> "Kuliah"
                    3 -> "Kerja"
                    else -> "Lainnya"
                }

                taskList.add(TaskModel(id, title, description, categoryName, priority, dueDate, dueTime, completed))
            } while (cursor.moveToNext())
        }
        cursor.close()

        if (taskList.isNotEmpty()) {
            binding.taskRecyclerView.visibility = android.view.View.VISIBLE
            binding.emptyIllustration.visibility = android.view.View.GONE
            binding.emptyText.visibility = android.view.View.GONE
        } else {
            binding.taskRecyclerView.visibility = android.view.View.GONE
            binding.emptyIllustration.visibility = android.view.View.VISIBLE
            binding.emptyText.visibility = android.view.View.VISIBLE
        }

        val adapter = TaskAdapter(taskList.toMutableList()) { updatedTask ->
            if (updatedTask.completed) {
                // Delay agar animasi centang selesai dulu sebelum task dihapus dari tampilan
                android.os.Handler().postDelayed({
                    loadTasks()
                }, 600) // delay 300ms, bisa kamu ubah sesuai durasi animasi
            }
        }

        binding.taskRecyclerView.adapter = adapter
        binding.taskRecyclerView.layoutManager = LinearLayoutManager(this)
    }
}
