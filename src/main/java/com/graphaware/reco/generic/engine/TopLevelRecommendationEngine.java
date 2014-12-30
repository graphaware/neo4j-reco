/*
 * Copyright (c) 2014 GraphAware
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

package com.graphaware.reco.generic.engine;

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.context.ContextFactory;
import com.graphaware.reco.generic.context.Mode;
import com.graphaware.reco.generic.result.Recommendations;

/**
 *
 */
public class TopLevelRecommendationEngine<OUT, IN> extends DelegatingRecommendationEngine<OUT, IN> {

    private final ContextFactory<OUT, IN> contextFactory;

    public TopLevelRecommendationEngine(ContextFactory<OUT, IN> contextFactory) {
        this.contextFactory = contextFactory;
    }

    public Recommendations<OUT> recommend(IN input, Mode mode, int limit) {
        return recommend(input, contextFactory.produceContext(input, mode, limit));
    }
}

