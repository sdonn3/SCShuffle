package com.donnelly.steve.scshuffle.features.player.service

import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import com.donnelly.steve.scshuffle.broadcast.Broadcasters
import com.donnelly.steve.scshuffle.database.dao.TrackDao
import com.donnelly.steve.scshuffle.network.SCServiceV2
import com.donnelly.steve.scshuffle.network.models.Track
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import dagger.android.DaggerService
import kotlinx.coroutines.*
import javax.inject.Inject

class AudioService : DaggerService() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private val binder = AudioBinder()

    @Inject
    lateinit var scServiceV2: SCServiceV2

    @Inject
    lateinit var trackDao: TrackDao

    lateinit var dataSourceFactory: DefaultDataSourceFactory
    lateinit var exoPlayer: ExoPlayer

    override fun onCreate() {
        super.onCreate()

        exoPlayer = SimpleExoPlayer.Builder(applicationContext).build()
        dataSourceFactory = DefaultDataSourceFactory(applicationContext,
                Util.getUserAgent(applicationContext, application.applicationInfo.name))
        exoPlayer.setForegroundMode(true)
        loadRandomTrack()
    }

    private fun getUrlAndStream(track: Track?) {
        track?.streamUrl?.let { urlQueryString ->
            serviceScope.launch {
                val response = scServiceV2.getStreamUrl(urlQueryString)
                startAudioTrack(response.url)
            }
        }
    }

    private fun startAudioTrack(audioString: String) {
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(audioString))
        exoPlayer.prepare(mediaSource)
        exoPlayer.playWhenReady = true
    }

    inner class AudioBinder : Binder() {
        fun getService(): AudioService = this@AudioService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    private fun loadRandomTrack() {
        serviceScope.launch (Dispatchers.IO) {
            val track = trackDao.returnRandomTrack()
            withContext(Dispatchers.Main) {
                getUrlAndStream(track)
            }
        }
    }

    override fun onDestroy() {
        serviceJob.cancel()
        exoPlayer.release()
        super.onDestroy()
    }
}
