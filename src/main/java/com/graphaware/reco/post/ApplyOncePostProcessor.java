package com.graphaware.reco.post;

import com.graphaware.reco.score.Recommendations;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

/**
 * A {@link PostProcessor} that should only be applied once.
 */
public abstract class ApplyOncePostProcessor<T> extends BasePostProcessor<T> {

    @Override
    protected void postProcess(T t, Node node, Recommendations<Node> output, Node input) {
        if (hasBeenApplied(node, output)) {
            return;
        }

        doPostProcess(t, node, output, input);
    }



    /**
     * Has this {@link PostProcessor} been applied to this output yet?
     *
     * @param output out.
     * @return true iff already applied.
     */
    protected abstract boolean hasBeenApplied(Node node, Recommendations<Node> output);

    /**
     * Do the actual post processing.
     *
     * @param output scored recommendations.
     * @param input  for whom the recommendation have been produced, must not be <code>null</code>.
     */
    protected abstract void doPostProcess(T t, Node node, Recommendations<Node> output, Node input);
}