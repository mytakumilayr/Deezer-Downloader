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

import com.sun.javafx.fxml.LoadListener

open class AbstractLoadListener : LoadListener {
    override fun readLanguageProcessingInstruction(language: String?) {
    }

    override fun beginUnknownTypeElement(name: String?) {
    }

    override fun beginScriptElement() {
    }

    override fun beginUnknownStaticPropertyElement(name: String?) {
    }

    override fun beginReferenceElement() {
    }

    override fun readInternalAttribute(name: String?, value: String?) {
    }

    override fun readComment(comment: String?) {
    }

    override fun readPropertyAttribute(name: String?, sourceType: Class<*>?, value: String?) {
    }

    override fun endElement(value: Any?) {
    }

    override fun beginInstanceDeclarationElement(type: Class<*>?) {
    }

    override fun readEventHandlerAttribute(name: String?, value: String?) {
    }

    override fun beginIncludeElement() {
    }

    override fun beginRootElement() {
    }

    override fun readImportProcessingInstruction(target: String?) {
    }

    override fun beginPropertyElement(name: String?, sourceType: Class<*>?) {
    }

    override fun readUnknownStaticPropertyAttribute(name: String?, value: String?) {
    }

    override fun beginDefineElement() {
    }

    override fun beginCopyElement() {
    }

}