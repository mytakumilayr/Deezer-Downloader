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

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.support.v7.preference.ListPreference
import android.support.v7.preference.Preference
import com.nononsenseapps.filepicker.FilePickerActivity
import com.takisoft.fix.support.v7.preference.EditTextPreference
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat
import com.takisoft.fix.support.v7.preference.SwitchPreferenceCompat
import de.bigboot.deezerdownloader.i18n.CoreStrings
import kotlinExtensions.android.lazyPref
import android.R.attr.data
import android.app.Activity
import android.net.Uri


class SettingsFragment: PreferenceFragmentCompat() {
    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_general)

        setupPreferences()
    }

    val config get() = DownloaderApplication.config

    val prefMaxQuality: ListPreference by lazyPref("pref_max_quality")
    val prefCoverSize: ListPreference by lazyPref("pref_cover_size")
    val prefCoverType: ListPreference by lazyPref("pref_cover_type")
    val prefOutputFolder: Preference by lazyPref("pref_output_folder")
    val prefExistingFileBehaviour: ListPreference by lazyPref("pref_existing_file_behaviour")
    val prefEmbedGenres: SwitchPreferenceCompat by lazyPref("pref_embed_genres")
    val prefGenreSeparator: EditTextPreference by lazyPref("pref_genre_separator")
    val prefFilenameTracks: EditTextPreference by lazyPref("pref_filename_tracks")
    val prefFilenameAlbums: EditTextPreference by lazyPref("pref_filename_albums")
    val prefFilenamePlaylists: EditTextPreference by lazyPref("pref_filename_playlists")
    val prefFilenameArtists: EditTextPreference by lazyPref("pref_filename_artists")
    val prefAdvancedSettings: Preference by lazyPref("pref_advanced_settings")


    fun setupPreferences() {
        prefMaxQuality.entries = arrayOf("128", "256", "320")
        prefMaxQuality.entryValues = StreamQuality.values().map { it.toString() }.toTypedArray()
        prefMaxQuality.onChange<String> { config.maxDownloadQuality = StreamQuality.valueOf(it) }
        prefMaxQuality.value = config.maxDownloadQuality.name
        prefMaxQuality.summary = prefMaxQuality.entry

        prefCoverSize.entries = arrayOf(
                CoreStrings.settings__size_small(),
                CoreStrings.settings__size_medium(),
                CoreStrings.settings__size_big(),
                CoreStrings.settings__size_xl()
        )
        prefCoverSize.entryValues = Config.ImageSize.values()
                .filterNot { it == Config.ImageSize.Custom }
                .map { it.name }
                .toTypedArray()
        prefCoverSize.onChange<String> { config.imageSize = Config.ImageSize.valueOf(it) }
        prefCoverSize.value = config.imageSize.name
        prefCoverSize.summary = prefCoverSize.entry

        val availableCoverTypes = Config.CoverType.values().filter { it.text != null }
        prefCoverType.entries = availableCoverTypes.map { it.text!!() }.toTypedArray()
        prefCoverType.entryValues = availableCoverTypes.map { it.name }.toTypedArray()
        prefCoverType.onChange<String> { config.coverType = Config.CoverType.valueOf(it) }
        prefCoverType.value = config.coverType.name
        prefCoverType.summary = prefCoverType.entry

        prefExistingFileBehaviour.entries = Config.ExistingFileBehaviour.values().map { it.text() }.toTypedArray()
        prefExistingFileBehaviour.entryValues = Config.ExistingFileBehaviour.values().map { it.name }.toTypedArray()
        prefExistingFileBehaviour.onChange<String> { config.existingFileBehaviour = Config.ExistingFileBehaviour.valueOf(it)}
        prefExistingFileBehaviour.value = config.existingFileBehaviour.name
        prefExistingFileBehaviour.summary = prefExistingFileBehaviour.entry

        prefOutputFolder.summary = config.outputFolder.canonicalPath
        prefOutputFolder.setOnPreferenceClickListener {
            val i = Intent(context, FilePickerActivity::class.java).apply {
                putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)
                putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true)
                putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR)
                putExtra(FilePickerActivity.EXTRA_START_PATH, config.outputFolder.canonicalPath)
            }
            startActivityForResult(i, OUTPUT_DIR_CODE)
            true
        }

        prefEmbedGenres.setOnPreferenceChangeListener { pref, value ->
            config.embedGenres = value as Boolean
            true
        }
        prefEmbedGenres.isChecked = config.embedGenres

        prefGenreSeparator.onChange<String> { config.genreSeparator = it }
        prefGenreSeparator.summary = config.genreSeparator

        prefFilenameTracks.onChange<String> { config.trackTemplate = it }
        prefFilenameTracks.summary = config.trackTemplate

        prefFilenameAlbums.onChange<String> { config.albumTemplate = it }
        prefFilenameAlbums.summary = config.albumTemplate

        prefFilenamePlaylists.onChange<String> { config.playlistTemplate = it }
        prefFilenamePlaylists.summary = config.playlistTemplate

        prefFilenameArtists.onChange<String> { config.artistTemplate = it }
        prefFilenameArtists.summary = config.artistTemplate
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == OUTPUT_DIR_CODE && resultCode == Activity.RESULT_OK && intent != null) {
            if (intent.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
                // The URI will now be something like content://PACKAGE-NAME/root/path/to/file
                val uri = intent.data
                // A utility method is provided to transform the URI to a File object
                val file = com.nononsenseapps.filepicker.Utils.getFileForUri(uri)
                config.outputFolder = file
                prefOutputFolder.summary = config.outputFolder.canonicalPath
            }
        }
    }

    companion object {
        val OUTPUT_DIR_CODE = 0x42

        fun newInstance(): SettingsFragment {
            val fragment = SettingsFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    private fun <T> Preference.onChange(listener: (value: T)->Unit) {
        this.setOnPreferenceChangeListener { pref, value ->
            @Suppress("UNCHECKED_CAST")
            listener.invoke(value as T)
            pref.summary = value.toString()
            true
        }
    }

    private fun <T: CharSequence> ListPreference.onChange(listener: (value: T)->Unit) {
        this.setOnPreferenceChangeListener { pref, value ->
            @Suppress("UNCHECKED_CAST")
            listener.invoke(value as T)
            pref.summary = this.entries[this.entryValues.indexOf(value)]
            true
        }
    }
}
