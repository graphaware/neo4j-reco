/*
 * Copyright (c) 2013-2016 GraphAware
 *
 * This file is part of the GraphAware Framework.
 *
 * GraphAware Framework is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.reco.generic.transform;

/**
 * A {@link TransformationFunction} that transforms the value based on an exponential (Pareto) function.
 * <p/>
 * f(x) = 1 - e^(-alpha*x), where x is the old value, alpha(h) = ln(5)/h, where h is the x that should get 80% of the
 * maximum value, and f(x) is the new value.
 * <p/>
 * For example, if we say that for common facebook friends, the maximum score is 100 and we want a person to have 80%
 * (i.e. 80) when they have 10 facebook friends, the score is computed as follows:
 * <p/>
 * score = 100 * (1-e^(-alpha*number_of_friends)), where alpha = ln(5)/10.
 */
public class ParetoFunction implements TransformationFunction {

    private final float maxValue;
    private final float eightyPercentLevel;
    private final float minimumThreshold;

    /**
     * Construct a new transformer.
     *
     * @param maxValue           maximum value this function will produce.
     * @param eightyPercentLevel value at which the function will produce 80% of the maximum value.
     */
    public ParetoFunction(float maxValue, float eightyPercentLevel) {
        this(maxValue, eightyPercentLevel, 0);
    }

    /**
     * Construct a new function.
     *
     * @param maxValue           maximum value this function will produce.
     * @param eightyPercentLevel value at which the function will produce 80% of the maximum value.
     * @param minimumThreshold   minimum input value that must be achieved to get a value higher than 0 out of this
     *                           function.
     */
    public ParetoFunction(float maxValue, float eightyPercentLevel, float minimumThreshold) {
        this.maxValue = maxValue;
        this.eightyPercentLevel = eightyPercentLevel;
        this.minimumThreshold = minimumThreshold;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float transform(float value) {
        if (value < minimumThreshold) {
            return 0;
        }

        double alpha = Math.log((double) 5) / eightyPercentLevel;
        double exp = Math.exp(-alpha * value);

        return new Double(maxValue * (1 - exp)).floatValue();
    }
}
