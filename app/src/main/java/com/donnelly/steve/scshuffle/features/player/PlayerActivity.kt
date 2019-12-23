package com.donnelly.steve.scshuffle.features.player

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.donnelly.steve.scshuffle.R
import com.donnelly.steve.scshuffle.application.Session
import com.donnelly.steve.scshuffle.database.dao.TrackDao
import com.donnelly.steve.scshuffle.exts.isVisible
import com.donnelly.steve.scshuffle.features.player.adapter.ScreenSlidePagerAdapter
import com.donnelly.steve.scshuffle.features.player.service.AudioService
import com.donnelly.steve.scshuffle.features.player.viewmodel.PlayerViewModel
import com.donnelly.steve.scshuffle.network.SCService
import com.donnelly.steve.scshuffle.network.SCServiceV2
import dagger.android.support.DaggerAppCompatActivity
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
class PlayerActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var scService: SCService
    @Inject
    lateinit var scServiceV2: SCServiceV2
    @Inject
    lateinit var session: Session
    @Inject
    lateinit var trackDao: TrackDao

    private lateinit var viewmodel: PlayerViewModel

    private var audioServiceBinder: AudioService.AudioBinder? = null
    private var serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(componentName: ComponentName?) {}
        override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
            audioServiceBinder = binder as AudioService.AudioBinder
            playerControl.player = binder.getService().exoPlayer
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        viewmodel = ViewModelProvider(this, viewModelFactory)[PlayerViewModel::class.java]

        Intent(this, AudioService::class.java).also { intent ->
            bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        }

        setSupportActionBar(toolbar)
        viewpager.adapter = ScreenSlidePagerAdapter(supportFragmentManager)

        viewmodel.loadingStateLiveData.observe(this, Observer { playerState ->
            pbLoading.isVisible = playerState.loadingInProgress
            btnLoad.isVisible = !playerState.loadingInProgress
                    && playerState.songPagedList.isNullOrEmpty()
        })

        btnLoad.clicks()
                .onEach { viewmodel.loadTracksToDatabase()}
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
}