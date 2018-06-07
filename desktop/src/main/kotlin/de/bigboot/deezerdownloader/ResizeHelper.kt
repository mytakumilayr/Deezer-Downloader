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

import javafx.event.EventHandler
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.input.MouseEvent
import javafx.stage.Stage

object ResizeHelper {
    fun addResizeListener(stage: Stage, config: (ResizeListener.()->Unit)? = null) {
        val resizeListener = ResizeListener(stage)
        stage.scene.addEventHandler(MouseEvent.MOUSE_MOVED, resizeListener)
        stage.scene.addEventHandler(MouseEvent.MOUSE_PRESSED, resizeListener)
        stage.scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, resizeListener)
        stage.scene.addEventHandler(MouseEvent.MOUSE_EXITED, resizeListener)
        stage.scene.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, resizeListener)

        for (child in stage.scene.root.childrenUnmodifiable) {
            addListenerRecursive(child, resizeListener)
        }

        config?.invoke(resizeListener)
    }

    fun addListenerRecursive(node: Node, listener: EventHandler<MouseEvent>) {
        node.addEventHandler(MouseEvent.MOUSE_MOVED, listener)
        node.addEventHandler(MouseEvent.MOUSE_PRESSED, listener)
        node.addEventHandler(MouseEvent.MOUSE_DRAGGED, listener)
        node.addEventHandler(MouseEvent.MOUSE_EXITED, listener)
        node.addEventHandler(MouseEvent.MOUSE_EXITED_TARGET, listener)
        if (node is Parent) {
            val children = node.childrenUnmodifiable
            for (child in children) {
                addListenerRecursive(child, listener)
            }
        }
    }

    class ResizeListener(private val stage: Stage) : EventHandler<MouseEvent> {
        var border = 0
        var grip = 8
        var minWidth = 0
        var minHeight = 0
        private var startX = 0.0
        private var cursorEvent = Cursor.DEFAULT
        private var startY = 0.0


        override fun handle(mouseEvent: MouseEvent) {
            val mouseEventType = mouseEvent.eventType
            val scene = stage.scene

            val mouseEventX = mouseEvent.sceneX
            val mouseEventY = mouseEvent.sceneY
            val sceneWidth = scene.width
            val sceneHeight = scene.height

            if (MouseEvent.MOUSE_MOVED == mouseEventType) {
                if (mouseEventX >= sceneWidth - border - grip && mouseEventY >= sceneHeight - border - grip &&
                    mouseEventX <= sceneWidth - border        && mouseEventY <= sceneHeight - border ) {
                    cursorEvent = Cursor.SE_RESIZE
                } else {
                    cursorEvent = Cursor.DEFAULT
                }
                scene.cursor = cursorEvent
            } else if (MouseEvent.MOUSE_EXITED == mouseEventType || MouseEvent.MOUSE_EXITED_TARGET == mouseEventType) {
                scene.cursor = Cursor.DEFAULT
            } else if (MouseEvent.MOUSE_PRESSED == mouseEventType) {
                startX = stage.width - mouseEventX
                startY = stage.height - mouseEventY
            } else if (MouseEvent.MOUSE_DRAGGED == mouseEventType) {
                if (Cursor.DEFAULT != cursorEvent) {

                    val minHeight = minHeight + border
                    if (stage.height > minHeight || mouseEventY + startY - stage.height > 0) {
                        stage.height = mouseEventY + startY
                    }

                    val minWidth = minWidth + border
                    if (stage.width > minWidth || mouseEventX + startX - stage.width > 0) {
                        stage.width = mouseEventX + startX
                    }
                }
            }
        }
    }
}