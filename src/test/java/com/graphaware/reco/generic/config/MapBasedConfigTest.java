/*
 * Copyright (c) 2015 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.reco.generic.config;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Unit test for {@link MapBasedConfig}.
 */
public class MapBasedConfigTest {

    @Test
    public void shouldReturnCorrectValues() {
        KeyValueConfig config = new MapBasedConfig(10, 20, Collections.<String, Object>singletonMap("testKey", "testValue"));

        assertEquals(10, config.limit());
        assertEquals(20, config.maxTime());
        assertTrue(config.contains("testKey"));
        assertFalse(config.contains("non-existing"));
        assertEquals("testValue", config.get("testKey"));
        assertEquals("testValue", config.get("testKey", "unknown"));
        assertEquals("testValue", config.get("testKey", String.class));
        assertEquals("testValue", config.get("testKey", "unknown", String.class));
        assertEquals("unknown", config.get("non-existing", "unknown"));
        assertEquals("unknown", config.get("non-existing", "unknown", String.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutIncorrectTypes() {
        KeyValueConfig config = new MapBasedConfig(10, Collections.<String, Object>singletonMap("testKey", "testValue"));

        config.get("testKey", Integer.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutIncorrectTypes2() {
        KeyValueConfig config = new MapBasedConfig(10, Collections.<String, Object>singletonMap("testKey", "testValue"));

        config.get("testKey", 1, Integer.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutNonExistingKey() {
        KeyValueConfig config = new MapBasedConfig(10, 20, Collections.<String, Object>singletonMap("testKey", "testValue"));

        config.get("non-existing");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutNonExistingKey2() {
        KeyValueConfig config = new MapBasedConfig(10, 20, Collections.<String, Object>singletonMap("testKey", "testValue"));

        config.get("non-existing", String.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutWrongConstruction() {
        new MapBasedConfig(-1, 20, Collections.<String, Object>singletonMap("testKey", "testValue"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutWrongConstruction2() {
        new MapBasedConfig(10, -1, Collections.<String, Object>singletonMap("testKey", "testValue"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutWrongConstruction3() {
        new MapBasedConfig(10, 20, null);
    }
}
