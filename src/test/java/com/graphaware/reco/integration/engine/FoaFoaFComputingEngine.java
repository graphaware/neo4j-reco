package com.graphaware.reco.integration.engine;


import com.graphaware.reco.integration.domain.Relationships;
import com.graphaware.reco.neo4j.engine.SomethingThroughCommon;
import org.neo4j.graphdb.*;

import java.util.Collections;
import java.util.Map;

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

    @Override
    protected Map<String, Object> details(Node throughCommon, Node similar) {
        return Collections.singletonMap("foaf", similar.getProperty("name"));
    }
}
