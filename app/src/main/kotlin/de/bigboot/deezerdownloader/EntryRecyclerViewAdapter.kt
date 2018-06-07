/*
 * Copyright 2017 BigBoot
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.bigboot.deezerdownloader

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Checkable
import com.squareup.picasso.Picasso
import com.zeloon.deezer.domain.Album
import com.zeloon.deezer.domain.Artist
import com.zeloon.deezer.domain.Playlist
import com.zeloon.deezer.domain.Track
import kotlinExtensions.deezer.durationString
import kotlinx.android.synthetic.main.fragment_search_result_item.view.*


class EntryRecyclerViewAdapter: RecyclerView.Adapter<EntryRecyclerViewAdapter.ViewHolder>() {

    lateinit var context: Context
    private val items = ArrayList<Entry>()
    private val mSelected = HashSet<Int>()
    private var mClickListener: ((view: View, position: Int)->Unit)? = null
    private var mLongClickListener: ((view: View, position: Int)->Boolean)? = null

    fun setItems(items: List<Entry>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent:ViewGroup, viewType:Int):ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context)
                .inflate(R.layout.fragment_search_result_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder:ViewHolder, position:Int) {
        val item = items[position]
        holder.mItem = item
        holder.mIndex = position

        holder.mBackgroundView.setImageResource(when {
            mSelected.contains(position) -> R.color.list_background_selected
            position%2 == 1 -> R.color.list_background_alternate
            else  -> R.color.list_background
        })

        holder.mView.setOnClickListener { mClickListener?.invoke(it, position) }
        holder.mView.setOnLongClickListener { mLongClickListener?.invoke(it, position) ?: false }

        when (item) {
            is Entry.Track -> bindTrack(holder, item.track)
            is Entry.Album -> bindAlbum(holder, item.album)
            is Entry.Artist -> bindArtist(holder, item.artist)
            is Entry.Playlist -> bindPlaylist(holder, item.playlist)
        }
    }

    private fun bindTrack(holder: ViewHolder, track: Track)
            = bindEntry(holder, track.title, track.album.title, track.artist.name, track.durationString, imageURL = track.album.cover)

    private fun bindAlbum(holder: ViewHolder, album: Album)
            = bindEntry(holder, album.title, album.artist.name, "${album.nb_tracks?:"0"} tracks", imageURL = album.cover)

    private fun bindArtist(holder: ViewHolder, artist: Artist)
            = bindEntry(holder, artist.name, "Top20", imageURL = artist.picture)

    private fun bindPlaylist(holder: ViewHolder, playlist: Playlist)
            = bindEntry(holder, playlist.title, playlist.creator.name, "${playlist.nb_tracks?:"0"} tracks", imageURL = playlist.picture)

    private fun bindEntry(holder: ViewHolder, title: String, subtitle: String = "", info: String = "", additionalInfo: String = "", imageURL: String? = null) {

        holder.mTitleView.text = title
        holder.mSubtitleView.text = subtitle
        holder.mInfoView.text = info
        holder.mAdditionalInfoView.text = additionalInfo

        Picasso.with(context).load(imageURL).into(holder.mCoverView)

    }

    override fun getItemCount():Int {
        return items.size
    }

    fun toggleSelection(pos: Int) {
        if (mSelected.contains(pos))
            mSelected.remove(pos)
        else
            mSelected.add(pos)
        notifyItemChanged(pos)
    }

    fun select(pos: Int, selected: Boolean) {
        if (selected)
            mSelected.add(pos)
        else
            mSelected.remove(pos)
        notifyItemChanged(pos)
    }

    fun selectRange(start: Int, end: Int, selected: Boolean) {
        for (i in start..end) {
            if (selected)
                mSelected.add(i)
            else
                mSelected.remove(i)
        }
        notifyItemRangeChanged(start, end - start + 1)
    }

    fun deselectAll() {
        mSelected.clear()
        notifyDataSetChanged()
    }

    fun selectAll() {
        mSelected += 0 until itemCount
        notifyDataSetChanged()
    }

    val selectedCount: Int get() = mSelected.size
    val selectedIndices: HashSet<Int> get() = mSelected
    val selectedItems: List<Entry> get() = mSelected.map { items[it] }


    fun onClick(listener: (view: View, position: Int)->Unit) {
        mClickListener = listener
    }

    fun onLongClick(listener: (view: View, position: Int)->Boolean) {
        mLongClickListener = listener
    }



    inner class ViewHolder(val mView:View):RecyclerView.ViewHolder(mView) {
        val mBackgroundView = mView.item_background
        val mCoverView = mView.cover
        val mTitleView = mView.title
        val mSubtitleView = mView.subtitle
        val mInfoView = mView.info
        val mAdditionalInfoView = mView.additionalInfo
        var mItem: Entry? = null
        var mIndex = -1

        override fun toString() = "${super.toString()} \"${mTitleView.text}\""
    }
}
