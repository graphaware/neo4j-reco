package com.graphaware.reco.post;

import com.graphaware.common.util.Pair;
import com.graphaware.reco.score.CompositeScore;
import com.graphaware.reco.score.Recommendations;
import org.neo4j.graphdb.Node;

/**
 * A {@link com.graphaware.reco.post.PostProcessor} that should only be applied once.
 */
public abstract class BasePostProcessor<T> implements PostProcessor<Node, Node> {

    /**
     * {@inheritDoc}
     */
    @Override
    public final void postProcess(Recommendations<Node> output, Node input) {
        T t = prepare(output, input);

        for (Node node : output.getItems()) {
             postProcess(t, node, output, input);
        }
    }

    protected abstract T prepare(Recommendations<Node> output, Node input);

    /**
     * Do the actual post processing.
     *
     * @param output scored recommendations.
     * @param input  for whom the recommendation have been produced, must not be <code>null</code>.
     */
    protected abstract void postProcess(T t, Node node, Recommendations<Node> output, Node input);
}