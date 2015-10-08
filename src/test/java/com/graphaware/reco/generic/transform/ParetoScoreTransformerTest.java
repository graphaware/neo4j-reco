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

package com.graphaware.reco.generic.transform;

import com.graphaware.reco.generic.result.PartialScore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link ParetoScoreTransformer}.
 */
public class ParetoScoreTransformerTest {

    @Test
    public void verifyTransform() {
        ParetoScoreTransformer<?> transformer = ParetoScoreTransformer.create(100, 10);

        assertEquals(0, transformer.transform(null, new PartialScore(0), null).getValue(), 0.5);
        assertEquals(15, transformer.transform(null, new PartialScore(1), null).getValue(), 0.5);
        assertEquals(28, transformer.transform(null, new PartialScore(2), null).getValue(), 0.5);
        assertEquals(38, transformer.transform(null, new PartialScore(3), null).getValue(), 0.5);
        assertEquals(55, transformer.transform(null, new PartialScore(5), null).getValue(), 0.5);
        assertEquals(80, transformer.transform(null, new PartialScore(10), null).getValue(), 0.5);
        assertEquals(96, transformer.transform(null, new PartialScore(20), null).getValue(), 0.5);
        assertEquals(100, transformer.transform(null, new PartialScore(50), null).getValue(), 0.5);
        assertEquals(100, transformer.transform(null, new PartialScore(100), null).getValue(), 0.5);
        assertEquals(100, transformer.transform(null, new PartialScore(10000), null).getValue(), 0.5);

        assertEquals(0, ParetoScoreTransformer.create(100, 10, 10).transform(null, new PartialScore(9), null).getValue(), 0.5);

        PartialScore transformed = transformer.transform(null, new PartialScore(9), null);
        assertEquals("{value:76.50762, {value:67.50762, ParetoTransformationOf:9.0}}", transformed.toString());
    }
}
