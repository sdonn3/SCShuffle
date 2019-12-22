package com.donnelly.steve.scshuffle.features.player.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.donnelly.steve.scshuffle.R
import com.donnelly.steve.scshuffle.application.ShuffleApplication
import com.donnelly.steve.scshuffle.broadcast.Broadcasters
import com.donnelly.steve.scshuffle.features.player.PlayerActivity
import com.donnelly.steve.scshuffle.features.player.adapter.PlaylistAdapter
import com.donnelly.steve.scshuffle.features.player.viewmodel.PlayerViewModel
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_playlist.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class PlaylistFragment : DaggerFragment() {

    lateinit var viewmodel : PlayerViewModel

    @Inject
    lateinit var broadcasters: Broadcasters

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_playlist, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let{activity->
            viewmodel = ViewModelProviders.of(activity).get(PlayerViewModel::class.java)
            val adapter = PlaylistAdapter(activity, clearCallback = { clearPosition ->
                viewmodel.clearPlaylistPosition(clearPosition)
            })
            broadcasters.playlistChannel.asFlow().onEach{
                adapter.setTrackList(it)
            }.launchIn(lifecycleScope)

            rvPlaylist.setHasFixedSize(true)
            rvPlaylist.layoutManager = LinearLayoutManager(context)
            rvPlaylist.adapter = adapter

            rvPlaylist.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }
}