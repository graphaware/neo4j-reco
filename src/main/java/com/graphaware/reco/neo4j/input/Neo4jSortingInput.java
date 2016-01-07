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

package com.graphaware.reco.neo4j.input;

import com.graphaware.reco.generic.input.SortingInput;
import com.graphaware.reco.generic.result.Recommendation;
import org.neo4j.graphdb.Node;

import java.util.Collection;

/**
 * A {@link SortingInput} for Neo4j.
 */
public class Neo4jSortingInput implements SortingInput<Node, Node> {

    private final Node input;
    private final Collection<Recommendation<Node>> candidates;

    /**
     * Create a new sorting input.
     *
     * @param input      input (context) to the sorting process.
     * @param candidates recommendations that should be (re)ordered.
     */
    public Neo4jSortingInput(Node input, Collection<Recommendation<Node>> candidates) {
        this.input = input;
        this.candidates = candidates;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node input() {
        return input;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Recommendation<Node>> candidates() {
        return candidates;
    }
}
