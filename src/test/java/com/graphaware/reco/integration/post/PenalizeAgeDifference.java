package com.graphaware.reco.integration.post;

import com.graphaware.reco.generic.post.PostProcessor;
import com.graphaware.reco.generic.result.Recommendations;
import com.graphaware.reco.generic.transform.ParetoScoreTransformer;
import org.neo4j.graphdb.Node;

import static com.graphaware.common.util.PropertyContainerUtils.getInt;

/**
 * Subtracts a point of each year of difference in age.
 */
public class PenalizeAgeDifference implements PostProcessor<Node, Node> {

    private final ParetoScoreTransformer transformer = new ParetoScoreTransformer(10, 20, 0);

    @Override
    public void postProcess(Recommendations<Node> recommendations, Node input) {
        int age = getInt(input, "age", 40);

        for (Node reco : recommendations.getItems()) {
            int diff = Math.abs(getInt(reco, "age", 40) - age);
            recommendations.add(reco, "ageDifference", -transformer.transform(reco, diff));
        }
    }
}
