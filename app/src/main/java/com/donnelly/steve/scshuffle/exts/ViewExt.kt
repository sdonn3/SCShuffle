package com.donnelly.steve.scshuffle.exts

import android.view.View

var View.isVisible: Boolean
    get() = this.visibility == View.VISIBLE
    set(value) {
        this.visibility = if (value) View.VISIBLE else View.GONE
    }