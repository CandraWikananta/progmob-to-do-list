package com.progmob.todolist

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class TaskDetailDialog(private val task: TaskModel) : DialogFragment() {

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // Menghilangkan title
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.layout_bottom_sheet_task_detail, container, false)

        databaseHelper = DatabaseHelper(requireContext())

        val titleText = view.findViewById<TextView>(R.id.taskTitle)
        val descText = view.findViewById<TextView>(R.id.taskDesc)
        val dueText = view.findViewById<TextView>(R.id.taskDue)
        val categoryText = view.findViewById<TextView>(R.id.taskCategory)
        val priorityText = view.findViewById<TextView>(R.id.taskPriority)
        val editButton = view.findViewById<ImageView>(R.id.btnEdit)

        titleText.text = task.title
        descText.text = task.description
        dueText.text = "Due: ${task.dueDate} ${task.dueTime}"
        priorityText.text = "Priority: ${task.priority}"

        // Ambil nama kategori berdasarkan ID dari database
        val categoryName = databaseHelper.getCategoryNameByTaskId(task.id)
        categoryText.text = "Category: $categoryName"

        // Tombol Edit
        editButton.setOnClickListener {
            val intent = Intent(requireContext(), EditTaskActivity::class.java)
            intent.putExtra("TASK_ID", task.id)
            startActivity(intent)
            dismiss()
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}
