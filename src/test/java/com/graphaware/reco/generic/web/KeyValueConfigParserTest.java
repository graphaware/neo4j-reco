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

package com.graphaware.reco.generic.web;

import com.graphaware.reco.generic.config.KeyValueConfig;
import com.graphaware.reco.generic.config.MapBasedConfig;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit test for {@link KeyValueConfigParser}.
 */
public class KeyValueConfigParserTest {

    @Test
    public void shouldProduceConfig() {
        Map<String, Object> expected = new HashMap<>();
        expected.put("test1", "value1");
        expected.put("test2", "value2");

        KeyValueConfig actual = new KeyValueConfigParser(":").produceConfig(1, 2, "test1:value1:test2:value2");
        assertEquals(new MapBasedConfig(1, 2, expected), actual);
        assertEquals(new MapBasedConfig(1, 2, new HashMap<String, Object>()), new KeyValueConfigParser(":").produceConfig(1, 2, ""));
        assertEquals(new MapBasedConfig(1, Long.MAX_VALUE, new HashMap<String, Object>()), new KeyValueConfigParser(":").produceConfig(1, ""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutIllegalValues() {
        new KeyValueConfigParser(":").produceConfig(1, 2, "test1:value1:test2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutIllegalValues2() {
        new KeyValueConfigParser(":").produceConfig(1, 2, "test1");
    }
}
