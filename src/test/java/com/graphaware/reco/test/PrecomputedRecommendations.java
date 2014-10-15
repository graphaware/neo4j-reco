package com.graphaware.reco.test;

import com.graphaware.reco.filter.Filter;
import com.graphaware.reco.part.PrecomputedEnginePart;
import com.graphaware.reco.score.Recommendations;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import java.util.List;

import static com.graphaware.common.util.PropertyContainerUtils.getInt;
import static com.graphaware.reco.demo.Relationships.RECOMMEND;

public class PrecomputedRecommendations extends PrecomputedEnginePart {

    public PrecomputedRecommendations() {
    }

    public PrecomputedRecommendations(List<Filter<Node, Node>> filters) {
        super(filters);
    }

    @Override
    protected RelationshipType getType() {
        return RECOMMEND;
    }

    @Override
    protected void addToResult(Recommendations<Node> result, Relationship recommendation) {
        super.addToResult(result, recommendation);
        result.add(recommendation.getEndNode(), "preComputed", 0);
    }
}
