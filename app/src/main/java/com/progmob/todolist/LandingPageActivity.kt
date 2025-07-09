package com.progmob.todolist

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
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
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator

class LandingPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLandingPageBinding
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var adapter: TaskAdapter  // properti adapter agar bisa diakses di mana-mana

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLandingPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)

        setupRecyclerView()
        loadTasks()

        binding.addTask.setOnClickListener {
            startActivity(Intent(this, CreateTaskActivity::class.java))
            finish()
        }

        binding.viewCompletedText.setOnClickListener {
            startActivity(Intent(this, CompletedTaskActivity::class.java))
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

    private fun setupRecyclerView() {
        adapter = TaskAdapter(mutableListOf()) { updatedTask ->
            // Callback setelah centang atau hapus
            android.os.Handler().postDelayed({
                loadTasks()
            }, 600)
        }


        binding.taskRecyclerView.adapter = adapter
        binding.taskRecyclerView.layoutManager = LinearLayoutManager(this)

        // Swipe gesture
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
                        adapter.notifyItemChanged(position)  // Batalkan swipe
                    }
                    setCancelable(false)
                    show()
                }
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float,
                actionState: Int, isCurrentlyActive: Boolean
            ) {
                RecyclerViewSwipeDecorator.Builder(
                    c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive
                )
                    .addBackgroundColor(ContextCompat.getColor(this@LandingPageActivity, android.R.color.holo_red_dark))
                    .addActionIcon(R.drawable.ic_delete)
                    .addSwipeLeftLabel("Swipe to Delete")
                    .setSwipeLeftLabelColor(ContextCompat.getColor(this@LandingPageActivity, android.R.color.white))
                    .create()
                    .decorate()

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.taskRecyclerView)
    }

    private fun loadTasks() {
        val sharedPref = getSharedPreferences("user_session", MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        if (userId == -1) {
            startActivity(Intent(this, SignInActivity::class.java))
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

        // Show/hide empty state
        if (taskList.isNotEmpty()) {
            binding.taskRecyclerView.visibility = android.view.View.VISIBLE
            binding.emptyIllustration.visibility = android.view.View.GONE
            binding.emptyText.visibility = android.view.View.GONE
        } else {
            binding.taskRecyclerView.visibility = android.view.View.GONE
            binding.emptyIllustration.visibility = android.view.View.VISIBLE
            binding.emptyText.visibility = android.view.View.VISIBLE
        }

        // Update data in adapter
        adapter.taskList.clear()
        adapter.taskList.addAll(taskList)
        adapter.notifyDataSetChanged()
    }
}
