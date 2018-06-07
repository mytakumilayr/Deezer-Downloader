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

import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent

fun Node.enableDragging() {
    DragHandler(this)
}

internal class DragHandler(val node: Node) {
    private var mouseOffsetX = 0.0
    private var mouseOffsetY = 0.0

    private val mouseDraggedHandler = EventHandler<MouseEvent> { me ->
        if (me.button == MouseButton.PRIMARY) {
            node.scene.window.x = me.screenX - mouseOffsetX
            node.scene.window.y = me.screenY - mouseOffsetY
        }
    }

    private val mousePressedHandler = EventHandler<MouseEvent> { me ->
        if (me.button == MouseButton.PRIMARY) {
            mouseOffsetX = me.screenX - node.scene.window.x
            mouseOffsetY = me.screenY - node.scene.window.y
        }
    }

    init {
        node.onMousePressed = mousePressedHandler
        node.onMouseDragged = mouseDraggedHandler
    }
}