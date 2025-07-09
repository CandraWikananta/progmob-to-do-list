package com.progmob.todolist

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout

class LandingActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var menuButton: ImageView
    private lateinit var categoryContainer: LinearLayout
    private lateinit var categoryArrow: ImageView
    private lateinit var categorySubmenu: LinearLayout
    private lateinit var llCreateNew: LinearLayout
    private var isCategoryExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout)
        menuButton = findViewById(R.id.iv_menu)
        categoryContainer = findViewById(R.id.category_container)
        categoryArrow = findViewById(R.id.category_arrow)
        categorySubmenu = findViewById(R.id.category_submenu)
        llCreateNew = findViewById(R.id.ll_create_new)

        // Set initial state
        categorySubmenu.visibility = View.GONE
        categoryArrow.rotation = 270f // Point right when collapsed

        // Set click listener for menu button
        menuButton.setOnClickListener {
            drawerLayout.open()
        }

        // Set click listener for category container
        categoryContainer.setOnClickListener {
            toggleCategorySubmenu()
        }

        llCreateNew.setOnClickListener {
            val intent = Intent(this@LandingActivity, ManageCategoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun toggleCategorySubmenu() {
        isCategoryExpanded = !isCategoryExpanded
        
        if (isCategoryExpanded) {
            // Expand
            categorySubmenu.visibility = View.VISIBLE
            categoryArrow.animate().rotation(90f).setDuration(200).start() // Point up
        } else {
            // Collapse
            categorySubmenu.visibility = View.GONE
            categoryArrow.animate().rotation(270f).setDuration(200).start() // Point right
        }
    }
}