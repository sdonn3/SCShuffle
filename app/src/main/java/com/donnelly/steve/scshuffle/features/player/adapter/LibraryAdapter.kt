package com.donnelly.steve.scshuffle.features.player.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.donnelly.steve.scshuffle.R
import com.donnelly.steve.scshuffle.exts.transformDuration
import com.donnelly.steve.scshuffle.network.models.Track
import com.jakewharton.rxbinding2.view.clicks
import kotlinx.android.synthetic.main.item_library_track.view.*
import java.util.concurrent.TimeUnit

class LibraryAdapter (val context: Context) : PagedListAdapter<Track, LibraryAdapter.TrackViewHolder>(LibraryAdapter.TrackDiffCallback()) {

    var libraryAdapterStatus = MutableLiveData<LibraryStatus>()

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
                        .throttleFirst(500L, TimeUnit.MILLISECONDS)
                        .subscribe{
                            libraryAdapterStatus.value = LibraryStatus(
                                    LibraryStatus.Intent.Play,
                                    track
                            )
                        }

                ivQueue
                        .clicks()
                        .throttleFirst(500L, TimeUnit.MILLISECONDS)
                        .subscribe{
                            libraryAdapterStatus.value = LibraryStatus(
                                    LibraryStatus.Intent.Queue,
                                    track
                            )
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

    data class LibraryStatus(
            val intent: Intent,
            val track: Track
    ) {
        enum class Intent {
            Play, Queue
        }
    }
}