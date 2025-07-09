package com.progmob.todolist

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class NewCategoryActivity : AppCompatActivity() {

    private lateinit var ivBack: ImageView
    private lateinit var tvBack: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_category)

        ivBack = findViewById(R.id.iv_back)
        tvBack = findViewById(R.id.tv_back)

        ivBack.setOnClickListener { finish() }
        tvBack.setOnClickListener { finish() }
    }
}