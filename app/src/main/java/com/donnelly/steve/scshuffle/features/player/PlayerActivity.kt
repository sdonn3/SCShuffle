package com.donnelly.steve.scshuffle.features.player

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.donnelly.steve.scshuffle.R
import com.donnelly.steve.scshuffle.dagger.Session
import com.donnelly.steve.scshuffle.exts.shuffleApp
import com.donnelly.steve.scshuffle.network.SCService
import com.donnelly.steve.scshuffle.network.SCServiceV2
import com.donnelly.steve.scshuffle.network.models.Track
import com.donnelly.steve.scshuffle.network.models.User
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class PlayerActivity : AppCompatActivity() {
    companion object {
        private const val LIKE_LIMIT = 200
    }

    @Inject
    lateinit var scService: SCService

    @Inject
    lateinit var scServiceV2: SCServiceV2

    @Inject
    lateinit var session: Session

    lateinit var user: User
    private val disposables: CompositeDisposable by lazy { CompositeDisposable() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        shuffleApp.netComponent.inject(this)

        session.authToken?.let{
            disposables += scService.me(it)
                    .flatMap { meResponse ->
                        loadTracks(meResponse.id, null, mutableListOf())
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {trackList ->

                    }
        }
    }


    fun loadTracks(userId: Int, offset: Long?, listOfTracks: MutableList<Track>): Observable<MutableList<Track>> {
        return scServiceV2
                .getLikes(
                        userId = userId,
                        limit = LIKE_LIMIT,
                        offset = offset
                )
                .concatMap { response ->
                    response.next_href?.let{urlString ->
                        response.collection?.forEach {
                            listOfTracks.add(it.track)
                        }
                        loadTracks(userId, Uri.parse(urlString).getQueryParameter("offset").toLong(), listOfTracks)
                    } ?: run{
                        response.collection?.forEach {
                            listOfTracks.add(it.track)
                        }
                        Observable.just(listOfTracks)
                    }
                }

    }
}