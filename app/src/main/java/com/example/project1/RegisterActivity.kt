package com.example.project1

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
    lateinit var db: MyDataBaseHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)
        db = MyDataBaseHelper(this)

        val username = findViewById<EditText>(R.id.name)
        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.pass)
        val registerButton = findViewById<Button>(R.id.button)

        registerButton.setOnClickListener {
            val user = username.text.toString().trim()
            val mail = email.text.toString().trim()
            val pass = password.text.toString().trim()

            if (user.isNotEmpty() && mail.isNotEmpty() && pass.isNotEmpty()) {
                val isInserted = db.addUser(user, mail, pass)
                if (isInserted) {
                    Toast.makeText(this, "User Registered", Toast.LENGTH_SHORT).show()
                    finish() // Go back to the login screen
                } else {
                    Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}