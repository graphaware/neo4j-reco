package com.graphaware.reco.post;

import com.graphaware.reco.score.Recommendations;
import org.neo4j.graphdb.Node;

/**
 * {@link ApplyOncePostProcessor} that adds an additional score to the output.
 */
public abstract class AdditionalScorePostProcessor<T> extends ApplyOncePostProcessor<T> {

    @Override
    protected boolean hasBeenApplied(Node node, Recommendations<Node> output) {
        return output.get(node).second().contains(additionalScoreName());
    }

    /**
     * Get the name of the score added by this post processor.
     *
     * @return score name.
     */
    protected abstract String additionalScoreName();
}