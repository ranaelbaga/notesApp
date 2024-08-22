package com.example.project1

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AlertDialog // Add this import statement
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.recyclerview.widget.ItemTouchHelper

class NotesActivity : AppCompatActivity(), NoteDeletionListener {

    private lateinit var mydb: MyDataBaseHelper
    private lateinit var email: String
    private lateinit var adapter: CustomAdapter
    private lateinit var searchBar: EditText
    private lateinit var searchButton: Button
    private lateinit var logoutButton: Button
    private lateinit var username: String
    private lateinit var nametextview: TextView

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        mydb = MyDataBaseHelper(this@NotesActivity)
        sharedPreferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)

        adapter = CustomAdapter(this@NotesActivity, this, noteDeletionListener = this)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this@NotesActivity)

        val textViewEmail = findViewById<TextView>(R.id.emailtextview)
        val addButton = findViewById<Button>(R.id.addButton)
        logoutButton = findViewById<Button>(R.id.logout) // Initialize logout button
        searchBar = findViewById<EditText>(R.id.searchBar)
        searchButton = findViewById<Button>(R.id.searchButton)

        email = intent.getStringExtra("EXTRA_EMAIL") ?: ""
        textViewEmail.text = email

        username = mydb.getUsername(email)
        nametextview = findViewById(R.id.nametextview)
        nametextview.text = username

        addButton.setOnClickListener {
            val intent = Intent(this, AddActivity::class.java)
            intent.putExtra("userEmail", email)
            startActivityForResult(intent, 1)
        }

        logoutButton.setOnClickListener {
            logout()
        }

        val notes = mydb.readAllNotes(email ?: "")
        adapter.setNotesList(notes)

        searchButton.setOnClickListener {
            performSearch()
        }

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val note = adapter.notes[position]
                showDeleteConfirmationDialog(note, position)
            }
        })

        itemTouchHelper.attachToRecyclerView(recyclerView)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Log.d("NotesActivity", "onActivityResult: email: $email ")
            val notes = mydb.readAllNotes(email ?: "")

            if (notes.isNotEmpty()) {
                adapter.setNotesList(notes)
            } else {
                Toast.makeText(this, "No notes found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun logout() {
        // Clear SharedPreferences on logout
        with(sharedPreferences.edit()) {
            remove("email")
            remove("remembered")
            remove("password")
            apply()
        }

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun performSearch() {
        val query = searchBar.text.toString().trim()
        if (query.isNotEmpty()) {
            val searchResults = mydb.searchNotes(email, query)
            if (searchResults.isNotEmpty()) {
                adapter.setNotesList(searchResults)
            } else {
                adapter.setNotesList(emptyList())
                Toast.makeText(this, "No matching notes found", Toast.LENGTH_SHORT).show()
            }
        } else {
            val allNotes = mydb.readAllNotes(email)
            adapter.setNotesList(allNotes)
        }
    }

    override fun onDeleteNoteRequested(note: Note) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete ${note.title}?")
        builder.setMessage("Are you sure you want to delete ${note.title}?")
        builder.setPositiveButton("Yes") { _, _ ->
            onDeleteNoteConfirmed(note)
        }
        builder.setNegativeButton("No", null)
        builder.create().show()
    }

    override fun onDeleteNoteConfirmed(note: Note) {
        val success = mydb.deleteNoteById(note)
        if (success) {
            val notes = mydb.readAllNotes(email)
            adapter.setNotesList(notes)
            Toast.makeText(this, "Note is successfully deleted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Note isn't deleted", Toast.LENGTH_SHORT).show()
        }
    }
    private fun showDeleteConfirmationDialog(note: Note, position: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete ${note.title}?")
        builder.setMessage("Are you sure you want to delete ${note.title}?")
        builder.setPositiveButton("Yes") { _, _ ->
            onDeleteNoteConfirmed(note)
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
            adapter.notifyItemChanged(position) // Restore the item if deletion is cancelled
        }
        builder.create().show()
    }
}
