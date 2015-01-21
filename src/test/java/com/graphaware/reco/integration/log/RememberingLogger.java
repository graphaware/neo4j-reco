package com.graphaware.reco.integration.log;

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.log.RecommendationLogger;
import com.graphaware.reco.generic.result.Recommendation;
import com.graphaware.reco.neo4j.result.RecommendationsPrinter;
import org.neo4j.graphdb.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RememberingLogger implements RecommendationLogger<Node, Node> {

    private static final Map<Node, String> loggedRecommendations = new HashMap<>();

    @Override
    public void logRecommendations(Node input, List<Recommendation<Node>> recommendations, Context<Node, Node> context) {
        loggedRecommendations.put(input, RecommendationsPrinter.toString(recommendations));
    }

    public String get(Node node) {
        return loggedRecommendations.containsKey(node) ? loggedRecommendations.get(node) : "";
    }

    public void clear() {
        loggedRecommendations.clear();
    }
}
