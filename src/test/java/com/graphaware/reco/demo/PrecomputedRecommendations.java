package com.graphaware.reco.demo;

import com.graphaware.reco.filter.Filter;
import com.graphaware.reco.part.PrecomputedEnginePart;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

import java.util.List;

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
}
