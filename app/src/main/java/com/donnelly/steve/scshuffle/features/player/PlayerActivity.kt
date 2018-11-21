package com.donnelly.steve.scshuffle.features.player

import android.content.*
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.palette.graphics.Palette
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.donnelly.steve.scshuffle.R
import com.donnelly.steve.scshuffle.application.RxBus
import com.donnelly.steve.scshuffle.dagger.Session
import com.donnelly.steve.scshuffle.database.dao.TrackDao
import com.donnelly.steve.scshuffle.exts.shuffleApp
import com.donnelly.steve.scshuffle.exts.transformDuration
import com.donnelly.steve.scshuffle.features.player.adapter.ScreenSlidePagerAdapter
import com.donnelly.steve.scshuffle.features.player.service.AudioService
import com.donnelly.steve.scshuffle.features.player.viewmodel.PlayerViewModel
import com.donnelly.steve.scshuffle.glide.GlideApp
import com.donnelly.steve.scshuffle.network.SCService
import com.donnelly.steve.scshuffle.network.SCServiceV2
import com.donnelly.steve.scshuffle.network.models.CollectionResponse
import com.donnelly.steve.scshuffle.network.models.Track
import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView.queryTextChanges
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_player.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PlayerActivity : AppCompatActivity() {

    companion object {
        private const val LIKE_LIMIT = 100
        private const val LOADED_PREVIOUSLY = "LoadedPreviously"
    }

    @Inject lateinit var scService: SCService
    @Inject lateinit var scServiceV2: SCServiceV2
    @Inject lateinit var session: Session
    @Inject lateinit var trackDao: TrackDao
    @Inject lateinit var sharedPreferences: SharedPreferences
    @Inject lateinit var rxBus: RxBus

    var audioServiceBinder: AudioService.AudioBinder? = null

    private lateinit var viewmodel: PlayerViewModel

    private var serviceConnection = object: ServiceConnection {
        override fun onServiceDisconnected(componentName: ComponentName?) {}
        override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
            audioServiceBinder = binder as AudioService.AudioBinder
        }
    }

    var trackListSize: Int? = null

    var currentlyPlayingTrack : Track? = null

    private val disposables: CompositeDisposable by lazy { CompositeDisposable() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        shuffleApp.playerComponent.inject(this)
        bindAudioService()
        viewmodel = ViewModelProviders.of(this).get(PlayerViewModel::class.java)
        viewmodel.init(shuffleApp)

        setSupportActionBar(toolbar)

        if (sharedPreferences.getBoolean(LOADED_PREVIOUSLY, false)) {
            btnLoad.visibility = View.GONE
            showLoadedScreen()
        } else {
            btnLoad.visibility = View.VISIBLE
        }

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
                        .into(object: SimpleTarget<Bitmap>(){
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                visualizer.clear()
                                val palette = Palette.from(resource).generate()
                                toolbar.setBackgroundColor(palette.getDarkVibrantColor(Color.WHITE))
                                toolbar.setTitleTextColor(palette.getLightVibrantColor(Color.LTGRAY))

                                it[0].waveformUrl?.let{waveUrl ->
                                    disposables += scServiceV2
                                            .getWaveform(waveUrl)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe{waveResponse ->
                                                visualizer.setPalette(palette)
                                                visualizer.setAmplitudes(waveResponse.samples)
                                            }
                                }
                            }
                        })
            }
        })

        disposables += rxBus
                .getEvents()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{
            if (it is Pair<*, *>) {
                val progress = it.first as Int
                val duration = it.second as Int
                val progressString = "${progress.transformDuration()}/${duration.transformDuration()}"
                tvProgress.text = progressString
            }
        }

        disposables += ivPlay
                .clicks()
                .throttleFirst(500L, TimeUnit.MILLISECONDS)
                .subscribe{
                    audioServiceBinder?.getService()?.playAudio()
                }

        disposables += ivPause
                .clicks()
                .throttleFirst(500L, TimeUnit.MILLISECONDS)
                .subscribe{
                    audioServiceBinder?.getService()?.pauseAudio()
                }

        disposables += ivSkip
                .clicks()
                .throttleFirst(500L, TimeUnit.MILLISECONDS)
                .subscribe{
                    audioServiceBinder?.getService()?.goToNextSong()
                }

        disposables += trackDao
                .getCount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    trackListSize = it
                },{},{
                    Toast.makeText(this, "$trackListSize songs in database" , Toast.LENGTH_LONG).show()
                },{})

        disposables += btnLoad
                .clicks()
                .throttleFirst(500L, TimeUnit.MILLISECONDS)
                .subscribe{
                    pbLoading.visibility = View.VISIBLE
                    session.authToken?.let{ token ->
                        disposables += scService.me(token)
                                .flatMap { meResponse ->
                                    loadTracks(meResponse.id, null, mutableListOf())
                                }

                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe ({trackList ->
                                    pbLoading.visibility = View.GONE
                                    Toast.makeText(this, "Added ${trackList.size} songs to library", Toast.LENGTH_LONG).show()
                                    trackListSize = trackList.size
                                    sharedPreferences.edit().putBoolean(LOADED_PREVIOUSLY, true).apply()
                                    btnLoad.visibility = View.GONE
                                    showLoadedScreen()
                                },{error ->
                                    pbLoading.visibility = View.GONE
                                    Toast.makeText(this, error.localizedMessage, Toast.LENGTH_LONG).show()
                                    error.printStackTrace()
                                })
                }
        }
    }

    fun playSong(track: Track) {
        audioServiceBinder?.getService()?.playSong(track)
    }

    fun queueSong(track: Track) {
        audioServiceBinder?.getService()?.queueSong(track)
    }

    fun removeSong(position: Int) {
        audioServiceBinder?.getService()?.removeSong(position)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_player, menu)
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.apply{
            setOnCloseListener {
                viewmodel.searchCleared()
                false
            }
            queryHint = "Song Name Search"
            maxWidth = Integer.MAX_VALUE

            queryTextChanges(this)
                    .debounce(500L, TimeUnit.MILLISECONDS)
                    .filter{ !searchView.isIconified}
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe{
                        viewmodel.searchEntered(it.toString())
                    }
        }
        return true
    }

    private fun showLoadedScreen() {
        viewpager.adapter = ScreenSlidePagerAdapter(supportFragmentManager)
    }

    private fun bindAudioService(){
        if (audioServiceBinder == null) {
            Intent(this, AudioService::class.java).also { intent ->
                bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    //TODO: Move all the song loading stuff to the ViewModel, clean up its UI
    private fun loadTracks(userId: Int, offset: Long?, listOfTracks: MutableList<Track>): Observable<MutableList<Track>> {
        return scServiceV2
                .getLikes(
                        userId = userId,
                        limit = LIKE_LIMIT,
                        offset = offset
                )
                .concatMap { response ->
                    response.next_href?.let{urlString ->
                        response.collection?.forEach {response->
                            populateStreamUrl(response)
                            response.track.streamUrl?.let{
                                trackDao.insert(response.track)
                                listOfTracks.add(response.track)
                            }
                        }
                        val urlOffset = Uri.parse(urlString).getQueryParameter("offset")
                        loadTracks(userId, urlOffset?.toLong(), listOfTracks)
                    } ?: run{
                        response.collection?.forEach {response->
                            populateStreamUrl(response)
                            response.track.streamUrl?.let{
                                trackDao.insert(response.track)
                                listOfTracks.add(response.track)
                            }
                        }
                        Observable.just(listOfTracks)
                    }
                }

    }

    private fun populateStreamUrl(collectionResponse: CollectionResponse) {
        val media = collectionResponse.track.media
        media?.let{
            it.transcodings?.forEach { transcoding ->
                if (transcoding.format?.protocol == "progressive") {
                    collectionResponse.track.streamUrl = transcoding.url
                }
            }
        }
    }
}