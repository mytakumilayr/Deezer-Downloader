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
import android.databinding.ObservableList
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.michaelflisar.dragselectrecyclerview.DragSelectTouchListener
import com.michaelflisar.dragselectrecyclerview.DragSelectionProcessor

import kotlinx.android.synthetic.main.fragment_search_result_list.*
import kotlinx.android.synthetic.main.fragment_search_results.*
import nl.komponents.kovenant.ui.promiseOnUi

class SearchResultListFragment : Fragment() {
    // TODO: Customize parameters
    private var mColumnCount = 1
    private var filterType: Entry.Type? = null
    private var mListener: SearchResultSelectionListener? = null
    private var adapter: EntryRecyclerViewAdapter? = null
    private var view: RecyclerView? = null

    private val dragSelectionProcesssor = DragSelectionProcessor(object: DragSelectionProcessor.ISelectionHandler {
        override fun getSelection() = adapter?.selectedIndices?:HashSet<Int>(0)
        override fun isSelected(index: Int) = adapter?.selectedIndices?.contains(index)?:false
        override fun updateSelection(start: Int, end: Int, isSelected: Boolean, calledFromStart: Boolean) {
            adapter?.selectRange(start, end, isSelected)
            mListener?.selectionChanged()
        }
    }).withMode(DragSelectionProcessor.Mode.FirstItemDependentToggleAndUndo)
    private val dragSelectTouchListener = DragSelectTouchListener().withSelectListener(dragSelectionProcesssor)

    private val downloadsListener = object: ObservableList.OnListChangedCallback<ObservableList<Entry>>() {
        override fun onChanged(p0: ObservableList<Entry>?) { updateItems() }
        override fun onItemRangeChanged(p0: ObservableList<Entry>?, p1: Int, p2: Int) { updateItems() }
        override fun onItemRangeMoved(p0: ObservableList<Entry>?, p1: Int, p2: Int, p3: Int) { updateItems() }
        override fun onItemRangeInserted(p0: ObservableList<Entry>?, p1: Int, p2: Int) { updateItems() }
        override fun onItemRangeRemoved(p0: ObservableList<Entry>?, p1: Int, p2: Int) { updateItems() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments != null) {
            mColumnCount = arguments.getInt(ARG_COLUMN_COUNT)
            filterType = arguments.getSerializable(ARG_FILTER_TYPE) as Entry.Type?
        }

        DownloaderActivity.SearchResults.addOnListChangedCallback(downloadsListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        DownloaderActivity.SearchResults.addOnListChangedCallback(downloadsListener)
    }

    fun updateItems() {
        promiseOnUi {
            if (filterType != null) {
                adapter?.setItems(DownloaderActivity.SearchResults.filter { it.type == filterType })
            } else {
                adapter?.setItems(DownloaderActivity.SearchResults)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.fragment_search_result_list, container, false) as RecyclerView

        adapter = EntryRecyclerViewAdapter()

        val context = view?.context
        val recyclerView = view
        if (mColumnCount <= 1) {
            recyclerView?.layoutManager = LinearLayoutManager(context)
        } else {
            recyclerView?.layoutManager = GridLayoutManager(context, mColumnCount)
        }
        recyclerView?.adapter = adapter

        adapter?.onClick { view, position ->
            adapter?.toggleSelection(position)
        }
        adapter?.onLongClick { view, position ->
            dragSelectTouchListener.startDragSelection(position)
            true
        }

        recyclerView?.addOnItemTouchListener(dragSelectTouchListener)


        return view
    }

    override fun onResume() {
        super.onResume()
        updateItems()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is SearchResultSelectionListener) {
            mListener = context as SearchResultSelectionListener?
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface SearchResultSelectionListener {
        fun selectionChanged()
    }

    val selectedItems get() = adapter?.selectedItems ?: ArrayList(0)

    fun clearSelection() {
        adapter?.deselectAll()
    }

    companion object {
        private val ARG_COLUMN_COUNT = "column-count"
        private val ARG_FILTER_TYPE = "filter-type"

        fun newInstance(filterType: Entry.Type? = null, columnCount: Int = 1): SearchResultListFragment {
            val fragment = SearchResultListFragment()
            val args = Bundle()
            args.putInt(ARG_COLUMN_COUNT, columnCount)
            args.putSerializable(ARG_FILTER_TYPE, filterType)
            fragment.arguments = args
            fragment.updateItems()
            return fragment
        }
    }
}
