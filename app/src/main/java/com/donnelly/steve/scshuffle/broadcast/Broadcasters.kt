package com.donnelly.steve.scshuffle.broadcast

import com.donnelly.steve.scshuffle.network.models.Track
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel

@ExperimentalCoroutinesApi
class Broadcasters {
    val playlistChannel = BroadcastChannel<List<Track>>(capacity = Channel.CONFLATED)
    val trackChannel = BroadcastChannel<Track?>(capacity = Channel.CONFLATED)
    val progressChannel = BroadcastChannel<Int>(capacity = Channel.CONFLATED)
}