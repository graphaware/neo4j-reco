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

package com.graphaware.reco.generic.filter;

import com.graphaware.reco.generic.config.Config;

import java.util.Set;

/**
 * Component that is able to build a blacklist of recommendations, i.e. items that must not be recommended for given input.
 * <p/>
 * Intended for filtering out recommendation that are already irrelevant, such as items a user has already bought, people
 * that are already friends, etc.
 *
 * @param <OUT> type of recommendations.
 * @param <IN>  type of input on which recommendations are based.
 */
public interface BlacklistBuilder<OUT, IN> {

    /**
     * Get a set of items that must not be used as a recommendation for given input.
     *
     * @param input  for which recommendations are being computed. Must not be <code>null</code>.
     * @param config for the recommendation computing process. Must not be <code>null</code>.
     * @return set of blacklisted recommendations.
     */
    Set<OUT> buildBlacklist(IN input, Config config);
}
