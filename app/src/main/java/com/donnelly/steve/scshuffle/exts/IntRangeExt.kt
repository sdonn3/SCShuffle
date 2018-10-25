package com.donnelly.steve.scshuffle.exts

import java.util.*

fun IntRange.random() = Random().nextInt(((endInclusive+1) - start) + start)