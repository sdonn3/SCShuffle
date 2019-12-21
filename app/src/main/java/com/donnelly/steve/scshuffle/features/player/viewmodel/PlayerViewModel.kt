package com.donnelly.steve.scshuffle.features.player.viewmodel

import android.icu.text.AlphabeticIndex
import android.net.Uri
import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.donnelly.steve.scshuffle.dagger.Session
import com.donnelly.steve.scshuffle.database.dao.TrackDao
import com.donnelly.steve.scshuffle.network.SCService
import com.donnelly.steve.scshuffle.network.SCServiceV2
import com.donnelly.steve.scshuffle.network.models.CollectionResponse
import com.donnelly.steve.scshuffle.network.models.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val DATABASE_PAGE_SIZE = 50
private const val LIKED_SONG_LIMIT = 100

class PlayerViewModel : ViewModel() {

    @Inject
    lateinit var session: Session

    @Inject
    lateinit var trackDao: TrackDao

    @Inject
    lateinit var scService: SCService

    @Inject
    lateinit var scServiceV2: SCServiceV2

    private val allTracksLiveData = LivePagedListBuilder(trackDao.getAllTracksPaged(),
            PagedList.Config.Builder()
                    .setPageSize(DATABASE_PAGE_SIZE)
                    .setPrefetchDistance(20)
                    .build()
    ).setInitialLoadKey(0).build()

    private fun getFilteredTracksFromDatabse(input: String): LiveData<PagedList<Track>> =
            LivePagedListBuilder(trackDao.getSearchedTracksPaged(input),
                    PagedList.Config.Builder()
                            .setPageSize(DATABASE_PAGE_SIZE)
                            .setPrefetchDistance(20)
                            .build()
            ).setInitialLoadKey(0).build()

    private var searchString = ""
    private val trackListLiveData: LiveData<PagedList<Track>> = when {
        searchString.isEmpty() -> allTracksLiveData
        else -> getFilteredTracksFromDatabse(searchString)
    }
    private val loadingLiveData = MutableLiveData<Boolean>(false)

    val playerStateLiveData: LiveData<PlayerState> = object : MediatorLiveData<PlayerState>() {
        private var loading : Boolean = false
        private var songList : PagedList<Track>? = null

        init {
            super.addSource(loadingLiveData) { isLoading ->
                loading = isLoading
                value = PlayerState(songList, loading)
            }
            super.addSource(trackListLiveData) { trackList ->
                songList = trackList
                value = PlayerState(songList, loading)
            }
        }

    }

    fun searchEntered(inputString: String) {
        searchString = inputString
    }

    fun loadTracksToDatabase() {
        loadingLiveData.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            var nextUrl: String? = "InitialUrl"
            var offset: Long?
            while (nextUrl != null) {
                val userId = session.authCode?.let { scService.me(it).id } ?: 0
                offset = Uri.parse(nextUrl)?.getQueryParameter("offset")?.toLong()
                val likedSongResponse = scServiceV2.getLikes(
                        userId = userId,
                        limit = LIKED_SONG_LIMIT,
                        offset = offset
                )
                likedSongResponse.collection?.forEach { collectionResponse ->
                    if (addStreamUrlToTrack(collectionResponse)) {
                        trackDao.insert(collectionResponse.track)
                    }
                }
                nextUrl = likedSongResponse.next_href
            }
            loadingLiveData.postValue(false)
        }
    }

    private fun addStreamUrlToTrack(collectionResponse: CollectionResponse): Boolean {
        val media = collectionResponse.track.media
        media?.transcodings?.filter { transcoding ->
            transcoding.format?.protocol?.equals("progressive", true) == true
        }?.forEach { transcoding ->
            collectionResponse.track.streamUrl = transcoding.url
        } ?: run { return false }
        return true
    }
}

class PlayerState(val songPagedList: PagedList<Track>?, val loadingInProgress: Boolean = false)