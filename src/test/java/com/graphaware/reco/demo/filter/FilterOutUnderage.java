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

package com.graphaware.reco.demo.filter;

import com.graphaware.reco.generic.config.KeyValueConfig;
import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.filter.Filter;
import org.neo4j.graphdb.Node;

import static com.graphaware.common.util.PropertyContainerUtils.getInt;

/**
 * {@link Filter} that filters out potentially recommended people that are underage. The minimum legal age is passed
 * in as a "legalAge" String configuration value ({@link Context#config()}), defaulting to "18".
 */
public class FilterOutUnderage implements Filter<Node, Node> {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(Node item, Node input, Context<Node, Node> context) {
        int age = getInt(item, "age", 0);
        int legalAge = Integer.valueOf(context.config(KeyValueConfig.class).get("legalAge", "18", String.class));

        return age >= legalAge;
    }
}

