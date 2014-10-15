package com.graphaware.reco.test;

import com.graphaware.common.util.PropertyContainerUtils;
import com.graphaware.reco.post.AdditionalScorePostProcessor;
import com.graphaware.reco.post.PostProcessor;
import com.graphaware.reco.score.Recommendations;
import com.graphaware.reco.transform.ParetoScoreTransformer;
import com.graphaware.reco.transform.ScoreTransformer;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import java.util.Arrays;

import static com.graphaware.common.util.PropertyContainerUtils.*;
import static org.neo4j.helpers.collection.Iterables.toArray;

/**
 * Subtract a point of each year of difference in age.
 */
public class PenalizeAgeDifference extends AdditionalScorePostProcessor<Integer> {

    private final ParetoScoreTransformer transformer = new ParetoScoreTransformer(10, 20, 0);

    @Override
    protected String additionalScoreName() {
        return "ageDifference";
    }

    @Override
    protected void doPostProcess(Integer age, Node node, Recommendations<Node> output, Node input) {
        for (Node reco : output.getItems()) {
            int diff = Math.abs(getInt(reco, "age", 40) - age);
            output.add(reco, "ageDifference", -transformer.transform(diff));
        }
    }

    @Override
    protected Integer prepare(Recommendations<Node> output, Node input) {
        return getInt(input, "age", 40);
    }
}
