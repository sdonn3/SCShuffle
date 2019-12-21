package com.donnelly.steve.scshuffle.features.player.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.donnelly.steve.scshuffle.R
import com.donnelly.steve.scshuffle.features.player.PlayerActivity
import com.donnelly.steve.scshuffle.features.player.adapter.LibraryAdapter
import com.donnelly.steve.scshuffle.features.player.viewmodel.PlayerViewModel
import com.donnelly.steve.scshuffle.network.models.Track
import kotlinx.android.synthetic.main.fragment_library.*

class LibraryFragment : Fragment() {

    lateinit var viewmodel : PlayerViewModel
    lateinit var adapter : LibraryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_library, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let{activity->
            viewmodel = ViewModelProviders.of(activity).get(PlayerViewModel::class.java)
            adapter = LibraryAdapter(activity)
            rvLibraryTracks.setHasFixedSize(true)
            rvLibraryTracks.layoutManager = LinearLayoutManager(context)
            rvLibraryTracks.adapter = adapter

            viewmodel.searchLiveData.observe(this, Observer {
                viewmodel.livePagedList?.removeObservers(this)
                viewmodel.livePagedList?.observe(this, Observer { trackList ->
                    observeListItems(trackList)
                })
            })

            rvLibraryTracks.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

            adapter.libraryAdapterStatus.observe(this, Observer {
                when (it.intent) {
                    LibraryAdapter.LibraryStatus.Intent.Play -> {
                        (activity as PlayerActivity).playSong(it.track)
                    }
                    LibraryAdapter.LibraryStatus.Intent.Queue -> {
                        (activity as PlayerActivity).queueSong(it.track)
                    }
                }
            })
        }
    }

    private fun observeListItems(pagedList: PagedList<Track>?) {
        adapter.submitList(pagedList)
        rvLibraryTracks.smoothScrollToPosition(0)
    }
}