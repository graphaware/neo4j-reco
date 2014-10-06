package com.graphaware.reco.demo;

import com.graphaware.reco.engine.DelegatingEngine;
import com.graphaware.reco.filter.Blacklist;
import com.graphaware.reco.filter.ExcludeSelf;
import com.graphaware.reco.filter.ExistingRelationshipBlacklist;
import com.graphaware.reco.filter.Filter;
import com.graphaware.reco.part.EnginePart;
import com.graphaware.reco.post.PostProcessor;
import org.neo4j.graphdb.Node;

import java.util.Arrays;
import java.util.List;

import static com.graphaware.reco.demo.Relationships.FRIEND_OF;
import static org.neo4j.graphdb.Direction.BOTH;

/**
 *
 */
public class FriendsRecommendationEngine extends DelegatingEngine<Node, Node> {

    public FriendsRecommendationEngine() {
        super(parts(), blacklists(), postProcessors());
    }

    private static List<EnginePart<Node, Node>> parts() {
        return Arrays.<EnginePart<Node, Node>>asList(new FriendsInCommon(filters()));
    }

    private static List<Filter<Node, Node>> filters() {
        return Arrays.<Filter<Node, Node>>asList(new ExcludeSelf());
    }

    private static List<Blacklist<Node, Node>> blacklists() {
        return Arrays.asList(new ExcludeSelf(), new ExistingRelationshipBlacklist(FRIEND_OF, BOTH));
    }

    private static List<PostProcessor<Node, Node>> postProcessors() {
        return Arrays.<PostProcessor<Node, Node>>asList(new RewardSameGender());
    }
}
