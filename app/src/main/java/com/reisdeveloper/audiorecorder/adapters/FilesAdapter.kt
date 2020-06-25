package com.reisdeveloper.audiorecorder.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.reisdeveloper.audiorecorder.R
import com.reisdeveloper.audiorecorder.extensions.formatSeconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*


class FilesAdapter(
    val context: Context
) : RecyclerView.Adapter<FilesAdapter.ViewHolder>() {

    private val files = mutableListOf<File>()
    private var mediaPlayer : MediaPlayer? = null
    private var recorderSecondsElapsed = 0
    private var timer: Timer? = null

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
            holder.listContent.setOnLongClickListener {
                confirmDeleteRecord(this)
                true
            }
            holder.name.text = this.name
            holder.play.setOnClickListener {
                playAudio(this, holder.timerExec)
            }

            holder.stop.setOnClickListener {
                pauseAudio()
                stopTimer()
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

    private fun playAudio(file: File, view: TextView){
        try {
            val fileUri: Uri = Uri.fromFile(file)
            mediaPlayer = MediaPlayer()
            mediaPlayer?.apply {
                setDataSource(context, fileUri)
                prepare()
                start()
            }
            startTimer(view)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    private fun pauseAudio(){
        try {
            if (mediaPlayer != null)
                mediaPlayer?.pause()
        }catch (t : Throwable){
            t.printStackTrace()
        }
    }

    private fun startTimer(view: TextView) {
        stopTimer()
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                updateTimer(view)
            }
        }, 0, 1000)
    }

    @SuppressLint("SetTextI18n")
    private fun updateTimer(view: TextView) {
        GlobalScope.launch (Dispatchers.Main){
            recorderSecondsElapsed++
            view.text = "${"".formatSeconds(recorderSecondsElapsed)} / ${mediaPlayer?.duration?.let {
                "".formatSeconds(it)
            }}"
        }
    }

    private fun stopTimer() {
        if (timer != null) {
            timer?.cancel()
            timer?.purge()
            timer = null
        }
    }

    private fun resetTimer(view: TextView){
        recorderSecondsElapsed = 0
        view.text = "".formatSeconds(recorderSecondsElapsed)
        stopTimer()
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
        val listContent: LinearLayoutCompat = itemView.findViewById(R.id.list_content)
        val name: TextView = itemView.findViewById(R.id.name)
        val play: ImageButton = itemView.findViewById(R.id.iv_accept)
        val stop: ImageButton = itemView.findViewById(R.id.stop)
        val share: ImageButton = itemView.findViewById(R.id.share)
        val timerExec: TextView = itemView.findViewById(R.id.tv_timer_exec)
    }
}