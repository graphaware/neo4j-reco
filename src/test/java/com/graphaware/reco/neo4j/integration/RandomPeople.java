package com.graphaware.reco.neo4j.integration;

import com.graphaware.common.policy.NodeInclusionPolicy;
import com.graphaware.reco.generic.filter.Filter;
import com.graphaware.reco.neo4j.engine.RandomEngine;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;

import java.util.List;

/**
 *
 */
public class RandomPeople extends RandomEngine {

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
}
