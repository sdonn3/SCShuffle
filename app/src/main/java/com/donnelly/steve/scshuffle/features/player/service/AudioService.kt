package com.donnelly.steve.scshuffle.features.player.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.donnelly.steve.scshuffle.application.ShuffleApplication
import com.donnelly.steve.scshuffle.database.dao.TrackDao
import com.donnelly.steve.scshuffle.network.SCServiceV2
import com.donnelly.steve.scshuffle.network.models.Track
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class AudioService : Service(), MediaPlayer.OnPreparedListener,  MediaPlayer.OnCompletionListener {

    private val binder = AudioBinder()

    private var audioPlayer: MediaPlayer? = null

    @Inject lateinit var scServiceV2: SCServiceV2
    @Inject lateinit var rxBus: RxBus
    @Inject lateinit var trackDao: TrackDao

    private val playlist = ArrayList<Track>()

    private val disposables: CompositeDisposable by lazy { CompositeDisposable() }

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

        (application as ShuffleApplication).playerComponent.inject(this)

        //TODO: Test if this is necessary
        val wifiLock: WifiManager.WifiLock = (applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "SCShuffle::PlayerActivity::WakeLock")
        wifiLock.acquire()
        Thread(MediaObserver()).start()
    }

    override fun onCompletion(mp: MediaPlayer?) {
        goToNextSong()
    }

    private fun getUrlAndStream(track: Track?){
        track?.streamUrl?.let{urlQueryString ->
            disposables += scServiceV2
                    .getStreamUrl(urlQueryString, SCServiceV2.SOUNDCLOUD_CLIENT_ID)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe{response ->
                        Log.d("ScShuffle", "Playing Song")
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
        notifyAboutPlaylistChanges()
    }

    fun playSong(track: Track) {
        if (playlist.isNotEmpty()){
            playlist.clear()
            playlist.add(track)
            getUrlAndStream(track)
            notifyAboutPlaylistChanges()
        }
        else {
            loadRandomTrack()
        }
    }

    fun removeSong(position: Int) {
        if (playlist.size >= (position + 1)) {
            playlist.removeAt(position)
        }
        notifyAboutPlaylistChanges()
    }

    fun goToNextSong() {
        if (playlist.size > 1) {
            playlist.removeAt(0)
            getUrlAndStream(playlist[0])
            notifyAboutPlaylistChanges()
        } else {
            playlist.clear()
            loadRandomTrack()
        }
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
                    Thread.sleep(200)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    inner class AudioBinder : Binder() {
        fun getService() : AudioService = this@AudioService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    private fun notifyAboutPlaylistChanges() {
        rxBus.setEvent(playlist)
    }

    fun loadRandomTrack() {
        disposables += trackDao
                .returnRandomTrack()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Log.d("ScShuffle", "Loaded track from database")
                    playlist.add(it)
                    getUrlAndStream(it)
                    notifyAboutPlaylistChanges()
                }
    }
}
