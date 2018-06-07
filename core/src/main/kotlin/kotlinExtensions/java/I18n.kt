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

package kotlinExtensions.java

import java.text.MessageFormat
import java.util.*

/**
 * Utility storage for global [ResourceBundle] object.
 * @author MJ
 */
object I18n {
    private val listeners = ArrayList<(ResourceBundle?) -> Unit>(4)
    /**
     * Used as the default application's [ResourceBundle] instance by [nls] methods. Has to be set explicitly.
     */
    var defaultBundle: ResourceBundle? = null
        set(value) {
            field = value
            for (listener in listeners) {
                listener(value)
            }
        }

    /**
     * @param listener will be invoked each time the [ResourceBundle] is reloaded.
     * @see removeListener
     */
    fun addListener(listener: (ResourceBundle?) -> Unit) {
        listeners.add(listener)
    }

    /**
     * @param listener will no longer be invoked each time the default bundle is changed.
     */
    fun removeListener(listener: (ResourceBundle?) -> Unit) {
        listeners.remove(listener)
    }

    /**
     * Removes all bundle reload listeners.
     */
    fun clearListeners() {
        listeners.clear()
    }
}

/**
 * @param key property name in the i18n bundle.
 * @param args will replace the argument placeholders in the selected bundle line. The order is preserved and honored.
 * @return formatted value mapped to the key extracted from the bundle.
 */
private fun ResourceBundle.format(key: String, vararg args: Any?): String {
    return MessageFormat(getString(key)).format(args)
}

/**
 * @param key property name in the i18n bundle.
 * @param args will replace the argument placeholders in the selected bundle line. The order is preserved and honored.
 * @return formatted value mapped to the key extracted from the bundle.
 */
operator fun ResourceBundle.get(key: String, vararg args: Any?): String = this.format(key, *args)

/**
 * @param key property name in the i18n bundle.
 * @param bundle i18n bundle which must contain the key. Defaults to bundle stored in [I18n].
 * @return value mapped to the key extracted from the bundle.
 */
fun nls(key: String, bundle: ResourceBundle = I18n.defaultBundle!!): String = bundle[key]

/**
 * @param key property name in the i18n bundle.
 * @param args will replace the argument placeholders in the selected bundle line. The order is preserved and honored.
 * @param bundle i18n bundle which must contain the key. Defaults to bundle stored in [I18n].
 * @return formatted value mapped to the key extracted from the bundle.
 */
fun nls(key: String, vararg args: Any?, bundle: ResourceBundle = I18n.defaultBundle!!): String = bundle.format(key, *args)

/**
 * Adds default functions to an object, turning it into a [ResourceBundle] line data container. Expects that its [toString]
 * method returns a valid bundle line ID. Advised to be implemented by an enum for extra utility.
 * @author MJ
 */
interface BundleLine {
    /**
     * [ResourceBundle] instance storing the localized line.
     */
    val bundle: ResourceBundle
        get() = I18n.defaultBundle!!

    val resId: String
        get() = toString().replace("__", ".")

    /**
     * @return value mapped to the selected [ResourceBundle] line ID without formatting.
     */
    operator fun invoke(): String = bundle[resId]

    /**
     * @param args will be used to format the line.
     * @return value mapped to the selected [ResourceBundle] line ID formatted with the passed args.
     */
    operator fun invoke(vararg args: Any?): String = bundle.format(resId, *args)
}

/**
 * @param line value mapped to its ID will be extracted. Its [toString] method should match property name in i18n bundle.
 * @return localized text from the selected bundle.
 */
fun nls(line: BundleLine): String = line()

/**
 * @param line value mapped to its ID will be extracted. Its [toString] method should match property name in i18n bundle.
 * @param args will be used to format the bundle line.
 * @return formatted value mapped to the key extracted from the bundle.
 */
fun nls(line: BundleLine, vararg args: Any?): String = line(*args)

/**
 * @param line value mapped to its ID will be extracted. Its [toString] method should match property name in i18n bundle.
 * @return localized text from the selected bundle.
 */
operator fun ResourceBundle.get(line: BundleLine): String = this[line.toString()]

/**
 * @param line value mapped to its ID will be extracted. Its [toString] method should match property name in i18n bundle.
 * @param args will replace the argument placeholders in the selected bundle line. The order is preserved and honored.
 * @return formatted value mapped to the key extracted from the bundle.
 */
operator fun ResourceBundle.get(line: BundleLine, vararg args: Any?): String = this.format(line.toString(), *args)