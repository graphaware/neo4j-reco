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

import static org.springframework.util.Assert.isTrue;

/**
 * The simplest implementation of {@link Config}. To be used when the recommendation engine is configured entirely
 * in Java (using hard-coded values for scores etc.), and the only user-driven configuration is the number of desired
 * results ({@link #limit()}) and optionally the maximum time the computation should take ({@link #maxTime()}).
 */
public class SimpleConfig implements Config {

    private final int limit;
    private final long maxTime;

    /**
     * Create new config with no limit on how long the computation should take.
     *
     * @param limit desired maximum number of produced recommendations. Must be positive.
     */
    public SimpleConfig(int limit) {
        this(limit, Long.MAX_VALUE);
    }

    /**
     * Create new config.
     *
     * @param limit   desired maximum number of produced recommendations. Must be positive.
     * @param maxTime desired maximum time in ms that the recommendation-computing process should take. Must be positive.
     */
    public SimpleConfig(int limit, long maxTime) {
        isTrue(limit > 0);
        isTrue(maxTime > 0);

        this.limit = limit;
        this.maxTime = maxTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int limit() {
        return limit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long maxTime() {
        return maxTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleConfig that = (SimpleConfig) o;

        if (limit != that.limit) return false;
        return maxTime == that.maxTime;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = limit;
        result = 31 * result + (int) (maxTime ^ (maxTime >>> 32));
        return result;
    }
}
