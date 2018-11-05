package com.donnelly.steve.scshuffle.features.player.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.donnelly.steve.scshuffle.R
import com.donnelly.steve.scshuffle.network.models.Track
import com.jakewharton.rxbinding2.view.clicks
import kotlinx.android.synthetic.main.item_playlist_track.view.*
import java.util.concurrent.TimeUnit


class PlaylistAdapter (val context: Context) : RecyclerView.Adapter<PlaylistAdapter.TrackViewHolder>() {

    var playlistStatus = MutableLiveData<PlaylistStatus>()

    var playlist : ArrayList<Track> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder =
            TrackViewHolder(LayoutInflater.from(context).inflate(R.layout.item_playlist_track, parent, false))


    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = playlist[position]
        track.let{
            holder.bind(track, position)
        }
    }

    fun setTrackList(trackList: ArrayList<Track>) {
        playlist = trackList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = playlist.size

    inner class TrackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(track: Track, position: Int) {
            itemView.apply{
                if (position == 0) {
                    ivClear.visibility = View.GONE
                }
                else {
                    tvCurrentlyPlaying.visibility = View.GONE
                }

                tvTrackName.text = track.title

                ivClear
                        .clicks()
                        .throttleFirst(500L, TimeUnit.MILLISECONDS)
                        .subscribe{
                            playlistStatus.value = PlaylistStatus(
                                    PlaylistStatus.Intent.Clear,
                                    position
                            )
                        }
            }
        }
    }

    data class PlaylistStatus(
            val intent: Intent,
            val position: Int
    ) {
        enum class Intent {
            Clear
        }
    }
}