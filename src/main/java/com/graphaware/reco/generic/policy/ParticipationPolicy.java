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

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.result.Recommendations;

/**
 * A participation policy for {@link com.graphaware.reco.generic.engine.RecommendationEngine}s deciding whether to participate
 * in the recommendation process for the given input and context.
 */
public interface ParticipationPolicy<OUT, IN> {

    static ParticipationPolicy ALWAYS = new ParticipationPolicy() {
        @Override
        public boolean participate(Object input, Context context, Recommendations recommendations) {
            return true;
        }
    };

    static ParticipationPolicy NEVER = new ParticipationPolicy() {
        @Override
        public boolean participate(Object input, Context context, Recommendations recommendations) {
            return false;
        }
    };

    static ParticipationPolicy IF_MORE_RESULTS_NEEDED = new ParticipationPolicy() {
        @Override
        public boolean participate(Object input, Context context, Recommendations recommendations) {
            return !recommendations.hasEnough(context.limit());
        }
    };

    /**
     * Decide whether to participate or not.
     *
     * @param input           for which recommendations are being computed.
     * @param context         in which recommendations are being computed.
     * @param recommendations recommendations produced so far.
     * @return true iff participate.
     */
    boolean participate(IN input, Context<OUT, IN> context, Recommendations<OUT> recommendations);
}
