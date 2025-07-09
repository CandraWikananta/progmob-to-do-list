package com.progmob.todolist

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper (private val context: Context):

    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){
    companion object{
        private const val DATABASE_NAME = "toDoList.db"
        private const val DATABASE_VERSION = 5

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
        private const val CATEGORY_USER_ID = "user_id"

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
        private const val TASK_COMPLETED = "completed"
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
                $CATEGORY_NAME TEXT,
                $CATEGORY_USER_ID INTEGER,
                FOREIGN KEY ($CATEGORY_USER_ID) REFERENCES $USER_TABLE($USER_ID)
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
                $TASK_COMPLETED INTEGER DEFAULT 0,
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

    fun getUserId(username: String, password: String): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT id FROM $USER_TABLE WHERE $USER_NAME = ? AND $USER_PASSWORD = ?",
            arrayOf(username, password)
        )

        var userId = -1
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
        }
        cursor.close()
        return userId
    }


    // Insert Task
    fun insertTask(userId: Int, categoryId: Int, title: String, description: String, priority: String, dueDate: String, dueTime: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("user_id", userId)
            put("category_id", categoryId)
            put("title", title)
            put("description", description)
            put("priority", priority)
            put("due_date", dueDate)
            put("due_time", dueTime)
            put("completed", 0)
        }
        return db.insert("task_tb", null, values)
    }


    // Get all tasks (by user)
    fun getAllTasks(userId: Int): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $TASK_TABLE WHERE $TASK_USER_ID = ?", arrayOf(userId.toString()))
    }

    // Get task by ID
    fun getTaskById(taskId: Int): Cursor {
        val db = readableDatabase
        return db.rawQuery("SELECT * FROM $TASK_TABLE WHERE $TASK_ID = ?", arrayOf(taskId.toString()))
    }

    // Update Task
    fun updateTask(id: Int, title: String, desc: String, priority: String, date: String, time: String, categoryId: Int): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("title", title)
        contentValues.put("description", desc)
        contentValues.put("priority", priority)
        contentValues.put("due_date", date)
        contentValues.put("due_time", time)
        contentValues.put("category_id", categoryId)

        return db.update("task_tb", contentValues, "id=?", arrayOf(id.toString()))
    }


    // Delete Task
    fun deleteTask(taskId: Int): Int {
        val db = writableDatabase
        return db.delete(TASK_TABLE, "$TASK_ID = ?", arrayOf(taskId.toString()))
    }

    // update kolom completed jika di centang
    fun updateTaskCompleted(taskId: Int, completed: Boolean): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("completed", if (completed) 1 else 0)
        }
        return db.update("task_tb", values, "id=?", arrayOf(taskId.toString()))
    }

    // ambil task sudah selesai
    fun getCompletedTasks(userId: Int): Cursor {
        val db = readableDatabase
        return db.rawQuery(
            "SELECT * FROM $TASK_TABLE WHERE $TASK_USER_ID = ? AND $TASK_COMPLETED = 1",
            arrayOf(userId.toString())
        )
    }

    // ambil task belum selesai
    fun getIncompleteTasks(userId: Int): Cursor {
        val db = readableDatabase
        return db.rawQuery(
            "SELECT * FROM $TASK_TABLE WHERE $TASK_USER_ID = ? AND $TASK_COMPLETED = 0",
            arrayOf(userId.toString())
        )
    }

    // fungsi menghapus semua completed task
    fun deleteAllCompletedTasks(userId: Int): Int {
        val db = this.writableDatabase
        return db.delete(TASK_TABLE, "$TASK_USER_ID = ? AND $TASK_COMPLETED = 1", arrayOf(userId.toString()))
    }

    // fungsi insert category
    fun insertCategory(name: String, userId: Int): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("user_id", userId)
        }
        return db.insert("category_tb", null, values)
    }

    // mengambil category yang user buat
    fun getCategoriesByUser(userId: Int): List<Pair<Int, String>> {
        val db = readableDatabase
        val list = mutableListOf<Pair<Int, String>>()
        val cursor = db.rawQuery(
            "SELECT id, name FROM category_tb WHERE user_id = ?",
            arrayOf(userId.toString())
        )
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                list.add(Pair(id, name))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getCategoryNameById(id: Int): String {
        val db = this.readableDatabase
        val cursor = db.query(
            "category_tb",
            arrayOf("name"),
            "id = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        var name = "Tidak diketahui"
        if (cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
        }
        cursor.close()
        return name
    }

    // mengambil nama category berdasarkan id
    fun getCategoryNameByTaskId(taskId: Int): String {
        val db = this.readableDatabase
        val query = """
        SELECT c.name FROM category_tb c
        JOIN task_tb t ON t.category_id = c.id
        WHERE t.id = ?
    """
        val cursor = db.rawQuery(query, arrayOf(taskId.toString()))
        var name = "Tidak diketahui"

        if (cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
        }

        cursor.close()
        return name
    }

    // mengecek apakah user sudah membuat category
    fun hasUserCategories(userId: Int): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM category_tb WHERE user_id = ?", arrayOf(userId.toString()))
        var hasCategory = false
        if (cursor.moveToFirst()) {
            hasCategory = cursor.getInt(0) > 0
        }
        cursor.close()
        return hasCategory
    }

    // mengambil username berdasarkan ID
    fun getUsernameById(userId: Int): String {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT name FROM $USER_TABLE WHERE $USER_ID = ?", arrayOf(userId.toString()))

        var username = "User"
        if (cursor.moveToFirst()) {
            username = cursor.getString(cursor.getColumnIndexOrThrow("name"))
        }
        cursor.close()
        return username
    }

    // menghitung task yang sudah selesai
    fun getCompletedTaskCount(userId: Int): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TASK_TABLE WHERE user_id = ? AND completed = 1",
            arrayOf(userId.toString())
        )
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    // menghitung task tersisa
    fun getRemainingTaskCount(userId: Int): Int {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TASK_TABLE WHERE user_id = ? AND completed = 0",
            arrayOf(userId.toString())
        )
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    // mengambil data user berdasarkan ID
    fun getUserById(userId: Int): Cursor {
        val db = this.readableDatabase
        return db.rawQuery("SELECT * FROM $USER_TABLE WHERE $USER_ID = ?", arrayOf(userId.toString()))
    }

    // fungsi update User
    fun updateUser(userId: Int, name: String, email: String, password: String): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("email", email)
            put("password", password)
        }
        return db.update(USER_TABLE, values, "$USER_ID = ?", arrayOf(userId.toString()))
    }

    // menghapus semua task berdasarkan user id
    fun deleteAllTasksByUserId(userId: Int): Int {
        val db = writableDatabase
        return db.delete(TASK_TABLE, "$TASK_USER_ID = ?", arrayOf(userId.toString()))
    }

    // menghapus semua category berdasarkan user id
    fun deleteAllCategoriesByUserId(userId: Int): Int {
        val db = writableDatabase
        return db.delete(CATEGORY_TABLE, "$CATEGORY_USER_ID = ?", arrayOf(userId.toString()))
    }

    // menghapus user
    fun deleteUser(userId: Int): Int {
        val db = writableDatabase
        return db.delete(USER_TABLE, "$USER_ID = ?", arrayOf(userId.toString()))
    }


}