package com.donnelly.steve.scshuffle.features.player.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.donnelly.steve.scshuffle.features.player.fragment.LibraryFragment
import com.donnelly.steve.scshuffle.features.player.fragment.PlaylistFragment

class ScreenSlidePagerAdapter(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager) {
    companion object {
        private const val NUM_PAGES = 2
    }

    override fun getItem(position: Int): Fragment {
        return if (position == 0) {
            LibraryFragment()
        }
        else {
            PlaylistFragment()
        }
    }

    override fun getCount(): Int {
        return NUM_PAGES
    }
}