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

import com.graphaware.reco.generic.result.ScorePart;

/**
 * A {@link ScoreTransformer} that transforms the score based on an exponential (Pareto) function
 * <p/>
 * f(x) = 1 - e^(-alpha*x), where x is the old score, alpha(h) = ln(5)/h, where h is the x that should get 80% of the
 * maximum score, and f(x) is the new score.
 * <p/>
 * For example, if we say that for common facebook friends, the maximum score is 100 and we want a person to have 80%
 * (i.e. 80) when they have 10 facebook friends, the score is computed as follows:
 * <p/>
 * score = 100 * (1-e^(-alpha*number_of_friends)), where alpha = ln(5)/10.
 */
public class ParetoScoreTransformer implements ScoreTransformer {

    private final TransformationFunction function;

    /**
     * Construct a new transformer.
     *
     * @param maxScore           maximum score this transformer will produce.
     * @param eightyPercentLevel score at which the transformer will produce 80% of the maximum score.
     */
    public ParetoScoreTransformer(float maxScore, float eightyPercentLevel) {
        this(maxScore, eightyPercentLevel, 0);
    }

    /**
     * Construct a new transformer.
     *
     * @param maxScore           maximum score this transformer will produce.
     * @param eightyPercentLevel score at which the transformer will produce 80% of the maximum score.
     * @param minimumThreshold   minimum input score that must be achieved to get a score higher than 0 out of this
     *                           transformer.
     */
    public ParetoScoreTransformer(float maxScore, float eightyPercentLevel, float minimumThreshold) {
        function = new ParetoFunction(maxScore, eightyPercentLevel, minimumThreshold);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <OUT> ScorePart transform(OUT item, ScorePart score) {
        score.setValue(function.transform(score.getValue()));

        return score;
    }
}
