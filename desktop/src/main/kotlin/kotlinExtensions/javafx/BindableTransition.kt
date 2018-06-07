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

package kotlinExtensions.javafx

import javafx.animation.Transition
import javafx.beans.property.SimpleDoubleProperty
import javafx.util.Duration

class BindableTransition(duration: Duration): Transition() {
    val fractionProperty = SimpleDoubleProperty()
    val targetProperty = SimpleDoubleProperty(1.0)
    val startProperty = SimpleDoubleProperty(0.0)
    private val deltaProperty = SimpleDoubleProperty().apply { bind(targetProperty.subtract(startProperty)) }

    init{
        cycleDuration = duration
    }

    override fun interpolate(frac: Double) {
        fractionProperty.set(startProperty.value + deltaProperty.value*frac)
    }
}