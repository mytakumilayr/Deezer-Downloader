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

package eu.stosdev

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class bindFXML<T : Any> : ReadOnlyProperty<Any?, T> {
    private var value: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val v = value ?: throw IllegalStateException("Node was not properly injected")
        return v
    }
}

class bindOptionalFXML<T : Any> : ReadOnlyProperty<Any?, T?> {
    private var value: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return value
    }
}
