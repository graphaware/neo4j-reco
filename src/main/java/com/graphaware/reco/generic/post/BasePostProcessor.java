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

package com.graphaware.reco.generic.post;

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.result.Recommendations;

/**
 * Base class for {@link PostProcessor} implementation that collects timing statistics about the process.
 *
 * @param <OUT> type of the post-processed recommendations.
 * @param <IN>  type of the item recommendations are for / based on.
 */
public abstract class BasePostProcessor<OUT, IN> implements PostProcessor<OUT, IN> {

    /**
     * Get the name of this post processor. This name should be unique within the overall recommendation engine structure and
     * will be used for naming scores produced by the post processor, as well as for collecting {@link com.graphaware.reco.generic.stats.Statistics}.
     *
     * @return name.
     */
    protected abstract String name();

    /**
     * {@inheritDoc}
     */
    @Override
    public final void postProcess(Recommendations<OUT> recommendations, IN input, Context<OUT, IN> context) {
        context.statistics().startTiming(name());

        doPostProcess(recommendations, input, context);

        context.statistics().stopTiming(name());
    }

    /**
     * Post-process results.
     *
     * @param recommendations scored recommendations.
     * @param input           for whom the recommendation have been produced, must not be <code>null</code>.
     * @param context         for the recommendation computing process.
     */
    protected abstract void doPostProcess(Recommendations<OUT> recommendations, IN input, Context<OUT, IN> context);
}
