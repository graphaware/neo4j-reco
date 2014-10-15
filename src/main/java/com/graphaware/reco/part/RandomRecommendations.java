package com.graphaware.reco.part;

import com.graphaware.common.policy.NodeInclusionPolicy;
import com.graphaware.reco.filter.Filter;
import com.graphaware.reco.transform.ScoreTransformer;
import com.graphaware.runtime.walk.NodeSelector;
import com.graphaware.runtime.walk.RandomNodeSelector;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.util.*;

import static org.neo4j.graphdb.Direction.*;

/**
 * {@link com.graphaware.reco.part.ScoringEnginePart} that randomly recommends {@link org.neo4j.graphdb.Node}s which
 * comply with the provided {@link com.graphaware.common.policy.NodeInclusionPolicy}.
 */
public abstract class RandomRecommendations extends ScoringEnginePart<Node, Node> {

    private NodeSelector selector;

    protected RandomRecommendations() {
    }

    protected RandomRecommendations(List<Filter<Node, Node>> filters) {
        super(filters);
    }

    @Override
    protected void populateResult(Map<Node, Integer> result, Node input, int limit, Set<Node> blacklist) {
        if (selector == null) {
            selector = new RandomNodeSelector(getPolicy());
        }

        int attempts = 0;
        while (attempts++ < 100 && result.size() < limit) {
            addToResult(result, input, blacklist, selector.selectNode(input.getGraphDatabase()), 0);
        }
    }

    /**
     * Get the node inclusion policy of the nodes that can be used as recommendations.
     *
     * @return policy.
     */
    protected abstract NodeInclusionPolicy getPolicy();

    /**
     * {@inheritDoc}
     */
    @Override
    public EnoughResultsPolicy enoughResultsPolicy() {
        return EnoughResultsPolicy.SKIP_AND_CONTINUE;
    }
}
