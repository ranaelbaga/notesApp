package com.example.project1

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class CustomAdapter internal constructor(
    var activity: Activity,
    private val context: Context,
    var notes: MutableList<Note> = mutableListOf() ,
    private val noteDeletionListener: NoteDeletionListener


) : RecyclerView.Adapter<CustomAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(
            R.layout.note_layout,
            parent,
            false
        )
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val note = notes[position]
        holder.title.text = note.title
        holder.content.text = note.content
        holder.time.text = Utils.formatDateTime(note.timestamp)

        // Handle click on Edit ImageView
        holder.edit.setOnClickListener {
            val intent = Intent(context, UpdateActivity::class.java)
            intent.putExtra("EXTRA_NOTE", note)
            activity.startActivityForResult(intent, 1)
        }

        // Handle click on Delete ImageView
        holder.delete.setOnClickListener {
            noteDeletionListener.onDeleteNoteRequested(note)
        }



    }

    override fun getItemCount(): Int {
        return notes.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var layout: CardView? = itemView.findViewById(R.id.card)
        var title: TextView = itemView.findViewById(R.id.title)
        var time: TextView = itemView.findViewById(R.id.time)
        var content: TextView = itemView.findViewById(R.id.content)
        var edit: ImageView = itemView.findViewById(R.id.edit)
        var delete: ImageView = itemView.findViewById(R.id.delete)
    }

    fun setNotesList(notes: List<Note>) {
        this.notes = notes.toMutableList()
        notifyDataSetChanged()
    }
    fun deleteItem(position: Int) {
        notes.removeAt(position)
        notifyItemRemoved(position)
    }


}
