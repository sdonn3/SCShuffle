package com.donnelly.steve.scshuffle.features.player.playlist

import com.donnelly.steve.scshuffle.database.dao.TrackDao
import com.donnelly.steve.scshuffle.network.models.Track
import kotlinx.coroutines.*

class Playlist(private val trackDao: TrackDao) {
    private val myPlaylist = mutableListOf<Track>()
    private val firstSongChangedListeners = mutableListOf<(Track?) -> (Unit)>()
    private val playlistChangedListeners = mutableListOf<(List<Track>) -> (Unit)>()

    fun addSong(track: Track) {
        myPlaylist.add(track)
        if (myPlaylist.size == 1) {
            notifyListenersFirstSongChanged()
        }
        notifyPlaylistChanged()
    }
    fun addSongAtFirstPosition(track: Track) {
        myPlaylist.add(0, track)
        firstSongChangedListeners.forEach {
            it.invoke(track)
        }
        notifyPlaylistChanged()
    }
    fun removeSongAtPosition(position: Int) {
        myPlaylist.removeAt(position)
        if (position == 0) {
            notifyListenersFirstSongChanged()
        }
        notifyPlaylistChanged()
    }

    private fun notifyListenersFirstSongChanged() {
        firstSongChangedListeners.forEach {
            it.invoke(myPlaylist.firstOrNull())
        }
    }

    private fun notifyPlaylistChanged() {
        playlistChangedListeners.forEach {
            it.invoke(myPlaylist)
        }
    }

    fun isEmpty() = myPlaylist.isEmpty()

    fun loadRandomSong() = GlobalScope.launch {
        addSong(trackDao.returnRandomTrack())
    }

    fun addFirstSongChangedListener(listener: (Track?) -> (Unit)) {
        firstSongChangedListeners.add(listener)
    }

    fun addPlaylistChangedListener(listener: (List<Track>) -> (Unit)) {
        playlistChangedListeners.add(listener)
    }
}