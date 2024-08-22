package com.example.project1

interface NoteDeletionListener {
    fun onDeleteNoteRequested(note: Note)
    fun onDeleteNoteConfirmed(note: Note)
}
