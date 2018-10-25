package com.donnelly.steve.scshuffle.features.player.service

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder

class AudioServiceBinder : MediaPlayer.OnPreparedListener, Binder(){
    private var audioPlayer: MediaPlayer? = null

    fun startAudioTrack(audioString: String){
        audioPlayer?.reset()
        if (audioString.isEmpty().not() && audioPlayer != null && audioPlayer?.isPlaying!!.not()){
            audioPlayer?.apply{
                setDataSource(audioString)
                prepareAsync()
            }
        }
    }

    fun playAudio() {
        if (audioPlayer?.isPlaying!!.not()) {
            audioPlayer?.prepareAsync()
        }
        else {
            audioPlayer?.start()
        }
    }

    fun stopAudio(){
        audioPlayer?.stop()
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