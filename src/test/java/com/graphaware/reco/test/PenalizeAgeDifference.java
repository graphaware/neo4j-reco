package com.graphaware.reco.test;

import com.graphaware.reco.post.BasePostProcessor;
import com.graphaware.reco.score.Recommendations;
import com.graphaware.reco.transform.ParetoScoreTransformer;
import org.neo4j.graphdb.Node;

import static com.graphaware.common.util.PropertyContainerUtils.getInt;

/**
 * Subtract a point of each year of difference in age.
 */
public class PenalizeAgeDifference extends BasePostProcessor<Integer> {

    private final ParetoScoreTransformer transformer = new ParetoScoreTransformer(10, 20, 0);

    @Override
    protected void postProcess(Integer age, Node node, Recommendations<Node> output, Node input) {
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
