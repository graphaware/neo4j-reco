package com.graphaware.reco.neo4j.engine;


import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.cypher.Statement;
import com.graphaware.reco.generic.engine.SingleScoreRecommendationEngine;
import com.graphaware.reco.generic.result.PartialScore;
import org.neo4j.graphdb.*;

import java.util.HashMap;
import java.util.Map;

public abstract class TriadicClosureEngine extends SingleScoreRecommendationEngine{

    private RelationshipType type;
    private Direction direction;
    private DynamicLabel label;

    public TriadicClosureEngine(DynamicLabel label, RelationshipType type, Direction direction){
        this.label = label;
        this.type = type;
        this.direction = direction;
    }

    protected final Map<Node, PartialScore> doRecommendSingle(Node input, Context<Node, Node> context){
        Map<Node, PartialScore> result = new HashMap<>();
        GraphDatabaseService database = input.getGraphDatabase();

        Statement statement = buildQuery(input.getId());
        Result queryResult = database.execute(statement.getQuery(), statement.getParameters());

        while (queryResult.hasNext()) {
            Map<String, Object> row = queryResult.next();
            addToResult(result, (Node) row.get("reco"), buildScore(row));
        }

        return result;
    }

    /**
     * Build a score for a particular recommendation from a row of Cypher query results.
     *
     * @param row of results from Cypher query.
     * @return score.
     */
    protected PartialScore buildScore(Map<String, Object> row) {
        if (row.containsKey("score")) {
            return new PartialScore(Float.valueOf(String.valueOf(row.get("score"))));
        }

        return new PartialScore(1);
    }

    private Statement buildQuery(Long inputId){
        String query = "MATCH (n:" + this.label.toString() + ") WHERE id(n) = {id}" +
                "MATCH (n)";

        String relPart;
        switch (this.direction) {
            case OUTGOING:
                relPart = "-[:" + this.type.toString() + "]->";
                break;
            case INCOMING:
                relPart = "<-[:" + this.type.toString() + "]-";
                break;
            default:
                throw new IllegalArgumentException("Invalid Relationship Direction");
        }
        query += relPart + "(b)" + relPart + "(c:" + this.label.toString() + ")" +
                "WHERE NOT (a)" + relPart + "(b)" +
                "AND (a) <> (b)" +
                "RETURN c as reco, count(*) as score";

        Statement statement = new Statement(query);
        statement.addParameter("id", inputId);

        return statement;
    }

}
