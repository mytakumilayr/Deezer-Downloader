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

import javafx.fxml.FXMLLoader
import javafx.fxml.JavaFXBuilderFactory
import javafx.util.BuilderFactory
import javafx.util.Callback
import java.io.InputStream
import java.net.URL
import java.nio.charset.Charset
import java.util.ArrayList
import java.util.LinkedList
import java.util.ResourceBundle

 class KotlinFXMLLoader constructor(location: URL? = null, resources: ResourceBundle? = null,
                                     builderFactory: BuilderFactory = JavaFXBuilderFactory(),
                                     controllerFactory: Callback<Class<*>, Any>? = null,
                                     charset: Charset = Charset.forName("UTF-8"),
                                     loaders: LinkedList<FXMLLoader> = LinkedList<FXMLLoader>())
: FXMLLoader(location, resources, builderFactory, controllerFactory, charset, loaders) {

    override fun <T> load(): T {
        @Suppress("DEPRECATION")
        impl_setLoadListener(object : AbstractLoadListener() {

            var counter = 0

            override fun beginInstanceDeclarationElement(type: Class<*>?) {
                counter++
            }

            override fun beginIncludeElement() {
                counter++
            }

            override fun beginRootElement() {
                counter++
            }

            override fun beginDefineElement() {
                counter++
            }

            override fun beginCopyElement() {
                counter++
            }

            override fun beginPropertyElement(name: String?, sourceType: Class<*>?) {
                counter++
            }

            override fun beginScriptElement() {
                counter++
            }

            override fun beginReferenceElement() {
                counter++
            }

            override fun beginUnknownStaticPropertyElement(name: String?) {
                counter++
            }

            override fun beginUnknownTypeElement(name: String?) {
                counter++
            }

            override fun endElement(value: Any?) {
                if (getController<Any>() == null)
                    return
                
                //If created element has 'getId' method try to inject to bindFXML or bindOptionalFXML field delegate
                try {
                    if (value == null) throw NoSuchMethodException()
                    val valueId: String = value::class.java.getMethod("getId").invoke(value) as String?
                            ?: throw NoSuchMethodException()
                    val controller: Any = getController()
                    try {
                        val field = controller::class.java.getDeclaredField("$valueId\$delegate")
                        if (field.type == bindFXML::class.java || field.type == bindOptionalFXML::class.java) {
                            field.isAccessible = true
                            val delegate: Any = field.get(controller)
                            val valueField = delegate::class.java.getDeclaredField("value")
                            valueField.isAccessible = true
                            val fieldType = controller::class.java.findMethod("get${valueId.capitalize()}").returnType
                            //Check if field type and created object have compatible types
                            if (!fieldType.isInstance(value)) {
                                throw IllegalArgumentException("Class '$valueId' type [${value::class.java.name}] is is not subtype of [${fieldType.name}] ")
                            }
                            valueField.set(delegate, value)
                            valueField.isAccessible = false
                            field.isAccessible = false
                        }
                    } catch (e: NoSuchFieldException) {
                        println("No field for element $valueId")
                    }
                } catch (e: NoSuchMethodException) {
                }
                counter--

                //When counter is 0 the root tag has been closed.
                //Now is good time to validate if all fields with bindFXML are set
                //Also inject the property bundle
                if (counter == 0) {
                    val controller: Any = getController()

                    try {
                        val valueId = "bundle"
                        val field = controller::class.java.getDeclaredField("$valueId\$delegate")
                        if (field.type == bindFXML::class.java || field.type == bindOptionalFXML::class.java) {
                            field.isAccessible = true
                            val delegate: Any = field.get(controller)
                            val valueField = delegate::class.java.getDeclaredField("value")
                            valueField.isAccessible = true
                            val fieldType = controller::class.java.findMethod("get${valueId.capitalize()}").returnType
                            //Check if field type and created object have compatible types
                            if (!fieldType.isAssignableFrom(ResourceBundle::class.java)) {
                                throw IllegalArgumentException("[${ResourceBundle::class.java.name}] is is not assignable to [${fieldType.name}] ")
                            }
                            valueField.set(delegate, resources)
                            valueField.isAccessible = false
                            field.isAccessible = false
                        }
                    } catch (e: NoSuchFieldException) {
                        // No bundle field, so we don't inject the bundle
                    }

                    val invalidFields = controller::class.java.declaredFields.filter {
                        if (bindFXML::class.java != it.type) {
                            return@filter false
                        }
                        try {
                            it.isAccessible = true
                            val valueField = it.type.getDeclaredField("value")
                            valueField.isAccessible = true
                            if (valueField.get(it.get(controller)) == null) {
                                return@filter true
                            }
                        } finally {
                            it.isAccessible = false
                        }
                        return@filter false
                    }.map { it.name.substringBefore("$") }.toCollection(ArrayList<String>())
                    if (invalidFields.isNotEmpty()) {
                        throw IllegalStateException("Those field $invalidFields has not been injected. It they are optional use bindOptionalFXML()")
                    }


                }
            }
        })
        return super.load()
    }

    override fun <T> load(inputStream: InputStream?): T? {
        throw UnsupportedOperationException("This operation is not implemented yet. Use load() method instead.")
    }
}
