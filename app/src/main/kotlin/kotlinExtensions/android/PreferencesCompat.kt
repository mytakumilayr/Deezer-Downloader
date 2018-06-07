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

package kotlinExtensions.android

import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.preference.PreferenceManager


private fun <T: Preference> findPref(manager: PreferenceManager, key: String): T {
    val pref = manager.findPreference(key)
            ?: throw RuntimeException("Preference with key: $key not found")

    @Suppress("UNCHECKED_CAST")
    val typedPref = (pref as? T)
            ?: throw RuntimeException("Preference has incompatible type ${pref::class.java.simpleName}")

    return typedPref
}

fun <T: Preference> PreferenceManager.lazyPref(key: String) = lazy{ findPref<T>(this, key) }
fun <T: Preference> PreferenceFragmentCompat.lazyPref(key: String) = lazy{ findPref<T>(preferenceManager, key) }

