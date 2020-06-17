package com.reisdeveloper.audiorecorder

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.squti.androidwaverecorder.WaveRecorder
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_RECORD_AUDIO = 77

    private lateinit var waveRecorder: WaveRecorder
    private lateinit var filePath: String
    private var isRecording = false
    private var isPaused = false

    private var timer: Timer? = null
    private var recorderSecondsElapsed = 0

    private lateinit var rvRecordings: RecyclerView
    private var recordingsAdapter = FilesAdapter(this@MainActivity)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (supportActionBar != null) {
            supportActionBar!!.setBackgroundDrawable(
                ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimaryDark))
            )
        }
        rvRecordings = findViewById(R.id.rv_recordings)

        record_start_stop.setOnClickListener {
            if (!isRecording) {
                if (permissionGranted()) {
                    startRecording()
                } else {
                    requestPermissions()
                }
            } else {
                stopRecording()
            }
        }

        play.setOnClickListener {
            if (!isPaused) {
                pauseRecording()
            } else {
                resumeRecording()
            }
        }

    }

    private fun permissionGranted()  =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

    private fun requestPermissions(){
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            PERMISSIONS_REQUEST_RECORD_AUDIO
        )
    }

    override fun onResume() {
        super.onResume()
        setAdapter()
    }

    override fun onStop() {
        super.onStop()
        recordingsAdapter.clearAudio()
    }

    private fun startRecording() {
        isRecording = true

        val date = DateTime().Now().getData("yyyy-MM-dd_HHmmss")

        filePath = externalCacheDir?.absolutePath + "/$date.wav"

        waveRecorder = WaveRecorder(filePath)

        /*waveRecorder.onAmplitudeListener = {
            GlobalScope.launch(Dispatchers.Main) {
                amplitude.text = "Amplitude : $it"
            }
        }*/

        waveRecorder.noiseSuppressorActive = true

        waveRecorder.startRecording()
        startTimer()
        record_start_stop.setImageDrawable(getDrawable(R.drawable.aar_ic_stop))
        play.setImageDrawable(getDrawable(R.drawable.aar_ic_pause))
    }

    private fun stopRecording() {
        isRecording = false
        waveRecorder.stopRecording()
        Toast.makeText(this, "File saved at : $filePath", Toast.LENGTH_LONG).show()
        resetTimer()
        recordingsAdapter.addRecording(File(filePath))
        record_start_stop.setImageDrawable(getDrawable(R.drawable.aar_ic_rec))
        play.setImageDrawable(getDrawable(R.drawable.aar_ic_play))
    }

    private fun pauseRecording() {
        record_start_stop.setImageDrawable(getDrawable(R.drawable.aar_ic_rec))
        play.setImageDrawable(getDrawable(R.drawable.aar_ic_play))
        isPaused = true
        waveRecorder.pauseRecording()
        stopTimer()
    }

    private fun resumeRecording() {
        record_start_stop.setImageDrawable(getDrawable(R.drawable.aar_ic_stop))
        play.setImageDrawable(getDrawable(R.drawable.aar_ic_pause))
        isPaused = false
        waveRecorder.resumeRecording()
        startTimer()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_RECORD_AUDIO -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startRecording()
                }
                return
            }

            else -> {
            }
        }
    }

    private fun startTimer() {
        stopTimer()
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                updateTimer()
            }
        }, 0, 1000)
    }

    private fun stopTimer() {
        if (timer != null) {
            timer?.cancel()
            timer?.purge()
            timer = null
        }
    }

    private fun updateTimer() {
        runOnUiThread {
            if (isRecording) {
                recorderSecondsElapsed++
                tv_timer.text = formatSeconds(recorderSecondsElapsed)
            }
        }
    }

    private fun resetTimer(){
        recorderSecondsElapsed = 0
        tv_timer.text = formatSeconds(recorderSecondsElapsed)
        stopTimer()
    }

    private fun formatSeconds(seconds: Int): String? {
        return (getTwoDecimalsValue(seconds / 3600) + ":"
                + getTwoDecimalsValue(seconds / 60) + ":"
                + getTwoDecimalsValue(seconds % 60))
    }

    private fun getTwoDecimalsValue(value: Int): String? {
        return if (value in 0..9) {
            "0$value"
        } else {
            value.toString() + ""
        }
    }

    private fun setAdapter() {
        recordingsAdapter.clearRecordings()
        if(readPath().isNullOrEmpty().not()){
            readPath()?.forEach {
                recordingsAdapter.addRecording(it)
            }

            with(rvRecordings) {
                setHasFixedSize(true)
                itemAnimator = null
                adapter = recordingsAdapter
                layoutManager = LinearLayoutManager(
                    this@MainActivity,
                    RecyclerView.VERTICAL,
                    false
                )
            }
        }

    }

    private fun readPath() = externalCacheDir?.absolutePath?.let{File(it).listFiles()}

}
