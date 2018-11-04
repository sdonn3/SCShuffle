package com.donnelly.steve.scshuffle.network.models

data class WaveFormResponse (
        val width: Int = 0,
        val height: Int = 0,
        val samples: List<Float>? = null
)