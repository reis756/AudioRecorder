package com.reisdeveloper.audiorecorder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.squti.androidwaverecorder.WaveRecorder
import com.reisdeveloper.audiorecorder.adapters.FilesAdapter
import com.reisdeveloper.audiorecorder.extensions.formatSeconds
import com.reisdeveloper.audiorecorder.util.DateTime
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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

        instanceViews()

    }

    private fun instanceViews(){
        rvRecordings = findViewById(R.id.rv_recordings)

        iv_start_stop.setOnClickListener {
            if (!isRecording) {
                if (permissionGranted()) {
                    startRecording()
                } else {
                    requestPermissions()
                }
            } else if(isPaused) {
                resumeRecording()
            }else{
                pauseRecording()
            }
        }

        tv_start_stop.text = getString(R.string.record)

        ll_accept.visibility = View.GONE
        iv_accept.setOnClickListener {
            stopRecording()
        }

        ll_discard.visibility = View.GONE
        iv_discard.setOnClickListener {
            discardRecord()
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if(item.itemId == R.id.menu_main) {
            val intent = Intent(applicationContext, About::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            true
        }else super.onOptionsItemSelected(item)

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
        isPaused = false

        val date = DateTime().Now().getData("yyyy-MM-dd_HHmmss")

        filePath = externalCacheDir?.absolutePath + "/$date.wav"

        waveRecorder = WaveRecorder(filePath)

        waveRecorder.onAmplitudeListener = {
            GlobalScope.launch(Dispatchers.Main) {
                if(it > 500 && isPaused.not())
                    ripple.newRipple()
            }
        }

        waveRecorder.noiseSuppressorActive = true

        waveRecorder.startRecording()
        startTimer()
        iv_start_stop.setImageDrawable(getDrawable(R.drawable.aar_ic_pause))
        tv_start_stop.text = getString(R.string.pause)
    }

    private fun pauseRecording() {
        iv_start_stop.setImageDrawable(getDrawable(R.drawable.aar_ic_play))
        tv_start_stop.text = getString(R.string.play)
        ll_discard.visibility = View.VISIBLE
        ll_accept.visibility = View.VISIBLE

        isPaused = true
        waveRecorder.pauseRecording()

        stopTimer()
    }

    private fun resumeRecording() {
        iv_start_stop.setImageDrawable(getDrawable(R.drawable.aar_ic_pause))
        tv_start_stop.text = getString(R.string.pause)
        ll_discard.visibility = View.GONE
        ll_accept.visibility = View.GONE
        isPaused = false
        waveRecorder.resumeRecording()
        startTimer()
    }

    private fun stopRecording() {
        try {
            isRecording = false
            isPaused = false

            waveRecorder.stopRecording()

            Toast.makeText(this, getString(R.string.file_successfully_saved), Toast.LENGTH_LONG).show()

            resetTimer()

            recordingsAdapter.addRecording(File(filePath))

            iv_start_stop.setImageDrawable(getDrawable(R.drawable.aar_ic_rec))
            tv_start_stop.text = getString(R.string.record)
            ll_discard.visibility = View.GONE
            ll_accept.visibility = View.GONE

        }catch (t: Throwable){
            t.stackTrace
        }
    }

    private fun discardRecord(){
        try {
            isRecording = false
            isPaused = false

            waveRecorder.stopRecording()

            resetTimer()

            val f = File(filePath)
            if(f.exists())
                f.delete()

            iv_start_stop.setImageDrawable(getDrawable(R.drawable.aar_ic_rec))
            tv_start_stop.text = getString(R.string.record)
            ll_discard.visibility = View.GONE
            ll_accept.visibility = View.GONE

        }catch (t: Throwable){
            t.stackTrace
        }
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

            else -> {}
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
                tv_timer.text = "".formatSeconds(recorderSecondsElapsed)
            }
        }
    }

    private fun resetTimer(){
        recorderSecondsElapsed = 0
        tv_timer.text = "".formatSeconds(recorderSecondsElapsed)
        stopTimer()
    }

    private fun setAdapter() {
        recordingsAdapter.clearRecordings()

        if(readPath().isNullOrEmpty().not()){
            readPath()?.forEach {
                recordingsAdapter.addRecording(it)
            }
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

    private fun readPath() = externalCacheDir?.absolutePath?.let{File(it).listFiles()}

}
