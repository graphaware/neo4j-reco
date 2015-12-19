package com.graphaware.reco.integration.post;

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.post.BasePostProcessor;
import com.graphaware.reco.generic.result.Recommendation;
import com.graphaware.reco.generic.result.Recommendations;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpanders;

public class ShortestPath extends BasePostProcessor<Node, Node> {

    @Override
    protected String name() {
        return "shortestPath";
    }

    @Override
    protected void doPostProcess(Recommendations<Node> recommendations, Node input, Context<Node, Node> context) {
        PathFinder<Path> finder = GraphAlgoFactory.shortestPath(
                PathExpanders.allTypesAndDirections(), 15
        );

        for (Recommendation<Node> recommendation : recommendations.get()) {
            Path path = finder.findSinglePath(input, recommendation.getItem());
            recommendation.add(name(), path.length());
        }
    }
}
