package com.donnelly.steve.scshuffle.features.player.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.donnelly.steve.scshuffle.application.ShuffleApplication
import com.donnelly.steve.scshuffle.database.dao.TrackDao
import com.donnelly.steve.scshuffle.network.models.Track
import javax.inject.Inject

class PlayerViewModel : ViewModel() {
    companion object {
        private const val PAGE_SIZE = 50
    }

    @Inject
    lateinit var trackDao: TrackDao

    val trackListLiveData : LiveData<PagedList<Track>> by lazy {
        val dataSourceFactory = trackDao.getAllTracksPaged()
        LivePagedListBuilder<Int, Track>(dataSourceFactory, PAGE_SIZE).build()
    }

    fun init(shuffleApplication: ShuffleApplication) {
        shuffleApplication.playerComponent.inject(this)
    }
}