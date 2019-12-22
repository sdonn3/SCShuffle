package com.donnelly.steve.scshuffle.features.player.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.donnelly.steve.scshuffle.R
import com.donnelly.steve.scshuffle.exts.transformDuration
import com.donnelly.steve.scshuffle.network.models.Track
import kotlinx.android.synthetic.main.item_library_track.view.*
import kotlinx.coroutines.flow.onEach
import reactivecircus.flowbinding.android.view.clicks

class LibraryAdapter (
        val context: Context,
        val playCallback: (Track) -> Unit,
        val queueCallback: (Track) -> Unit
) : PagedListAdapter<Track, LibraryAdapter.TrackViewHolder>(TrackDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder =
        TrackViewHolder(LayoutInflater.from(context).inflate(R.layout.item_library_track, parent, false))

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = getItem(position)
        track?.let{
            holder.bind(track)
        }
    }

    inner class TrackViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(track: Track) {
            itemView.apply{
                tvTrackName.text = track.title

                track.duration?.let{
                    tvDuration.text = it.transformDuration()
                }

                ivPlay
                        .clicks()
                        .onEach{
                            playCallback.invoke(track)
                        }

                ivQueue
                        .clicks()
                        .onEach{
                            queueCallback.invoke(track)
                        }
            }
        }
    }

    class TrackDiffCallback : DiffUtil.ItemCallback<Track>() {
        override fun areItemsTheSame(oldItem: Track, newItem: Track): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Track, newItem: Track): Boolean {
            return oldItem == newItem
        }
    }
}