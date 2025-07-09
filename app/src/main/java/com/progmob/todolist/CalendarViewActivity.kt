package com.progmob.todolist

import android.app.ProgressDialog.show
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
            val selectedDate = formatDateForDb(dayOfMonth, month, year)
            loadTasks(selectedDate)
        }
    }

    override fun onResume() {
        super.onResume()
        // Memuat data tugas berdasarkan tanggal yang dipilih
        val currentDate = getCurrentDate()
        loadTasks(currentDate)
    }

    private fun formatDateForDb(day: Int, month: Int, year: Int): String {
        // Format jadi "dd-MM-yyyy"
        return String.format("%02d-%02d-%04d", day, month + 1, year)
    }


    // Fungsi untuk menyiapkan RecyclerView dan adapter
    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            taskList = mutableListOf(),
            onCheckedChanged = { updatedTask ->
                databaseHelper.updateTaskCompletion(updatedTask.id, updatedTask.completed)
                loadTasks(getCurrentDate())
            },
            onItemClick = { task ->
                val dialog = TaskDetailDialog(task, isCompleted = task.completed)
                dialog.show(supportFragmentManager, "TaskDetailDialog")
            }
        )

        binding.taskRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.taskRecyclerView.adapter = adapter

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                AlertDialog.Builder(this@CalendarViewActivity).apply {
                    setTitle("Hapus Tugas")
                    setMessage("Apakah kamu yakin ingin menghapus tugas ini?")
                    setPositiveButton("Ya") { _, _ ->
                        val deleted = adapter.deleteTaskAndRemoveAt(position, databaseHelper)
                        if (deleted) {
                            Toast.makeText(this@CalendarViewActivity, "Tugas berhasil dihapus", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@CalendarViewActivity, "Gagal menghapus tugas", Toast.LENGTH_SHORT).show()
                            adapter.notifyItemChanged(position)
                        }
                    }
                    setNegativeButton("Batal") { _, _ ->
                        adapter.notifyItemChanged(position)
                    }
                    setCancelable(false)
                    show()
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val background = ContextCompat.getDrawable(this@CalendarViewActivity, R.drawable.rounded_red_background)

                background?.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                background?.draw(c)

                val icon = ContextCompat.getDrawable(this@CalendarViewActivity, R.drawable.ic_delete)
                val iconMargin = 32
                val iconTop = itemView.top + (itemView.height - icon!!.intrinsicHeight) / 2
                val iconLeft = itemView.right - iconMargin - icon.intrinsicWidth
                val iconRight = itemView.right - iconMargin
                val iconBottom = iconTop + icon.intrinsicHeight
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                icon.draw(c)

                val paint = Paint().apply {
                    color = ContextCompat.getColor(this@CalendarViewActivity, android.R.color.white)
                    textSize = 36f
                    isAntiAlias = true
                    textAlign = Paint.Align.RIGHT
                }

                val text = "Swipe to Delete"
                val textX = iconLeft - 16f
                val textY = itemView.top + (itemView.height / 2f) + (paint.textSize / 3f)

                c.drawText(text, textX, textY, paint)

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.taskRecyclerView)


        itemTouchHelper.attachToRecyclerView(binding.taskRecyclerView)
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
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        return formatDateForDb(day, month, year)
    }
}
