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

package com.graphaware.reco.generic.web;

import com.graphaware.reco.generic.config.SimpleConfig;

/**
 * {@link ConfigParser} producing {@link SimpleConfig}. Completely ignores extra config.
 */
public class SimpleConfigParser implements ConfigParser<SimpleConfig> {

    /**
     * {@inheritDoc}
     */
    @Override
    public SimpleConfig produceConfig(int limit, String config) {
        return new SimpleConfig(limit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SimpleConfig produceConfig(int limit, long maxTime, String config) {
        return new SimpleConfig(limit, maxTime);
    }
}
