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

package com.graphaware.reco.generic.web;

import com.graphaware.reco.generic.config.Config;

/**
 * Component that produces a {@link Config} from a {@link String}. Intended for web environments, where config is passed
 * in as a request parameter.
 *
 * @param <C> type of the config produced.
 */
public interface ConfigParser<C extends Config> {

    /**
     * Produce config.
     *
     * @param limit  desired maximum number of produced recommendations. Must be positive.
     * @param config additional config. Must not be <code>null</code>, but can be empty for no config.
     * @return config.
     */
    C produceConfig(int limit, String config);

    /**
     * Produce config.
     *
     * @param limit   desired maximum number of produced recommendations. Must be positive.
     * @param maxTime desired maximum time in ms that the recommendation-computing process should take. Must be positive.
     * @param config  additional config. Must not be <code>null</code>, but can be empty for no config.
     * @return config.
     */
    C produceConfig(int limit, long maxTime, String config);
}
