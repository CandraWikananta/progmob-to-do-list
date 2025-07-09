package com.progmob.todolist

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.progmob.todolist.databinding.ActivityCalendarViewBinding
import java.text.SimpleDateFormat
import java.util.*

class CalendarViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarViewBinding  // ViewBinding untuk activity_calendar_view.xml
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Menghubungkan layout dengan binding
        binding = ActivityCalendarViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)

        // Setup RecyclerView dan Adapter
        setupRecyclerView()

        // Mengatur padding untuk edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Menangani navigasi di BottomNavigationView
        binding.bottomNav.selectedItemId = R.id.nav_calendar
        binding.bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_tasks -> {
                    startActivity(Intent(this, LandingPageActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_calendar -> true
                else -> false
            }
        }

        // Tangkap pemilihan tanggal pada CalendarView
        binding.calendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = "$dayOfMonth/${month + 1}/$year"
            loadTasks(selectedDate)  // Memuat task berdasarkan tanggal yang dipilih
        }
    }

    override fun onResume() {
        super.onResume()
        // Memuat data tugas berdasarkan tanggal yang dipilih
        val currentDate = getCurrentDate()
        loadTasks(currentDate)
    }

    // Fungsi untuk menyiapkan RecyclerView dan adapter
    private fun setupRecyclerView() {
        adapter = TaskAdapter(mutableListOf(), { updatedTask ->
            // Handle onCheckedChanged
            loadTasks(getCurrentDate())
        }, { task ->
            // Handle item click, for example to show details
            // Example: startActivity(Intent(this, TaskDetailActivity::class.java))
        })
        binding.taskRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.taskRecyclerView.adapter = adapter
    }

    // Fungsi untuk memuat data tugas dari database dan menampilkan di RecyclerView berdasarkan tanggal
    private fun loadTasks(selectedDate: String) {
        val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        if (userId == -1) {
            startActivity(Intent(this, LandingPageActivity::class.java))
            finish()
            return
        }

        // Ambil tugas berdasarkan tanggal yang dipilih
        val cursor = databaseHelper.getTasksByDate(userId, selectedDate)
        val taskList = mutableListOf<TaskModel>()

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val description = cursor.getString(cursor.getColumnIndexOrThrow("description"))
                val categoryId = cursor.getInt(cursor.getColumnIndexOrThrow("category_id"))
                val priority = cursor.getString(cursor.getColumnIndexOrThrow("priority"))
                val dueDate = cursor.getString(cursor.getColumnIndexOrThrow("due_date"))
                val dueTime = cursor.getString(cursor.getColumnIndexOrThrow("due_time"))
                val completed = cursor.getInt(cursor.getColumnIndexOrThrow("completed")) == 1

                val categoryName = databaseHelper.getCategoryNameById(categoryId)

                taskList.add(TaskModel(id, title, description, categoryName, priority, dueDate, dueTime, completed))
            } while (cursor.moveToNext())
        }
        cursor.close()

        // Menampilkan RecyclerView atau ilustrasi jika tidak ada tugas
        if (taskList.isNotEmpty()) {
            binding.taskRecyclerView.visibility = View.VISIBLE
            binding.emptyText.visibility = View.GONE
        } else {
            binding.taskRecyclerView.visibility = View.GONE
            binding.emptyText.visibility = View.VISIBLE
        }

        // Memperbarui adapter dengan data baru
        adapter.taskList.clear()
        adapter.taskList.addAll(taskList)
        adapter.notifyDataSetChanged()
    }

    // Mendapatkan tanggal saat ini dalam format yang sesuai
    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1 // Bulan mulai dari 0, jadi tambahkan 1
        val year = calendar.get(Calendar.YEAR)
        return "$day/$month/$year"
    }
}
