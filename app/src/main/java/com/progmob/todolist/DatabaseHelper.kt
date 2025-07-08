package com.progmob.todolist

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper (private val context: Context):

    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){
    companion object{
        private const val DATABASE_NAME = "toDoList.db"
        private const val DATABASE_VERSION = 1

        // Tabel User
        private const val USER_TABLE = "user_tb"
        private const val USER_ID = "id"
        private const val USER_NAME = "name"
        private const val USER_PASSWORD = "password"
        private const val USER_EMAIL = "email"

        // Tabel Category
        private const val CATEGORY_TABLE = "category_tb"
        private const val CATEGORY_ID = "id"
        private const val CATEGORY_NAME = "name"
        private const val CATEGORY_COLOR = "color"

        // Tabel Task
        private const val TASK_TABLE = "task_tb"
        private const val TASK_ID = "id"
        private const val TASK_USER_ID = "user_id"
        private const val TASK_CATEGORY_ID = "category_id"
        private const val TASK_TITLE = "title"
        private const val TASK_DESCRIPTION = "description"
        private const val TASK_PRIORITY = "priority"
        private const val TASK_DUE_DATE = "due_date"
        private const val TASK_DUE_TIME = "due_time"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createUserTable = """
            CREATE TABLE $USER_TABLE (
                $USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $USER_NAME TEXT,
                $USER_PASSWORD TEXT,
                $USER_EMAIL TEXT
            )
        """.trimIndent()

        val createCategoryTable = """
            CREATE TABLE $CATEGORY_TABLE (
                $CATEGORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                ${CATEGORY_NAME}_NAME TEXT,
                $CATEGORY_COLOR TEXT
            )
        """.trimIndent()

        val createTaskTable = """
            CREATE TABLE $TASK_TABLE (
                $TASK_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $TASK_USER_ID INTEGER,
                $TASK_CATEGORY_ID INTEGER,
                $TASK_TITLE TEXT,
                $TASK_DESCRIPTION TEXT,
                $TASK_PRIORITY TEXT,
                $TASK_DUE_DATE DATE,
                $TASK_DUE_TIME DATETIME,
                FOREIGN KEY ($TASK_USER_ID) REFERENCES $USER_TABLE($USER_ID),
                FOREIGN KEY ($TASK_CATEGORY_ID) REFERENCES $CATEGORY_TABLE($CATEGORY_ID)
            )
        """.trimIndent()
        db?.execSQL(createUserTable)
        db?.execSQL(createCategoryTable)
        db?.execSQL(createTaskTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dropTableUser = "DROP TABLE IF EXISTS $USER_TABLE"
        val dropTableCategory = "DROP TABLE IF EXISTS $CATEGORY_TABLE"
        val dropTableTask = "DROP TABLE IF EXISTS $TASK_TABLE"

        db?.execSQL(dropTableUser)
        db?.execSQL(dropTableCategory)
        db?.execSQL(dropTableTask)

        onCreate(db)
    }

    fun insertUser(username: String, password: String, email:String): Long {
        val values =ContentValues().apply {
            put(USER_NAME, username)
            put(USER_PASSWORD, password)
            put(USER_EMAIL, email)
        }
        val db = writableDatabase
        return db.insert(USER_TABLE, null, values)
    }

    fun readUser(username: String, password: String): Boolean{
        val db = readableDatabase
        val selection = "$USER_NAME = ? AND $USER_PASSWORD = ?"
        val selectionArgs = arrayOf(username, password)
        val cursor = db.query(USER_TABLE, null, selection, selectionArgs,null, null, null)

        val userExists = cursor.count > 0
        cursor.close()
        return userExists
    }
}