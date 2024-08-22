package com.example.project1

import java.io.Serializable

data class Note(
    val id: Int=0,
    val title: String,
    val content: String,
    val email: String,
    val timestamp:Long=0

): Serializable
