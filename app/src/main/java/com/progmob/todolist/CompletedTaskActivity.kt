package com.progmob.todolist

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
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
        binding.deleteAllText.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setTitle("Hapus Semua Tugas Selesai")
                setMessage("Apakah Anda yakin ingin menghapus semua tugas yang telah selesai?")
                setPositiveButton("Ya") { _, _ ->
                    val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
                    val userId = sharedPref.getInt("user_id", -1)

                    if (userId != -1) {
                        val rowsDeleted = databaseHelper.deleteAllCompletedTasks(userId)
                        Toast.makeText(this@CompletedTaskActivity, "$rowsDeleted tugas berhasil dihapus", Toast.LENGTH_SHORT).show()
                        loadCompletedTasks()
                    }
                }
                setNegativeButton("Batal", null)
                show()
            }
        }

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
                val categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("category_id"))
                val priority = cursor.getString(cursor.getColumnIndexOrThrow("priority"))
                val dueDate = cursor.getString(cursor.getColumnIndexOrThrow("due_date"))
                val dueTime = cursor.getString(cursor.getColumnIndexOrThrow("due_time"))
                val completed = true

                val categoryName = databaseHelper.getCategoryNameById(categoryId)

                completedTasks.add(TaskModel(id, title, description, categoryName, priority, dueDate, dueTime, completed))
            } while (cursor.moveToNext())
        }
        cursor.close()


        val adapter = TaskAdapter(
            taskList = completedTasks.toMutableList(),
            onCheckedChanged = { task ->
                // Saat user uncheck task (dipindahkan ke landing page), reload ulang
                Handler(Looper.getMainLooper()).postDelayed({
                    loadCompletedTasks()
                }, 600)
            },
            onItemClick = { task ->
                val dialog = TaskDetailDialog(task, isCompleted = true)
                dialog.show(supportFragmentManager, "TaskDetailDialog")
            }
        )

        binding.completedRecyclerView.adapter = adapter
        binding.completedRecyclerView.layoutManager = LinearLayoutManager(this)
    }
}
