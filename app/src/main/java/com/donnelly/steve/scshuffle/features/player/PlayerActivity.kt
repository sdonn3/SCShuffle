package com.donnelly.steve.scshuffle.features.player

import android.content.*
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import com.donnelly.steve.scshuffle.R
import com.donnelly.steve.scshuffle.dagger.Session
import com.donnelly.steve.scshuffle.database.dao.TrackDao
import com.donnelly.steve.scshuffle.exts.shuffleApp
import com.donnelly.steve.scshuffle.features.player.adapter.ScreenSlidePagerAdapter
import com.donnelly.steve.scshuffle.features.player.service.AudioService
import com.donnelly.steve.scshuffle.features.player.service.AudioServiceBinder
import com.donnelly.steve.scshuffle.features.player.viewmodel.PlayerViewModel
import com.donnelly.steve.scshuffle.network.SCService
import com.donnelly.steve.scshuffle.network.SCServiceV2
import com.donnelly.steve.scshuffle.network.models.CollectionResponse
import com.donnelly.steve.scshuffle.network.models.Track
import com.jakewharton.rxbinding2.view.clicks
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_player.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PlayerActivity : FragmentActivity() {
    companion object {
        private const val LIKE_LIMIT = 100
        private const val LOADED_PREVIOUSLY = "LoadedPreviously"
    }

    @Inject lateinit var scService: SCService
    @Inject lateinit var scServiceV2: SCServiceV2
    @Inject lateinit var session: Session
    @Inject lateinit var trackDao: TrackDao
    @Inject lateinit var sharedPreferences: SharedPreferences

    var audioServiceBinder: AudioServiceBinder? = null

    lateinit var viewmodel: PlayerViewModel

    private var serviceConnection = object: ServiceConnection {
        override fun onServiceDisconnected(componentName: ComponentName?) {}
        override fun onServiceConnected(componentName: ComponentName?, binder: IBinder?) {
            audioServiceBinder = binder as AudioServiceBinder
            audioServiceBinder?.initAudioPlayer()
        }
    }

    var trackListSize: Int? = null

    private val disposables: CompositeDisposable by lazy { CompositeDisposable() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        shuffleApp.playerComponent.inject(this)
        bindAudioService()
        viewmodel = ViewModelProviders.of(this).get(PlayerViewModel::class.java)
        viewmodel.init(shuffleApp)

        if (sharedPreferences.getBoolean(LOADED_PREVIOUSLY, false)) {
            btnLoad.visibility = View.GONE
            showLoadedScreen()
        } else {
            btnLoad.visibility = View.VISIBLE
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
                .subscribe{ _ ->
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
                                },{
                                    pbLoading.visibility = View.GONE
                                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_LONG).show()
                                    it.printStackTrace()
                                })
                }
        }
    }

    private fun showLoadedScreen() {
        viewpager.adapter = ScreenSlidePagerAdapter(supportFragmentManager)
    }

    private fun bindAudioService(){
        if (audioServiceBinder == null) {
            val intent = Intent(this, AudioService::class.java)
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    fun getUrlAndStream(track: Track?){
        track?.streamUrl?.let{urlQueryString ->
            disposables += scServiceV2
                    .getStreamUrl(urlQueryString, SCServiceV2.SOUNDCLOUD_CLIENT_ID)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe{response ->
                        audioServiceBinder?.startAudioTrack(response.url)
                    }
        }
    }

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
                        loadTracks(userId, Uri.parse(urlString).getQueryParameter("offset").toLong(), listOfTracks)
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