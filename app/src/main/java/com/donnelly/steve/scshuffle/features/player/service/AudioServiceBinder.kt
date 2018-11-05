package com.donnelly.steve.scshuffle.features.player.service

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.PowerManager
import com.donnelly.steve.scshuffle.application.RxBus
import java.util.concurrent.atomic.AtomicBoolean

class AudioServiceBinder : MediaPlayer.OnPreparedListener, Binder() {
    private var audioPlayer: MediaPlayer? = null
    private var mediaObserver: MediaObserver? = null
    lateinit var rxBus: RxBus

    fun requestWakelock(context: Context) {
        audioPlayer?.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
        audioPlayer?.setScreenOnWhilePlaying(true)
    }

    fun startAudioTrack(audioString: String) {
        audioPlayer?.reset()
        if (audioString.isEmpty().not() && audioPlayer != null && audioPlayer?.isPlaying!!.not()) {
            audioPlayer?.apply {
                setDataSource(audioString)
                prepareAsync()
            }
        }
    }

    fun setCompletionListener(listener: MediaPlayer.OnCompletionListener) {
        audioPlayer?.setOnCompletionListener(listener)
    }

    fun playAudio() {
        audioPlayer?.start()
    }

    fun pauseAudio() {
        audioPlayer?.pause()
    }

    fun initAudioPlayer() {
        if (audioPlayer == null) {
            audioPlayer = MediaPlayer()
            val attributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
            audioPlayer?.setAudioAttributes(attributes)
        } else {
            audioPlayer?.reset()
        }
        audioPlayer?.setOnPreparedListener(this)
        Thread(MediaObserver()).start()
    }

    override fun onPrepared(player: MediaPlayer?) {
        player?.start()
    }

    inner class MediaObserver : Runnable {
        private val stop = AtomicBoolean(false)

        override fun run() {
            while (!stop.get()) {
                try {
                    audioPlayer?.let {
                        if (it.isPlaying) {
                            rxBus.setEvent(Pair(it.currentPosition, it.duration))
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                try {
                    Thread.sleep(100)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }
}