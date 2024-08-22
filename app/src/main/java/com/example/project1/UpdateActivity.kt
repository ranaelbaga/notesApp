package com.example.project1

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class UpdateActivity : AppCompatActivity(), NoteDeletionListener {

    private var title: EditText? = null
    private var description: EditText? = null
    private var note: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)

        title = findViewById(R.id.title)
        description = findViewById(R.id.description)
        val updateBtn = findViewById<Button>(R.id.updatebtn)
        val deleteBtn = findViewById<Button>(R.id.deletebtn)
        getAndSetIntentData()

        val ab = supportActionBar
        ab?.title = title?.text.toString()

        updateBtn.setOnClickListener {
            val mydb = MyDataBaseHelper(this@UpdateActivity)
            val result = mydb.updateNoteData(
                Note(
                    note?.id ?: 0,
                    title?.text.toString(),
                    description?.text.toString(),
                    note?.email ?: "",
                    System.currentTimeMillis() / 1000
                )
            )
            if (result) {
                setResult(RESULT_OK)
                Toast.makeText(this@UpdateActivity, "Note is successfully updated", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@UpdateActivity, "Note isn't updated", Toast.LENGTH_SHORT).show()
            }
        }

        deleteBtn.setOnClickListener {
            note?.let { onDeleteNoteRequested(it) }
        }
    }

    private fun getAndSetIntentData() {
        if (intent.hasExtra("EXTRA_NOTE")) {
            note = intent.getSerializableExtra("EXTRA_NOTE") as Note?
            title?.setText(note?.title)
            description?.setText(note?.content)
        } else {
            Toast.makeText(this, "No data.", Toast.LENGTH_SHORT).show()
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
        val mydb = MyDataBaseHelper(this@UpdateActivity)
        val result = mydb.deleteNoteById(note)
        if (result) {
            setResult(RESULT_OK)
            Toast.makeText(this@UpdateActivity, "Note is successfully deleted", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this@UpdateActivity, "Note isn't deleted", Toast.LENGTH_SHORT).show()
        }
    }
}
