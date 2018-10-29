package com.donnelly.steve.scshuffle.features.player.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.donnelly.steve.scshuffle.application.ShuffleApplication
import com.donnelly.steve.scshuffle.database.dao.TrackDao
import com.donnelly.steve.scshuffle.network.models.Track
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class PlayerViewModel : ViewModel() {
    companion object {
        private const val PAGE_SIZE = 50
    }

    @Inject
    lateinit var trackDao: TrackDao

    private val disposables: CompositeDisposable by lazy { CompositeDisposable() }

    val playlist: MutableLiveData<ArrayList<Track>> by lazy {
        val liveData = MutableLiveData<ArrayList<Track>>()
        liveData.postValue(ArrayList())
        liveData
    }

    val trackListLiveData : LiveData<PagedList<Track>> by lazy {
        val dataSourceFactory = trackDao.getAllTracksPaged()
        LivePagedListBuilder<Int, Track>(dataSourceFactory, PAGE_SIZE).build()
    }

    fun init(shuffleApplication: ShuffleApplication) {
        shuffleApplication.playerComponent.inject(this)
    }

    fun loadRandomTrack() {
        disposables += trackDao
                .returnRandomTrack()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{
                    playlist.value?.add(it)
                    playlist.value = playlist.value
                }
    }
}