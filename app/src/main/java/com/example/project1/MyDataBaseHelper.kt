package com.example.project1

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyDataBaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "my_app.db"
        private const val DATABASE_VERSION = 3

        // Login table
        private const val TABLE_LOGIN = "login"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"

        // Notes table
        private const val TABLE_NOTES = "notes"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_USER_EMAIL = "user_email"
        private const val COLUMN_TIMESTAMP = "timestamp"

        const val COLUMN_ID = "_id"

    }

    private val mContext: Context = context

    override fun onCreate(db: SQLiteDatabase) {
        val createLoginTable =
            "CREATE TABLE $TABLE_LOGIN (" +
                    "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_NAME TEXT, " +
                    "$COLUMN_EMAIL TEXT UNIQUE, " +
                    "$COLUMN_PASSWORD TEXT);"

        val createNotesTable =
            "CREATE TABLE $TABLE_NOTES (" +
                    "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_TITLE TEXT, " +
                    "$COLUMN_DESCRIPTION TEXT," +
                    "$COLUMN_TIMESTAMP INTEGER,"+
                    "$COLUMN_USER_EMAIL TEXT)"


        db.execSQL(createLoginTable)
        db.execSQL(createNotesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LOGIN")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTES")
        onCreate(db)
    }

    // User authentication methods

    fun addUser(name: String?, email: String?, password: String?): Boolean {
        val db = this.writableDatabase
        val cv = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PASSWORD, password)
        }
        val result = db.insert(TABLE_LOGIN, null, cv)
        db.close()
        return result != -1L
    }

    fun getUsername(email: String): String
    { Log.d("MyDataBaseHelper", "getUsername: $email")

        val db = this.readableDatabase
        val query = "SELECT $COLUMN_NAME FROM $TABLE_LOGIN WHERE $COLUMN_EMAIL = ?"
        var username = ""
        val cursor = db.rawQuery(query, arrayOf(email))
        if (cursor != null && cursor.count > 0)
        {

            if (cursor.moveToFirst()) {
                Log.d("MyDataBaseHelper", "getUsername: ${cursor.count}")
                username = cursor.getString(0)
                Log.d("MyDataBaseHelper", "getUsername: ${username}")
            }
        }


        cursor.close()

        return username
    }

    fun checkUser(email: String, password: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_LOGIN WHERE $COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(email, password))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // Notes methods

    fun addNote(note:Note): Boolean {
        val db = this.writableDatabase
        db.beginTransaction()
        var success: Boolean
        try {

            Log.d("MyDataBaseHelper", "addNote: $note.title")
            val cv = ContentValues().apply {

                put(COLUMN_TITLE, note.title)
                put(COLUMN_DESCRIPTION, note.content)
                put(COLUMN_TIMESTAMP, note.timestamp)
                put(COLUMN_USER_EMAIL, note.email)




            }
            val result = db.insert(TABLE_NOTES, null, cv)
            success = result != -1L
            Log.d("MyDataBaseHelper", "addNote.Result: $result")
            if (success) {
                db.setTransactionSuccessful()
            } else {
                Toast.makeText(mContext, "Failed to add note", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            success = false
            e.printStackTrace()
            Toast.makeText(mContext, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        } finally {
            db.endTransaction()
            db.close()
        }
        return success
    }

    fun readAllNotes(email: String): List<Note> {
        val query = "SELECT * FROM $TABLE_NOTES WHERE $COLUMN_USER_EMAIL = ?"
        val db = this.readableDatabase
        val noteList = arrayListOf<Note>()
        try{ val cursor = db.rawQuery(query, arrayOf(email))
            if(  cursor != null && cursor.count > 0) {
                Log.d("MyDataBaseHelper", "readAllNotes: ${cursor.count}")
                while (cursor.moveToNext()) {
                    val id = (cursor.getInt(0))
                    val title = (cursor.getString(1))
                    val content = (cursor.getString(2))
                    val timestamp = cursor.getLong(3)

                    val note = Note(
                        id,
                        title,
                        content,
                        email,
                        timestamp= timestamp
                    )
                    noteList.add(note)
                }
            }

            cursor.close()
        }catch (e: Exception ){  e.printStackTrace()

        }

        return noteList.toList()

    }


    fun updateNoteData(note:Note) : Boolean {
        Log.d("MyDataBaseHelper", "updateNoteData: ${note.email}")
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COLUMN_TITLE, note.title)
        cv.put(COLUMN_DESCRIPTION, note.content)
        cv.put(COLUMN_TIMESTAMP, note.timestamp)



        val result = db.update(TABLE_NOTES, cv, "$COLUMN_ID=? AND $COLUMN_USER_EMAIL=?", arrayOf(note.id.toString(),note.email)
        )
        db.close()


        return result != -1
    }

    fun deleteNoteById(note: Note) : Boolean {
        val db = this.writableDatabase
        val result = db.delete(TABLE_NOTES, "$COLUMN_ID=? AND $COLUMN_USER_EMAIL=?", arrayOf(note.id.toString(), note.email))
        db.close()

        if (result == -1) {
            Toast.makeText(mContext, "Failed to Delete Note", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(mContext, "Note Deleted Successfully", Toast.LENGTH_SHORT).show()
        }
        return result != -1
    }


    fun searchNotes(email: String, query: String): List<Note> {
        val db = this.readableDatabase
        val noteList = arrayListOf<Note>()
        val searchQuery = "%$query%" // Adding wildcards for flexible search

        val sqlQuery = "SELECT * FROM $TABLE_NOTES WHERE $COLUMN_USER_EMAIL = ? AND " +
                "($COLUMN_TITLE LIKE ? OR $COLUMN_DESCRIPTION LIKE ?)"

        try {
            val cursor = db.rawQuery(sqlQuery, arrayOf(email, searchQuery, searchQuery))
            if (cursor != null && cursor.count > 0) {
                while (cursor.moveToNext()) {
                    val id = (cursor.getInt(0))
                    val title = (cursor.getString(1))
                    val content = (cursor.getString(2))


                    val note = Note(
                        id,
                        title,
                        content,
                        email
                    )
                    noteList.add(note)
                }
            }
            cursor?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }

        return noteList.toList()
    }


}