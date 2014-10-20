package com.graphaware.reco.test;

import com.graphaware.reco.post.BasePostProcessor;
import com.graphaware.reco.score.Recommendations;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import java.util.Arrays;

import static org.neo4j.helpers.collection.Iterables.toArray;

/**
 * Rewards same gender (exactly the same labels) by 5 points.
 */
public class RewardSameGender extends BasePostProcessor<Label[]> {

    @Override
    protected void postProcess(Label[] inputLabels, Node node, Recommendations<Node> output, Node input) {
        for (Node recommendation : output.getItems()) {
            if (Arrays.equals(inputLabels, toArray(Label.class, recommendation.getLabels()))) {
                output.add(recommendation, "sameGender", 10);
            }
        }
    }

    @Override
    protected Label[] prepare(Recommendations<Node> output, Node input) {
        return toArray(Label.class, input.getLabels());
    }
}
