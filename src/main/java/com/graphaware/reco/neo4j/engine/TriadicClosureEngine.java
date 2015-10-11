package com.graphaware.reco.neo4j.engine;


import com.graphaware.reco.generic.context.Context;
import com.graphaware.reco.generic.cypher.Statement;
import com.graphaware.reco.generic.engine.SingleScoreRecommendationEngine;
import com.graphaware.reco.generic.result.PartialScore;
import com.graphaware.common.util.DirectionUtils;
import org.neo4j.graphdb.*;

import java.util.HashMap;
import java.util.Map;

public abstract class TriadicClosureEngine extends SingleScoreRecommendationEngine<Node,Node>{

    private RelationshipType type;
    private Direction direction;
    private Label label;
    private boolean inverseExpandedDirection = false;
    private boolean isNegative = true;
    private RelationshipType expandedRelationshipType;

    public TriadicClosureEngine(Label label, RelationshipType type, Direction direction){
        this.label = label;
        this.type = type;
        this.direction = direction;
        this.expandedRelationshipType = type;
    }

    @Override
    protected final Map<Node, PartialScore> doRecommendSingle(Node input, Context context) {
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

    /**
     * Returns whether or not the Direction of the expanded path should be inversed.
     * Given a triangle with <code>(a)-[r]-(b)-[r2]-(c)</code>, if the Direction for r
     * is OUTGOING, and this method returns true, then r2 will be INCOMING and produce the
     * following pattern : <code>(a)-->(b)<--(c)</code>.
     *
     * @return boolean
     */
    protected boolean inverseExpandedDirection(){
        return inverseExpandedDirection;
    }

    /**
     * This method returns true by default, you can override it to specify that you are searching
     * for nodes that are already connected in the triangle.
     * This will produce a <code>WHERE (a)-->(c)</code> instead of a <code>WHERE NOT</code>
     * @return boolean
     */
    protected boolean isNegative(){
        return isNegative;
    }

    /**
     * By default, the Relationship type from b to c will be the same as for a to b.
     * You can override this method to specify a different RelationshipType.
     * @return RelationshipType
     */
    protected RelationshipType expandedRelationshipType(){
        return expandedRelationshipType;
    }

    protected String startIdentifier(){
        return "a";
    }

    protected String endIdentifier(){
        return "c";
    }

    private Statement buildQuery(Long inputId){
        String query = "MATCH (" + startIdentifier() + ":" + this.label.toString() + ") WHERE id(" + startIdentifier() + ") = {id} " +
                " MATCH (" + startIdentifier() + ")";

        query += buildRelationshipPattern(this.direction, this.type) + "(b)" + buildRelationshipPattern(expandedRelationshipDirection(), expandedRelationshipType()) + "(" + endIdentifier() + ":" + this.label.toString() + ") " +
                getWhereClause() +
                "AND (" + startIdentifier() + ") <> (" + endIdentifier() + ")" +
                "RETURN " + endIdentifier() + " as reco, count(*) as score";

        Statement statement = new Statement(query);
        statement.addParameter("id", inputId);

        return statement;
    }

    private String buildRelationshipPattern(Direction direction, RelationshipType type){
        String relPart;
        switch (this.direction) {
            case OUTGOING:
                relPart = "-[:" + type.toString() + "]->";
                break;
            case INCOMING:
                relPart = "<-[:" + type.toString() + "]-";
                break;
            default:
                throw new IllegalArgumentException("Invalid Relationship Direction");
        }

        return relPart;
    }

    private Direction expandedRelationshipDirection(){
        if (inverseExpandedDirection) return DirectionUtils.reverse(direction);

        return direction;
    }

    private String getWhereClause(){
        String clause = "WHERE ";
        if (isNegative()) {
            clause += "NOT ";
        }
        clause += "(" + startIdentifier() + ")" + buildRelationshipPattern(this.direction, this.type) + "(" + endIdentifier() + ")";

        return clause;
    }

}
