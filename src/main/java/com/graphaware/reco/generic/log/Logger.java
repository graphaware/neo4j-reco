/*
 * Copyright (c) 2013-2015 GraphAware
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

package com.graphaware.reco.generic.log;

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.result.Recommendation;

import java.util.List;

/**
 * A component capable of recording / logging information about the computation of recommendations.
 *
 * @param <OUT> type of the recommendations produced.
 * @param <IN>  type of the item recommendations are for / based on.
 */
public interface Logger<OUT, IN> {

    /**
     * Record / log recommendations.
     *
     * @param input           for which the recommendations have been produced. Must not be <code>null</code>.
     * @param recommendations that have been computed. Must not be <code>null</code>.
     * @param context         in which the recommendations were produced.
     */
    void log(IN input, List<Recommendation<OUT>> recommendations, Context<OUT, IN> context);

    /**
     * Convert recommendation information to String.
     *
     * @param input           for which the recommendations have been produced. Must not be <code>null</code>.
     * @param recommendations that have been computed. Must not be <code>null</code>.
     * @param context         in which the recommendations were produced.
     * @return string representation of the stuff this logger normally logs.
     */
    String toString(IN input, List<Recommendation<OUT>> recommendations, Context<OUT, IN> context);
}
