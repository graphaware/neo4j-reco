package com.graphaware.reco.integration.engine;

import com.graphaware.common.policy.NodeInclusionPolicy;
import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.neo4j.engine.RandomRecommendations;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;

/**
 *
 */
public class RandomPeople extends RandomRecommendations {

    @Override
    protected NodeInclusionPolicy getPolicy() {
        return new NodeInclusionPolicy() {
            @Override
            public boolean include(Node node) {
                return node.hasLabel(DynamicLabel.label("Person"));
            }
        };
    }

    @Override
    protected String scoreName() {
        return "random";
    }

    @Override
    protected int numberOfRecommendations(Context<Node, Node> context) {
        return context.limit() * 5;
    }
}
