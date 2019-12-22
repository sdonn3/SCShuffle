package com.donnelly.steve.scshuffle.features.player.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.donnelly.steve.scshuffle.R
import com.donnelly.steve.scshuffle.network.models.Track
import kotlinx.android.synthetic.main.item_playlist_track.view.*
import kotlinx.coroutines.flow.onEach
import reactivecircus.flowbinding.android.view.clicks


class PlaylistAdapter(val context: Context, val clearCallback: (Int) -> Unit) : RecyclerView.Adapter<PlaylistAdapter.TrackViewHolder>() {

    private var playlist = listOf<Track>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder =
            TrackViewHolder(LayoutInflater.from(context).inflate(R.layout.item_playlist_track, parent, false))

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = playlist[position]
        track.let {
            holder.bind(track, position)
        }
    }

    fun setTrackList(trackList: List<Track>) {
        playlist = trackList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = playlist.size

    inner class TrackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(track: Track, position: Int) {
            itemView.apply {
                if (position == 0) {
                    ivClear.visibility = View.GONE
                } else {
                    tvCurrentlyPlaying.visibility = View.GONE
                }

                tvTrackName.text = track.title

                ivClear
                        .clicks()
                        .onEach {
                            clearCallback.invoke(position)
                        }
            }
        }
    }
}