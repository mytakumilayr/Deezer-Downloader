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

import android.Manifest
import android.app.Activity
import android.databinding.ObservableArrayList
import android.databinding.ObservableList
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener
import kotlinExtensions.sfl4j.error
import kotlinx.android.synthetic.main.activity_downloader.*
import kotlinx.android.synthetic.main.app_bar_downloader.*
import android.support.v7.widget.SearchView
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.promiseOnUi
import nl.komponents.kovenant.ui.successUi


class DownloaderActivity: AppCompatActivity(),
        NavigationView.OnNavigationItemSelectedListener,
        DownloadItemFragment.OnListFragmentInteractionListener,
        SearchResultListFragment.SearchResultSelectionListener,
        Search.Listener {

    val downloader = Downloader()
    val search = Search()

    fun showFab() {
        fab.show()
    }

    fun hideFab() {
        fab.hide()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Dexter
                .withActivity(this)
                .withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(DialogOnAnyDeniedMultiplePermissionsListener.Builder.withContext(this)
                        .withTitle("Grant Permissions")
                        .withMessage("Grant pls!")
                        .withButtonText(android.R.string.ok)
                        .withIcon(R.mipmap.ic_launcher)
                        .build())
                .check()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_downloader)

        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            (supportFragmentManager.findFragmentByTag("content_fragment") as? SearchResultsFragment)?.let { fragment ->
                task {
                    fragment.selectedItems
                            .map { Download(it)}
                            .apply { forEach { downloader.queueDownload(it) } }
                } success { downloads ->
                    runOnUiThread {
                        RunningDownloadsMutable.addAll(downloads)
                        fragment.clearSelection()
                        hideFab()
                        Snackbar.make(view, "Downloads added to queue", Snackbar.LENGTH_SHORT)
                                .setAction("Action", null).show()
                    }
                } fail {
                    error(it){ "Error starting downloads" }
                }

            }
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)

        search.addListener(this)

//        task {
//            val album = Deezer.get(AlbumId(10192016))
//            album.tracks.data
//                    .map { Download(Entry.Track(Deezer.get(it.id)))}
//                    .apply { forEach { downloader.queueDownload(it) } }
//        } success { downloads ->
//            runOnUiThread { RunningDownloadsMutable.addAll(downloads) }
//        } fail {
//            Log.e("DeezerDownloader", "error", it)
//        }

        navigate(R.id.nav_search)

        fab.hide()
    }

    override fun onDestroy() {
        super.onDestroy()
        search.removeListener(this)
    }

    private var searchIndicatorVisible: Boolean
        get() = findViewById(R.id.busy_indicator)?.let { it.visibility == VISIBLE } ?: false
        set(value) { findViewById(R.id.busy_indicator)?.let {
            promiseOnUi { it.visibility = if (value) VISIBLE else GONE }
        }}

    override fun searchStarted() {
        searchIndicatorVisible = true
    }

    override fun searchFinished(result: Search.SearchResult) {
        SearchResultsMutable.clear()
        SearchResultsMutable.addAll(result.tracks)
        SearchResultsMutable.addAll(result.albums)
        SearchResultsMutable.addAll(result.artists)
        SearchResultsMutable.addAll(result.playlists)

        searchIndicatorVisible = false
    }

    override fun searchFailed(e: Throwable) {
        searchIndicatorVisible = false
        error(e) { "Search failed" }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.downloader, menu)


        val searchItem = menu.findItem(R.id.action_search)
        val searchView = (searchItem.actionView as SearchView)
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                search.doSearch(query)
                val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                val window = findViewById(android.R.id.content).rootView.windowToken
                imm.hideSoftInputFromWindow(window, 0)
                searchView.onActionViewCollapsed()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isNotEmpty())
                    search.doSearch(newText)
                return true
            }

        })

        return true
    }

    private fun navigate(id: Int) {
        hideFab()
        supportFragmentManager.beginTransaction()
                .replace(R.id.content_view, when (id) {
                    R.id.nav_search -> SearchResultsFragment.newInstance()
                    R.id.nav_downloads -> DownloadItemFragment.newInstance()
                    R.id.nav_settings -> SettingsFragment.newInstance()
                    else -> { throw RuntimeException("Invalid navigation item") }
                }, "content_fragment").commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_search) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId

        navigate(id)

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onListFragmentInteraction(download: Download) {
    }

    override fun selectionChanged() {
        (supportFragmentManager.findFragmentByTag("content_fragment") as? SearchResultsFragment)?.let { fragment ->
            if (fragment.selectedItems.isEmpty())
                hideFab()
            else
                showFab()
        }
    }

    fun onListFragmentInteraction(entry: Entry) {
        task {
            Download(entry).apply { downloader.queueDownload(this) }
        } successUi {
            RunningDownloadsMutable.add(it)
        }
    }

    companion object {
        private val RunningDownloadsMutable = ObservableArrayList<Download>()
        val RunningDownloads: ObservableList<Download> get() = RunningDownloadsMutable

        private val SearchResultsMutable = ObservableArrayList<Entry>()
        val SearchResults: ObservableList<Entry> get() = SearchResultsMutable
    }
}
