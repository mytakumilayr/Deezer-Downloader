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
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_downloaditem.view.*
import nl.komponents.kovenant.ui.promiseOnUi


class DownloadItemRecyclerViewAdapter(private val mListener: DownloadItemFragment.OnListFragmentInteractionListener?)
    :RecyclerView.Adapter<DownloadItemRecyclerViewAdapter.ViewHolder>() {
    val mValues: MutableList<Download> = ArrayList()
    lateinit var context: Context

    override fun onCreateViewHolder(parent:ViewGroup, viewType:Int):ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context)
                .inflate(R.layout.fragment_downloaditem, parent, false)
        return ViewHolder(view)
    }

    fun clear() {
        mValues.clear()
        notifyDataSetChanged()
    }


    fun addDownload(download: Download) {
        mValues.add(download)
        notifyItemInserted(mValues.size)
    }

    fun addDownloads(downloads: List<Download>) {
        val start = mValues.size + 1
        mValues.addAll(downloads)
        notifyItemRangeInserted(start, downloads.size)
    }

    fun insertDownloads(downloads: List<Download>, index: Int = itemCount) {
        mValues.addAll(index, downloads)
        notifyItemRangeInserted(index, downloads.size)
    }

    override fun onBindViewHolder(holder: ViewHolder, position:Int) {
        val item = mValues[position]
        holder.mItem = item
        holder.mIndex = position
        holder.mAlternateRowView.visibility = when(position%2 == 0) {
            true  -> View.INVISIBLE
            false -> View.VISIBLE
        }

        val entry = item.entry
        when (entry) {
            is Entry.Track -> bindDownload(holder, entry.track.title, entry.track.album.cover, item)
            is Entry.Album -> bindDownload(holder, entry.album.title, entry.album.cover, item)
            is Entry.Artist -> bindDownload(holder, entry.artist.name, entry.artist.picture, item)
            is Entry.Playlist -> bindDownload(holder, entry.playlist.title, entry.playlist.picture, item)
        }

        holder.mView.setOnClickListener {
            mListener?.onListFragmentInteraction(item)
        }
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.listener?.let {
            holder.mItem?.removeCallback(it)
        }
        holder.mItem = null
        holder.listener = null
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        if (holder.mIndex != -1)
            promiseOnUi { notifyItemChanged(holder.mIndex) }

    }

    private fun bindDownload(holder: ViewHolder, title: String, cover: String, item: Download) {
        holder.mTitleView.text = title

        Picasso.with(context).load(cover).into(holder.mCoverView)

        holder.listener?.let {
            holder.mItem?.removeCallback(it)
            holder.listener = null
        }

        holder.listener = item.onUpdate {
            promiseOnUi {
                updateItem(holder, item)
            }
        }


        updateItem(holder, item)
    }

    private fun updateItem(holder: ViewHolder, item: Download) {
        holder.mProgressView.progress = (item.progress * 100).toInt()
        holder.mStateView.setImageResource(when (item.state) {
            Download.State.Waiting -> R.drawable.ic_state_waiting
            Download.State.Preprocessing -> R.drawable.ic_state_preprocessing
            Download.State.Preprocessed -> R.drawable.ic_state_waiting
            Download.State.Skipped -> R.drawable.ic_state_finished
            Download.State.Running -> R.drawable.ic_state_running
            Download.State.Error -> R.drawable.ic_state_error
            Download.State.Finished -> R.drawable.ic_state_finished
            Download.State.Tagging -> R.drawable.ic_state_tagging
        })
    }

    override fun getItemCount():Int {
        return mValues.size
    }

    inner class ViewHolder(val mView:View): RecyclerView.ViewHolder(mView) {
        val mAlternateRowView = mView.alternate_row
        val mCoverView = mView.cover
        val mTitleView = mView.title
        val mProgressView = mView.progress
        val mStateView = mView.state
        var mItem: Download? = null
        var mIndex = -1

        var listener: ((Download)->Unit)? = null
        override fun toString() = "${super.toString()} \"${mTitleView.text}\""
    }
}
