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

import javafx.animation.Animation
import javafx.beans.binding.DoubleBinding
import javafx.beans.property.DoubleProperty
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.util.Duration

class SmoothDoubleBinding(private val target: DoubleProperty): DoubleBinding() {
    private val transition = BindableTransition(Duration(500.0))

    private val targetListener = ChangeListener<kotlin.Number> { _, oldValue, newValue ->
            transition.startProperty.value = oldValue.toDouble()
            transition.targetProperty.value = newValue.toDouble()
            transition.playFromStart()
    }

    init {
        target.addListener(targetListener)
        super.bind(transition.fractionProperty, target)
        transition.startProperty.value = target.value
        transition.targetProperty.value = target.value
        transition.fractionProperty.value = target.value
        invalidate()
    }

    override fun computeValue(): Double {
        return transition.fractionProperty.value
    }

    override fun getDependencies(): ObservableList<*>?
            = FXCollections.observableList(listOf(target, transition.fractionProperty))

    override fun dispose() {
        super.unbind(transition.fractionProperty, target)
        target.removeListener(targetListener)
    }
}