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

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.result.PartialScore;

/**
 * A component that can transform recommendation scores.
 */
public interface ScoreTransformer<OUT> {

    /**
     * Transform a partial score.
     *
     * @param item         recommended item.
     * @param partialScore partial score of the item.
     * @param context      of the recommendation computing process.
     * @return transformed partial score.
     */
    PartialScore transform(OUT item, PartialScore partialScore, Context<? extends OUT, ?> context);
}
