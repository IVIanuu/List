/*
 * Copyright 2018 Manuel Wrage
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ivianuu.list

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ItemPropertiesTest {

    private val properties = ItemProperties()

    @Test
    fun testSetGetProperties() {
        assertNull(properties.getProperty<String>("key"))
        properties.setProperty("key", "value")
        assertEquals("value", properties.getProperty<String>("key"))
    }

    @Test
    fun testEquals() {
        val otherProperties = ItemProperties()
        properties.setProperty("key", false)
        otherProperties.setProperty("key", true)
        assertNotEquals(properties, otherProperties)

        otherProperties.setProperty("key", false)
        assertEquals(properties, otherProperties)
    }

    @Test
    fun testInitializeValuesOnItemAdded() {
        var initialized = false
        ItemPropertyDelegate(properties, "key") {
            initialized = true
            "value"
        }

        properties.itemAdded()

        assertTrue(initialized)
    }

    @Test
    fun testAccessUninitializedValueBeforeItemAdded() {
        ItemPropertyDelegate(properties, "key") { "value" }
        assertEquals("value", properties.getProperty<String>("key"))
    }

    @Test
    fun testDoNotAllowMutationAfterItemBeingAdded() {
        properties.itemAdded()
        val throwed = try {
            properties.setProperty("key", "value")
            false
        } catch (e: Exception) {
            true
        }

        assertTrue(throwed)
    }

    @Test
    fun testExcludeDoNotHashFromEquals() {
        properties.setProperty("key", "value")
        val otherProperties = ItemProperties()
        otherProperties.setProperty("key", "value")
        otherProperties.setProperty("ignored", "ignored", false)
        assertEquals(properties, otherProperties)
    }

}