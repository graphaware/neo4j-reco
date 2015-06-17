package com.graphaware.reco.integration.engine;


import com.graphaware.reco.integration.domain.Relationships;
import com.graphaware.reco.neo4j.engine.SomethingThroughCommon;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;

public class FoaFoaFComputingEngine extends SomethingThroughCommon{

    private final Label PERSON = DynamicLabel.label("Person");

    @Override
    public String name()
    {
        return "friendsOfAFriend";
    }

    @Override
    protected RelationshipType getType()
    {
        return Relationships.FRIEND_OF;
    }

    @Override
    protected Direction getDirection()
    {
        return Direction.OUTGOING;
    }

    @Override
    protected Label getLabel()
    {
        return PERSON;
    }
}
