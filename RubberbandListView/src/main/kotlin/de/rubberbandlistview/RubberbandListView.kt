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

package de.rubberbandlistview

import com.sun.javafx.scene.control.skin.VirtualFlow
import javafx.collections.ObservableList
import javafx.scene.control.IndexedCell
import javafx.scene.control.ListView
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import javafx.scene.Group

class RubberbandListView<T>(items: ObservableList<T>? = null) : ListView<T>(items) {
    val virtualFlow: VirtualFlow<IndexedCell<T>>?
        @Suppress("UNCHECKED_CAST")
        get() = children.find { it is VirtualFlow<*> } as? VirtualFlow<IndexedCell<T>>
    val clippedContainer
        get() = virtualFlow?.childrenUnmodifiable?.find { it.styleClass.contains("clipped-container") } as? Region?
    val sheet
        get() = virtualFlow?.childrenUnmodifiable?.find { it is Group } as? Group?

    val scroll get() = virtualFlow?.position ?: 0.0

    init {
        var dragStart = 0.0
        val rectangle = javafx.scene.shape.Rectangle().apply {
            width = this.width
            height = 0.0
            x = 0.0
            y = 0.0
            fill = Color.BLUE.apply { setOpacity(0.5) }
        }

        this.setOnMousePressed { event ->
            rectangle.width = this.width

            children.add(rectangle)

            dragStart = event.y + scroll
            rectangle.y = dragStart

            rectangle.height = 0.0

            rectangle.toFront()
        }

        this.setOnMouseDragged { event ->
            if (rectangle.parent == this) {
                val delta = event.y - dragStart
                if (Math.abs(delta) < 5)
                    return@setOnMouseDragged

                if (delta >= 0) {
                    rectangle.y = dragStart
                    rectangle.height = delta
                } else {
                    rectangle.y = dragStart + delta
                    rectangle.height = - delta
                }

                if (rectangle.height + rectangle.y > height) {
                    rectangle.height = height - rectangle.y - 1
                }

                val bounds = rectangle.localToScene(rectangle.boundsInLocal)
                virtualFlow?.let { virtualFlow ->
                    for (i in 0 until virtualFlow.cellCount) {
                        val cell = virtualFlow.getCell(i)
                        val cellBounds = cell.localToScene(cell.boundsInLocal)
                        if (cellBounds.intersects(bounds)) {
                            selectionModel.select(cell.index)
                        }
                    }
                }
            }
        }

        this.setOnMouseReleased {
            children.remove(rectangle)
        }
    }
}