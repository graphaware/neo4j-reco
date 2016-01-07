/*
 * Copyright (c) 2013-2016 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.reco.generic.context;

import com.graphaware.reco.generic.config.Config;
import com.graphaware.reco.generic.config.KeyValueConfig;
import com.graphaware.reco.generic.config.SimpleConfig;
import com.graphaware.reco.generic.filter.Filter;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Unit test for {@link FilteringContext}.
 */
public class FilteringContextTest {

    private List<Filter<String, String>> testFilters;
    private Set<String> testBlacklist;

    @Before
    public void setUp() {
        testFilters = new LinkedList<>();
        testFilters.add(new Filter<String, String>() {
            @Override
            public boolean include(String item, String input, Context<String, String> context) {
                return !"excluded".equals(item);
            }
        });

        testBlacklist = new HashSet<>();
        testBlacklist.add("blacklisted");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutIllegalConstruction() {
        new FilteringContext<>(null, new SimpleConfig(1, 2), testFilters, testBlacklist);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutIllegalConstruction2() {
        new FilteringContext<>("test", null, testFilters, testBlacklist);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutIllegalConstruction3() {
        new FilteringContext<>(new Object(), new SimpleConfig(1, 2), null, testBlacklist);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutIllegalConstruction4() {
        new FilteringContext<>("test", new SimpleConfig(1, 2), testFilters, null);
    }

    @Test
    public void shouldCorrectlyAllowItems() {
        Context<String, String> context = new FilteringContext<>("test", new SimpleConfig(1, 2), testFilters, testBlacklist);

        assertTrue(context.allow("allowed", "test"));
        assertFalse(context.allow("excluded", "test"));
        assertFalse(context.allow("blacklisted", "test"));

        assertTrue(context.allow("disallowed", "test"));
        context.disallow("disallowed");
        assertFalse(context.allow("disallowed", "test"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutAllowingIllegalItem() {
        new FilteringContext<>("test", new SimpleConfig(1, 2), testFilters, testBlacklist).allow(null, "test");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutAllowingIllegalItem2() {
        new FilteringContext<>("test", new SimpleConfig(1, 2), testFilters, testBlacklist).allow("bla", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutAllowingIllegalItem3() {
        new FilteringContext<>("test", new SimpleConfig(1, 2), testFilters, testBlacklist).allow("bla", null);
    }

    @Test
    public void shouldReturnConfig() {
        Context<?, ?> context = new FilteringContext<>("test", new SimpleConfig(1, 2), testFilters, testBlacklist);

        assertEquals(new SimpleConfig(1, 2), context.config());
        assertEquals(new SimpleConfig(1, 2), context.config(Config.class));
        assertEquals(new SimpleConfig(1, 2), context.config(SimpleConfig.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutWrongTypes() {
        Context<?, ?> context = new FilteringContext<>("test", new SimpleConfig(1, 2), testFilters, testBlacklist);

        context.config(KeyValueConfig.class);
    }

    @Test
    public void shouldCorrectlyJudgeTiming() throws InterruptedException {
        Context<?, ?> context = new FilteringContext<>("test", new SimpleConfig(1, 10), testFilters, testBlacklist);

        assertTrue(context.timeLeft());

        Thread.sleep(11);

        assertFalse(context.timeLeft());
    }
}
