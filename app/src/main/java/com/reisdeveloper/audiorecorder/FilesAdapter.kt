package com.reisdeveloper.audiorecorder

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.IOException


class FilesAdapter(val context: Context) : RecyclerView.Adapter<FilesAdapter.ViewHolder>() {

    private val files = mutableListOf<File>()
    private lateinit var mediaPlayer : MediaPlayer

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        LayoutInflater.from(parent.context)
            .inflate(R.layout.list_recordings, parent, false)
            .let { ViewHolder(it) }

    override fun getItemCount(): Int = files.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(files[position]) {
            holder.name.text = this.name
            holder.play.setOnClickListener {
                playAudio(this)
            }

            holder.stop.setOnClickListener {
                stopAudio()
            }
        }
    }

    private fun playAudio(file: File){
        try {
            val fileUri: Uri = Uri.fromFile(file)
            mediaPlayer = MediaPlayer()
            mediaPlayer.apply {
                setDataSource(context, fileUri)
                prepare()
                start()
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    private fun stopAudio(){
        try {
            if (mediaPlayer != null)
                mediaPlayer.stop()
        }catch (t : Throwable){
            t.printStackTrace()
        }
    }

    fun clearAudio(){
        if (mediaPlayer != null)
            mediaPlayer.release()
    }

    fun addRecording(file: File) {
        files.add(file)
        notifyDataSetChanged()
    }

    fun clearRecordings() {
        files.clear()
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.name)
        val play: ImageButton = itemView.findViewById(R.id.play)
        val stop: ImageButton = itemView.findViewById(R.id.stop)
    }
}