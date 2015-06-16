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

package com.graphaware.reco.generic.context;

import com.graphaware.reco.generic.config.Config;
import com.graphaware.reco.generic.config.KeyValueConfig;
import com.graphaware.reco.generic.config.SimpleConfig;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for {@link SimpleContext}.
 */
public class SimpleContextTest {

    @Test
    public void shouldReturnConfig() {
        Context<?, ?> context = new SimpleContext<>(new Object(), new SimpleConfig(1, 2));

        assertEquals(new SimpleConfig(1, 2), context.config());
        assertEquals(new SimpleConfig(1, 2), context.config(Config.class));
        assertEquals(new SimpleConfig(1, 2), context.config(SimpleConfig.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldComplainAboutWrongTypes() {
        Context<?, ?> context = new SimpleContext<>(new Object(), new SimpleConfig(1, 2));

        context.config(KeyValueConfig.class);
    }
}
