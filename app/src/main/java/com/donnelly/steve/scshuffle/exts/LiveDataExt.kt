package com.donnelly.steve.scshuffle.exts

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.channelFlow

fun <T> LiveData<T>.asFlow() = channelFlow {
    offer(value)
    val observer = Observer<T> { t -> offer(t) }
    observeForever(observer)
    awaitClose {
        removeObserver(observer)
    }
}