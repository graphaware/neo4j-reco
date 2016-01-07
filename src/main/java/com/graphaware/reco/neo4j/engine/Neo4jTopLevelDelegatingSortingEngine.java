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

package com.graphaware.reco.neo4j.engine;

import com.graphaware.reco.generic.engine.TopLevelDelegatingSortingEngine;
import org.neo4j.graphdb.Node;

/**
 * A Neo4j {@link TopLevelDelegatingSortingEngine} where both the input to the
 * sorting process and the sorted recommendations are {@link org.neo4j.graphdb.Node}s.
 */
public class Neo4jTopLevelDelegatingSortingEngine extends TopLevelDelegatingSortingEngine<Node, Node> {
}
