package com.donnelly.steve.scshuffle.features.player.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.donnelly.steve.scshuffle.application.RxBus
import com.donnelly.steve.scshuffle.application.ShuffleApplication
import com.donnelly.steve.scshuffle.database.dao.TrackDao
import com.donnelly.steve.scshuffle.network.models.Track
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

class PlayerViewModel : ViewModel() {
    companion object {
        private const val PAGE_SIZE = 50
    }

    @Inject lateinit var trackDao: TrackDao
    @Inject lateinit var rxBus: RxBus

    var livePagedList: LiveData<PagedList<Track>>? = null
    private var searchString: String? = null

    val searchLiveData: MutableLiveData<SearchStatus> by lazy {
        val liveData = MutableLiveData<SearchStatus>()
        liveData.postValue(SearchStatus(Status.SUCCESS))
        liveData
    }

    private val disposables: CompositeDisposable by lazy { CompositeDisposable() }

    val playlist: MutableLiveData<ArrayList<Track>> by lazy {
        val liveData = MutableLiveData<ArrayList<Track>>()
        liveData.postValue(ArrayList())
        liveData
    }

    fun init(shuffleApplication: ShuffleApplication) {
        shuffleApplication.playerComponent.inject(this)
        livePagedList = getTracks()
        searchLiveData.value = SearchStatus(Status.SUCCESS)
        disposables += rxBus
                .getEvents()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{
                    if (it is ArrayList<*>) {
                        playlist.value = it as ArrayList<Track>
                    }
                }

    }

    private fun getTracks(): LiveData<PagedList<Track>> = LivePagedListBuilder(trackDao.getAllTracksPaged(),
            PagedList.Config.Builder()
                    .setPageSize(PAGE_SIZE)
                    .setPrefetchDistance(20)
                    .build())
            .setInitialLoadKey(0)
            .build()

    private fun getFilteredTracks(input: String): LiveData<PagedList<Track>> = LivePagedListBuilder(trackDao.getSearchedTracksPaged(input),
            PagedList.Config.Builder()
                    .setPageSize(PAGE_SIZE)
                    .setPrefetchDistance(20)
                    .build())
            .setInitialLoadKey(0)
            .build()

    fun searchEntered(inputString: String) {
        searchString = inputString
        livePagedList = getFilteredTracks(inputString)
        searchLiveData.value = SearchStatus(Status.SUCCESS)
    }

    fun searchCleared() {
        searchString = null
        livePagedList = getTracks()
        searchLiveData.value = SearchStatus(Status.SUCCESS)
    }

    class SearchStatus(
            val status: Status = Status.SUCCESS
    )

    enum class Status {
        SUCCESS
    }
}