package com.reisdeveloper.audiorecorder.adapters

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.reisdeveloper.audiorecorder.R
import java.io.File


class FilesAdapter(
    val context: Context
) : RecyclerView.Adapter<FilesAdapter.ViewHolder>() {

    private val files = mutableListOf<File>()
    private var mediaPlayer : MediaPlayer? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        LayoutInflater.from(parent.context)
            .inflate(R.layout.list_recordings, parent, false)
            .let {
                ViewHolder(
                    it
                )
            }

    override fun getItemCount(): Int = files.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(files[position]) {
            holder.list_content.setOnLongClickListener {
                confirmDeleteRecord(this)
                true
            }
            holder.name.text = this.name
            holder.play.setOnClickListener {
                playAudio(this)
            }

            holder.stop.setOnClickListener {
                stopAudio()
            }

            holder.share.setOnClickListener {
                shareRecord(this)
            }
        }
    }

    private fun confirmDeleteRecord(file: File){
        val builder = AlertDialog.Builder(context)

        builder.setTitle(R.string.delete_record)
        builder.setMessage(R.string.do_you_really_delete_this_record)
        builder.setPositiveButton(context.getString(R.string.delete)) { dialog, _ ->
            deleteRecord(file)
            dialog.dismiss()
        }
        builder.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
            dialog.dismiss()
        }
        builder.create()
        builder.show()
    }

    private fun deleteRecord(file: File){
        if (file.exists()) {
            deleteRecondingAdapter(file)
            file.delete()
        }
    }

    private fun playAudio(file: File){
        try {
            val fileUri: Uri = Uri.fromFile(file)
            mediaPlayer = MediaPlayer()
            mediaPlayer?.apply {
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
                mediaPlayer?.stop()
        }catch (t : Throwable){
            t.printStackTrace()
        }
    }

    private fun shareRecord(file: File){
        if(file.exists()){
            val audioUri = FileProvider.getUriForFile(
                context,
                context.applicationContext.packageName + ".fileprovider",
                file
            )
            val shareIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, audioUri)
                type = "audio/*"
            }
            context.startActivity(
                Intent.createChooser(
                    shareIntent,
                    context.resources.getText(R.string.share_audio)
                )
            )
        }
    }

    fun clearAudio(){
        if (mediaPlayer != null)
            mediaPlayer?.release()
    }

    fun addRecording(file: File) {
        files.add(file)
        notifyDataSetChanged()
    }

    fun deleteRecondingAdapter(file: File){
        files.remove(file)
        notifyDataSetChanged()
    }

    fun clearRecordings() {
        files.clear()
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val list_content: LinearLayout = itemView.findViewById(R.id.list_content)
        val name: TextView = itemView.findViewById(R.id.name)
        val play: ImageButton = itemView.findViewById(R.id.play)
        val stop: ImageButton = itemView.findViewById(R.id.stop)
        val share: ImageButton = itemView.findViewById(R.id.share)
    }
}