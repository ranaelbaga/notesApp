package com.example.project1

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class AddActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        val title = findViewById<EditText>(R.id.title)
        val description = findViewById<EditText>(R.id.description)
        val add = findViewById<Button>(R.id.addbtn)


        add.setOnClickListener {
            val mydb: MyDataBaseHelper = MyDataBaseHelper(this@AddActivity)
            val userEmail = intent.getStringExtra("userEmail") ?: ""

            val result = mydb.addNote(
                Note(
                    title=title.text.toString().trim(),
                   content= description.text.toString().trim(),
                    email=userEmail,
                    timestamp = System.currentTimeMillis())
            )

            if (result) {
                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this@AddActivity, "Note isn't added", Toast.LENGTH_SHORT).show()
            }
        }
    }
}