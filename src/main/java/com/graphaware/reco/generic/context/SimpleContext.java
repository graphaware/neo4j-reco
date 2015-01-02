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

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

/**
 * The simplest possible context, allowing all recommendations.
 */
public class SimpleContext<OUT, IN> implements Context<OUT, IN> {

    private final Mode mode;
    private final int limit;

    /**
     * Construct a new context.
     *
     * @param mode  in which recommendations are being computed. Must not be <code>null</code>.
     * @param limit the maximum number of desired recommendations. Must be positive.
     */
    SimpleContext(Mode mode, int limit) {
        notNull(mode);
        isTrue(limit > 0);

        this.mode = mode;
        this.limit = limit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Mode mode() {
        return mode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int limit() {
        return limit;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * no-op by default.
     */
    @Override
    public void initialize(IN input) {
        //no-op
    }

    /**
     * {@inheritDoc}
     * <p/>
     * always returns true by default.
     */
    @Override
    public boolean allow(OUT recommendation, IN input) {
        notNull(recommendation);
        notNull(input);

        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @throws java.lang.UnsupportedOperationException at all times, unless overridden.
     */
    @Override
    public void disallow(OUT recommendation) {
        throw new UnsupportedOperationException("SimpleContext does not support blacklisting items. Please use FilteringContext");
    }
}

