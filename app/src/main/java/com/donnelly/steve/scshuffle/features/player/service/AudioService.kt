package com.donnelly.steve.scshuffle.features.player.service

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.donnelly.steve.scshuffle.application.ShuffleApplication
import com.donnelly.steve.scshuffle.broadcast.Broadcasters
import com.donnelly.steve.scshuffle.database.dao.TrackDao
import com.donnelly.steve.scshuffle.network.SCServiceV2
import com.donnelly.steve.scshuffle.network.models.Track
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val NOTIFICATION_CHANNEL = "SC_AUDIO_SERVICE"
private const val NOTIFICATION_ID = 95

class AudioService : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private val binder = AudioBinder()
    private var audioPlayer: MediaPlayer? = null
    private lateinit var notificationManager: NotificationManager

    @Inject
    lateinit var scServiceV2: SCServiceV2
    @Inject
    lateinit var trackDao: TrackDao
    @Inject
    lateinit var broadcasters: Broadcasters

    private var playlist = mutableListOf<Track>()

    override fun onCreate() {
        audioPlayer?.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        audioPlayer = MediaPlayer()
        val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
        audioPlayer?.setAudioAttributes(attributes)
        audioPlayer?.reset()
        audioPlayer?.setOnPreparedListener(this)
        audioPlayer?.setOnCompletionListener(this)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
                .setContentTitle("SCShuffle")
                .setContentText("The SCShuffle app is ready to play your music")
                .build()

        startForeground(NOTIFICATION_ID, notification)
        broadcasters.playlistChannel.asFlow().onEach {  newPlaylist ->
            playlist = newPlaylist.toMutableList()
        }.launchIn(serviceScope)
    }

    override fun onCompletion(mp: MediaPlayer?) {
        goToNextSong()
    }

    private fun getUrlAndStream(track: Track?) {
        track?.streamUrl?.let { urlQueryString ->
            serviceScope.launch {
                val response = scServiceV2.getStreamUrl(urlQueryString)
                broadcasters.trackChannel.offer(track)
                startAudioTrack(response.url)
            }
        }
    }

    private fun startAudioTrack(audioString: String) {
        audioPlayer?.reset()
        if (audioString.isEmpty().not() && audioPlayer != null && audioPlayer?.isPlaying!!.not()) {
            audioPlayer?.apply {
                setDataSource(audioString)
                prepareAsync()
            }
        }
    }

    fun playAudio() {
        audioPlayer?.start()
    }

    fun pauseAudio() {
        audioPlayer?.pause()
    }

    fun queueSong(track: Track) {
        playlist.add(track)
        broadcasters.playlistChannel.offer(playlist)
    }

    fun playSong(track: Track) {
        if (playlist.isNotEmpty()) {
            playlist.add(index = 0, element = track)
            getUrlAndStream(track)
        } else {
            loadRandomTrack()
        }
    }

    fun goToNextSong() {
        if (playlist.size > 1) {
            playlist.removeAt(0)
            getUrlAndStream(playlist[0])
        } else {
            playlist.clear()
            loadRandomTrack()
        }
    }

    override fun onPrepared(player: MediaPlayer?) {
        player?.start()
    }

    inner class AudioBinder : Binder() {
        fun getService(): AudioService = this@AudioService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    private fun loadRandomTrack() {
        serviceScope.launch {
            val track = trackDao.returnRandomTrack()
            getUrlAndStream(track)
            playlist.add(index = 0, element = track)
        }
    }

    override fun onDestroy() {
        serviceJob.cancel()
        notificationManager.cancel(NOTIFICATION_ID)
        super.onDestroy()
    }
}
