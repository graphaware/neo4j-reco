package com.graphaware.reco.test;

import com.graphaware.reco.post.AdditionalScorePostProcessor;
import com.graphaware.reco.post.PostProcessor;
import com.graphaware.reco.score.Recommendations;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import java.util.Arrays;

import static org.neo4j.helpers.collection.Iterables.toArray;

/**
 * Rewards same gender (exactly the same labels) by 5 points.
 */
public class RewardSameGender extends AdditionalScorePostProcessor<Label[]> {

    @Override
    protected String additionalScoreName() {
        return "sameGender";
    }

    @Override
    protected void doPostProcess(Label[] inputLabels, Node node, Recommendations<Node> output, Node input) {
        for (Node recommendation : output.getItems()) {
            if (Arrays.equals(inputLabels, toArray(Label.class, recommendation.getLabels()))) {
                output.add(recommendation, additionalScoreName(), 10);
            }
        }
    }

    @Override
    protected Label[] prepare(Recommendations<Node> output, Node input) {
        return toArray(Label.class, input.getLabels());
    }
}
