package com.graphaware.reco.neo4j.demo;

import com.graphaware.reco.generic.post.PostProcessor;
import com.graphaware.reco.generic.result.Recommendations;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import java.util.Arrays;

import static org.neo4j.helpers.collection.Iterables.toArray;

/**
 * Rewards same gender (exactly the same labels) by 10 points.
 */
public class RewardSameGender implements PostProcessor<Node, Node> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void postProcess(Recommendations<Node> recommendations, Node input) {
        Label[] inputLabels = toArray(Label.class, input.getLabels());
        for (Node recommendation : recommendations.getItems()) {
            if (Arrays.equals(inputLabels, toArray(Label.class, recommendation.getLabels()))) {
                recommendations.add(recommendation, "sameGender", 10);
            }
        }
    }
}
