package com.progmob.todolist

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.CalendarView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.progmob.todolist.databinding.ActivityLandingPageBinding

class LandingPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLandingPageBinding
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLandingPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)

        setupRecyclerView()
        loadTasks()

        binding.addTask.setOnClickListener {
            val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPref.getInt("user_id", -1)

            if (!databaseHelper.hasUserCategories(userId)) {
                AlertDialog.Builder(this)
                    .setTitle("Kategori Tidak Ditemukan")
                    .setMessage("Silakan buat kategori terlebih dahulu sebelum menambahkan task.")
                    .setPositiveButton("Buat Kategori") { _, _ ->
                        val intent = Intent(this, NewCategoryActivity::class.java)
                        intent.putExtra("user_id", userId)
                        startActivity(intent)
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            } else {
                startActivity(Intent(this, CreateTaskActivity::class.java))
            }
        }

        binding.viewCompletedText.setOnClickListener {
            startActivity(Intent(this, CompletedTaskActivity::class.java))
        }

        binding.btnCreateCategory.setOnClickListener {
            val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
            val userId = sharedPref.getInt("user_id", -1)

            val intent = Intent(this, NewCategoryActivity::class.java)
            intent.putExtra("user_id", userId)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // BOTTOM NAVIGATION
        binding.bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_tasks -> {
                    // Sudah di halaman tasks
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_calendar -> {
                    val intent = Intent(this, CalendarViewActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        binding.bottomNav.selectedItemId = R.id.nav_tasks
    }

    override fun onResume() {
        super.onResume()
        loadTasks()
    }

    // set up recycler view untuk menampilkan task
    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            taskList = mutableListOf(),
            onCheckedChanged = { task ->
                loadTasks()
            },
            onItemClick = { task ->
                val dialog = TaskDetailDialog(task, isCompleted = false)
                dialog.show(supportFragmentManager, "TaskDetailDialog")
            }
        )

        binding.taskRecyclerView.adapter = adapter
        binding.taskRecyclerView.layoutManager = LinearLayoutManager(this)

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                AlertDialog.Builder(this@LandingPageActivity).apply {
                    setTitle("Hapus Tugas")
                    setMessage("Apakah kamu yakin ingin menghapus tugas ini?")
                    setPositiveButton("Ya") { _, _ ->
                        val deleted = adapter.deleteTaskAndRemoveAt(position, databaseHelper)
                        if (deleted) {
                            Toast.makeText(this@LandingPageActivity, "Tugas berhasil dihapus", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@LandingPageActivity, "Gagal menghapus tugas", Toast.LENGTH_SHORT).show()
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
                val background = ContextCompat.getDrawable(this@LandingPageActivity, R.drawable.rounded_red_background)

                background?.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                background?.draw(c)

                val icon = ContextCompat.getDrawable(this@LandingPageActivity, R.drawable.ic_delete)
                val iconMargin = 32
                val iconTop = itemView.top + (itemView.height - icon!!.intrinsicHeight) / 2
                val iconLeft = itemView.right - iconMargin - icon.intrinsicWidth
                val iconRight = itemView.right - iconMargin
                val iconBottom = iconTop + icon.intrinsicHeight
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                icon.draw(c)

                val paint = Paint().apply {
                    color = ContextCompat.getColor(this@LandingPageActivity, android.R.color.white)
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
    }

    private fun loadTasks() {
        val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        if (userId == -1) {
            startActivity(Intent(this, LandingPageActivity::class.java))
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

        if (taskList.isNotEmpty()) {
            binding.taskRecyclerView.visibility = View.VISIBLE
            binding.emptyIllustration.visibility = View.GONE
            binding.emptyText.visibility = View.GONE
        } else {
            binding.taskRecyclerView.visibility = View.GONE
            binding.emptyIllustration.visibility = View.VISIBLE
            binding.emptyText.visibility = View.VISIBLE
        }


        adapter.taskList.clear()
        adapter.taskList.addAll(taskList)
        adapter.notifyDataSetChanged()
    }
}
