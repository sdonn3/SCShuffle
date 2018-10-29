package com.donnelly.steve.scshuffle.features.player.service

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.PowerManager

class AudioServiceBinder : MediaPlayer.OnPreparedListener, Binder(){
    private var audioPlayer: MediaPlayer? = null

    fun requestWakelock(context: Context){
        audioPlayer?.setWakeMode(context , PowerManager.PARTIAL_WAKE_LOCK)
        audioPlayer?.setScreenOnWhilePlaying(true)
    }

    fun startAudioTrack(audioString: String){
        audioPlayer?.reset()
        if (audioString.isEmpty().not() && audioPlayer != null && audioPlayer?.isPlaying!!.not()){
            audioPlayer?.apply{
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

    fun pauseAudio(){
        audioPlayer?.pause()
    }

    fun initAudioPlayer(){
        if (audioPlayer == null){
            audioPlayer = MediaPlayer()
            val attributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
            audioPlayer?.setAudioAttributes(attributes)
        }
        else {
            audioPlayer?.reset()
        }
        audioPlayer?.setOnPreparedListener(this)
    }

    override fun onPrepared(player: MediaPlayer?) {
        player?.start()
    }
}