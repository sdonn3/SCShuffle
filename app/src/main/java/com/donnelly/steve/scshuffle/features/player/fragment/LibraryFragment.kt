package com.donnelly.steve.scshuffle.features.player.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.donnelly.steve.scshuffle.R
import com.donnelly.steve.scshuffle.features.player.adapter.LibraryAdapter
import com.donnelly.steve.scshuffle.features.player.viewmodel.PlayerViewModel
import kotlinx.android.synthetic.main.fragment_library.*

class LibraryFragment : Fragment() {

    lateinit var viewmodel : PlayerViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_library, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let{activity->
            viewmodel = ViewModelProviders.of(activity).get(PlayerViewModel::class.java)
            val adapter = LibraryAdapter(activity)
            rvLibraryTracks.setHasFixedSize(true)
            rvLibraryTracks.layoutManager = LinearLayoutManager(context)
            rvLibraryTracks.adapter = adapter

            viewmodel.trackListLiveData.observe(this, Observer {
                adapter.submitList(it)
            })

            adapter.libraryAdapterStatus.observe(this, Observer {
                when (it.intent) {
                    LibraryAdapter.LibraryStatus.Intent.Play -> {
                        viewmodel.playlist.value?.clear()
                        viewmodel.playlist.value?.add(it.track)
                        viewmodel.playlist.value = viewmodel.playlist.value
                    }
                    LibraryAdapter.LibraryStatus.Intent.Queue -> {
                        viewmodel.playlist.value?.add(it.track)
                        viewmodel.playlist.value = viewmodel.playlist.value
                    }
                }
            })
        }
    }
}