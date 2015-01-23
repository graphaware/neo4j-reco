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

/**
 * {@link ScoreTransformer} that performs no transformation. Singleton.
 */
public final class NoTransformation implements ScoreTransformer {

    private static final NoTransformation INSTANCE = new NoTransformation();

    public static NoTransformation getInstance() {
        return INSTANCE;
    }

    private NoTransformation() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <OUT> float transform(OUT item, float score) {
        return score;
    }
}
