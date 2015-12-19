package com.graphaware.reco.neo4j.post;

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.post.BasePostProcessor;
import com.graphaware.reco.generic.result.PartialScore;
import com.graphaware.reco.generic.result.Recommendation;
import com.graphaware.reco.generic.result.Recommendations;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.*;

/**
 * {@link com.graphaware.reco.generic.post.PostProcessor} that rewards paths between the subject of the recommendation
 * (ie. the input of the recommendation engine) and the recommended item.
 */
public abstract class RewardPathsInBetween extends BasePostProcessor<Node, Node> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doPostProcess(Recommendations<Node> recommendations, Node input, Context<Node, Node> context) {
        for (Recommendation<Node> recommendation : recommendations.get()) {
            for (Path path : finder().findAllPaths(input, recommendation.getItem())) {
                recommendation.add(name(), partialScore(recommendation.getItem(), input, path));
            }
        }
    }

    /**
     * Get a PathFinder built with knowledge of types and directions of relationships to follow
     *
     * @return PathFinder
     */
    protected abstract PathFinder<Path> finder();

    /**
     * Get the partial score this post processor adds if subject and the recommended item have a path found by the PathFinder.
     * Note that a partial score will be added for each path found by the PathFinder.
     *
     * @param recommendation the recommendation
     * @param input the input (subject)
     * @param path the path between the input and the recommendation
     *
     * @return PartialScore
     */
    protected abstract PartialScore partialScore(Node recommendation, Node input, Path path);
}
