package com.graphaware.reco.integration.log;

import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.log.Slf4jRecommendationLogger;
import com.graphaware.reco.generic.result.Recommendation;
import com.graphaware.reco.neo4j.result.RecommendationsPrinter;
import org.neo4j.graphdb.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendationsRememberingLogger extends Slf4jRecommendationLogger<Node, Node> {

    private static final Map<Node, String> loggedRecommendations = new HashMap<>();

    @Override
    public String toString(Node input, List<Recommendation<Node>> recommendations, Context<Node, Node> context) {
        String result = super.toString(input, recommendations, context);
        loggedRecommendations.put(input, result);
        return result;
    }

    @Override
    protected String inputToString(Node input) {
        return input.getProperty("name", "unknown").toString();
    }

    @Override
    protected String itemToString(Node item) {
        return item.getProperty("name", "unknown").toString();
    }

    @Override
    protected boolean logUuid() {
        return false;
    }

    public String get(Node node) {
        return loggedRecommendations.containsKey(node) ? loggedRecommendations.get(node) : "";
    }

    public void clear() {
        loggedRecommendations.clear();
    }
}
