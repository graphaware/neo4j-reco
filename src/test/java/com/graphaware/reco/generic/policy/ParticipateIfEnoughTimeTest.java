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

package com.graphaware.reco.generic.policy;

import com.graphaware.reco.generic.config.SimpleConfig;
import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.context.SimpleContext;
import com.graphaware.reco.generic.result.Recommendations;
import org.junit.Test;

import static com.graphaware.reco.generic.policy.ParticipationPolicy.IF_ENOUGH_TIME;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ParticipateIfEnoughTimeTest {

    @Test
    public void shouldParticipateIfThereIsEnoughTime() throws InterruptedException {
        Object input = new Object();
        Context<Object, Object> context = new SimpleContext<>(input, new SimpleConfig(10, 100));

        Thread.sleep(50);

        assertTrue(IF_ENOUGH_TIME.participate(input, context, new Recommendations<>()));

    }

    @Test
    public void shouldNotParticipateIfThereIsNotEnoughTime() throws InterruptedException {
        Object input = new Object();
        Context<Object, Object> context = new SimpleContext<>(input, new SimpleConfig(10, 100));

        Thread.sleep(101);

        assertFalse(IF_ENOUGH_TIME.participate(input, context, new Recommendations<>()));
    }
}
