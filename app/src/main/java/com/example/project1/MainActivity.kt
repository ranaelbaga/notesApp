package com.example.project1

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var db: MyDataBaseHelper
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        db = MyDataBaseHelper(this)
        sharedPreferences = getSharedPreferences("loginPrefs", Context.MODE_PRIVATE)


        val email = findViewById<EditText>(R.id.email)
        val pass = findViewById<EditText>(R.id.password)
        val register = findViewById<Button>(R.id.register)
        val submit = findViewById<Button>(R.id.submit)
        val remember = findViewById<RadioButton>(R.id.remember)

        // Check if user has previously logged in and remember me is checked
        if (sharedPreferences.getBoolean("remembered", false)) {
            val savedEmail = sharedPreferences.getString("email", "")
            val savedPassword = sharedPreferences.getString("password", "")


            if (!savedEmail.isNullOrBlank() && !savedPassword.isNullOrBlank()) {
                val result = db.checkUser(savedEmail, savedPassword)
                if (result) {
                    val intent = Intent(this, NotesActivity::class.java)
                    intent.putExtra("EXTRA_EMAIL", savedEmail)
                    startActivity(intent)
                    finish()
                }
            }
        }

        register.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        submit.setOnClickListener {

            val email1 = email.text.toString()
            val pass1 = pass.text.toString()

            if (db.checkUser(email1, pass1)) {
                // Remember me functionality
                if (remember.isChecked) {
                    // Save email in SharedPreferences
                    with(sharedPreferences.edit()) {
                        putString("email", email1)
                        putString("password", pass1)
                        putBoolean("remembered", true)
                        apply()
                    }
                }

                val intent = Intent(this, NotesActivity::class.java)

                intent.putExtra("EXTRA_EMAIL", email1)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
