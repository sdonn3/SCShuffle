package com.donnelly.steve.scshuffle.features.player

import android.content.*
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.donnelly.steve.scshuffle.R
import com.donnelly.steve.scshuffle.dagger.Session
import com.donnelly.steve.scshuffle.database.dao.TrackDao
import com.donnelly.steve.scshuffle.exts.isVisible
import com.donnelly.steve.scshuffle.exts.shuffleApp
import com.donnelly.steve.scshuffle.features.player.adapter.ScreenSlidePagerAdapter
import com.donnelly.steve.scshuffle.features.player.service.AudioService
import com.donnelly.steve.scshuffle.features.player.viewmodel.PlayerViewModel
import com.donnelly.steve.scshuffle.glide.GlideApp
import com.donnelly.steve.scshuffle.network.SCService
import com.donnelly.steve.scshuffle.network.SCServiceV2
import com.donnelly.steve.scshuffle.network.models.Track
import kotlinx.android.synthetic.main.activity_player.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import reactivecircus.flowbinding.android.view.clicks
import reactivecircus.flowbinding.appcompat.queryTextChanges
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
class PlayerActivity : AppCompatActivity() {

    @Inject
    lateinit var scService: SCService
    @Inject
    lateinit var scServiceV2: SCServiceV2

    @Inject
    lateinit var session: Session
    @Inject
    lateinit var trackDao: TrackDao
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private var audioServiceBinder: AudioService.AudioBinder? = null
    private lateinit var viewmodel: PlayerViewModel

    private var serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(componentName: ComponentName?) {}
        override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
            audioServiceBinder = binder as AudioService.AudioBinder
        }
    }

    var currentlyPlayingTrack: Track? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        shuffleApp.playerComponent.inject(this)
        bindAudioService()
        viewmodel = ViewModelProviders.of(this).get(PlayerViewModel::class.java)

        setSupportActionBar(toolbar)
        viewpager.adapter = ScreenSlidePagerAdapter(supportFragmentManager)

        viewmodel.playerStateLiveData.observe(this, Observer { playerState ->
            pbLoading.isVisible = playerState.loadingInProgress
            btnLoad.isVisible = !playerState.loadingInProgress
                    && playerState.songPagedList.isNullOrEmpty()
        })

        viewmodel.playlist.observe(this, Observer {
            Log.d("ScShuffle", "Triggered from song ending")
            if (it.size > 0) {
                currentlyPlayingTrack = it[0]
                toolbar.title = it[0].title
                GlideApp
                        .with(this)
                        .load(currentlyPlayingTrack?.artworkUrl)
                        .into(ivPlayerImage)

                GlideApp
                        .with(this)
                        .asBitmap()
                        .load(currentlyPlayingTrack?.artworkUrl)
                        .into(object : SimpleTarget<Bitmap>() {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                visualizer.clear()
                                val palette = Palette.from(resource).generate()
                                toolbar.setBackgroundColor(palette.getDarkVibrantColor(Color.WHITE))
                                toolbar.setTitleTextColor(palette.getLightVibrantColor(Color.LTGRAY))

                                it[0].waveformUrl?.let { waveUrl ->
                                    disposables += scServiceV2
                                            .getWaveform(waveUrl)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe { waveResponse ->
                                                visualizer.setPalette(palette)
                                                visualizer.setAmplitudes(waveResponse.samples)
                                            }
                                }
                            }
                        })
            }
        })

        btnLoad.clicks()
                .onEach { viewmodel.loadTracksToDatabase()}

        ivPlay.clicks()
                .onEach { audioServiceBinder?.getService()?.playAudio() }
                .launchIn(lifecycleScope)

        ivPause.clicks()
                .onEach { audioServiceBinder?.getService()?.pauseAudio() }
                .launchIn(lifecycleScope)

        ivSkip.clicks()
                .onEach { audioServiceBinder?.getService()?.goToNextSong() }
                .launchIn(lifecycleScope)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_player, menu)
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.apply {
            setOnCloseListener {
                viewmodel.searchEntered("")
                false
            }
            queryHint = "Song Name Search"
            maxWidth = Integer.MAX_VALUE

            queryTextChanges()
                    .debounce(500L)
                    .filter { !searchView.isIconified }
                    .onEach {
                        viewmodel.searchEntered(it.toString())
                    }
                    .launchIn(lifecycleScope)
        }
        return true
    }

    private fun bindAudioService() {
        if (audioServiceBinder == null) {
            Intent(this, AudioService::class.java).also { intent ->
                bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            }
        }
    }
}