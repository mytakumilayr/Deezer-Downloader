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

import javafx.scene.control.ListCell
import javafx.scene.layout.FlowPane
import javafx.scene.text.Text

class LogStringCell : ListCell<String>() {
    override fun updateItem(string: String?, empty: Boolean) {
        super.updateItem(string, empty)
        if (string != null && !isEmpty) {
            graphic = createAssembledFlowPane(string)
        } else {
            graphic = null
            text = null
        }
    }

    /* Erzeuge ein FlowPane mit gefüllten Textbausteien */
    private fun createAssembledFlowPane(vararg messageTokens: String): FlowPane {
        val flow = FlowPane()
        for (token in messageTokens) {
            val text = Text(token)

            when {
                text.toString().contains(" TRACE ") -> text.style = "-fx-fill: #0000FF"
                text.toString().contains(" ALL ") -> text.style = "-fx-fill: #FF00FF"
                text.toString().contains(" ERROR ") -> text.style = "-fx-fill: #FF8080"
                text.toString().contains(" INFO ") -> text.style = "-fx-fill: #000000"
                text.toString().contains(" FATAL ") -> text.style = "-fx-fill: #FF0000"
                text.toString().contains(" DEBUG ") -> text.style = "-fx-fill: #808080"
                text.toString().contains(" OFF ") -> text.style = "-fx-fill: #8040FF"
                text.toString().contains(" WARN ") -> text.style = "-fx-fill: #FF8000"
            }

            flow.children.add(text)
        }
        return flow
    }
}