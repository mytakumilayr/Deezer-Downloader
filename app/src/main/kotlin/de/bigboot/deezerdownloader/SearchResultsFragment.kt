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
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_search_results.*
import kotlinx.android.synthetic.main.fragment_search_results.view.*

class SearchResultsFragment : Fragment() {
    private var mParam1: String? = null
    private var mParam2: String? = null
    private lateinit var viewpager: ViewPager
    private lateinit var adapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments.getString(ARG_PARAM1)
            mParam2 = arguments.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_search_results, container, false)

        viewpager = view.viewpager

        setupViewPager(viewpager)
        view.tabs.setupWithViewPager(viewpager)


        return view
    }


    fun setupViewPager(viewPager: ViewPager) {
        viewPager.adapter = ViewPagerAdapter(childFragmentManager).apply {
            addFragment(SearchResultListFragment.newInstance(Entry.Type.Track), "Tracks")
            addFragment(SearchResultListFragment.newInstance(Entry.Type.Album), "Albums")
            addFragment(SearchResultListFragment.newInstance(Entry.Type.Playlist), "Playlists")
            addFragment(SearchResultListFragment.newInstance(Entry.Type.Artist), "Artists")

            this@SearchResultsFragment.adapter = this
        }
    }

    val selectedItems get() = adapter.fragments
            .filterIsInstance<SearchResultListFragment>().flatMap { it.selectedItems }

    fun clearSelection() {
        (viewpager.adapter as ViewPagerAdapter)
                .fragments.filterIsInstance<SearchResultListFragment>()
                .forEach { it.clearSelection() }
    }

    private class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        private data class FragmentWrapper(val name: String, val fragment: Fragment)
        private val _fragments: MutableList<FragmentWrapper> = ArrayList()

        override fun getCount() = _fragments.size
        override fun getItem(position: Int) = _fragments[position].fragment
        override fun getPageTitle(position: Int) = _fragments[position].name


        fun addFragment(fragment: Fragment, title: String) {
            _fragments.add(FragmentWrapper(title, fragment))
        }

        val fragments get() =_fragments.map { it.fragment }
    }

    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(): SearchResultsFragment {
            val fragment = SearchResultsFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
