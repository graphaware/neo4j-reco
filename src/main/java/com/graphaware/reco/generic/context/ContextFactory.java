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

/**
 * A singleton component responsible for producing {@link com.graphaware.reco.generic.context.Context}s for the
 * recommendation-computing process. A new {@link com.graphaware.reco.generic.context.Context} should be produced every
 * time recommendations are computed for an input.
 * <p/>
 * Implementations must be thread-safe.
 */
public interface ContextFactory<OUT, IN> {

    /**
     * Produce a {@link com.graphaware.reco.generic.context.Context} for the recommendation-computing process.
     *
     * @param input for which recommendations are about to be computed.
     * @param mode  in which the computation takes place.
     * @param limit maximum number of recommendations desired.
     * @return context.
     */
    Context<OUT, IN> produceContext(IN input, Mode mode, int limit);
}
