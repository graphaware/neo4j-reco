package com.graphaware.reco.test;

import com.graphaware.common.policy.NodeInclusionPolicy;
import com.graphaware.reco.filter.Filter;
import com.graphaware.reco.part.RandomRecommendations;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;

import java.util.List;

/**
 *
 */
public class RandomPeople extends RandomRecommendations {

    public RandomPeople() {
    }

    public RandomPeople(List<Filter<Node, Node>> filters) {
        super(filters);
    }

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
    protected String name() {
        return "random";
    }
}
