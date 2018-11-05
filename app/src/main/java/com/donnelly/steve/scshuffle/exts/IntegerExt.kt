package com.donnelly.steve.scshuffle.exts

fun Int.transformDuration(): String {
    val numSeconds = this / 1000
    val minutes = (numSeconds / 60).toString()
    val seconds = (numSeconds % 60).toString().padStart(2, '0')

    return "$minutes:$seconds"
}

fun Long.transformDuration(): String {
    val numSeconds = this / 1000
    val minutes = (numSeconds / 60).toString()
    val seconds = (numSeconds % 60).toString().padStart(2, '0')

    return "$minutes:$seconds"
}