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

package com.graphaware.reco.neo4j.filter;

import com.graphaware.reco.generic.config.Config;
import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.filter.BlacklistBuilder;
import com.graphaware.reco.generic.filter.Filter;
import org.neo4j.graphdb.Node;

import java.util.Collections;
import java.util.Set;

import static org.springframework.util.Assert.notNull;

/**
 * {@link BlacklistBuilder} and {@link Filter} that blacklists/excludes suggestions that are themselves.
 */
public class ExcludeSelf implements BlacklistBuilder<Node, Node>, Filter<Node, Node> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Node> buildBlacklist(Node input, Config config) {
        notNull(input);

        return Collections.singleton(input);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(Node item, Node input, Context<Node, Node> context) {
        notNull(item);
        notNull(input);

        return input.getId() != item.getId();
    }
}
