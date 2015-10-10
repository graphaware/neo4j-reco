package com.graphaware.reco.neo4j.engine;


import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;

public class Triadic extends TriadicClosureEngine{

    public Triadic(){
        super(DynamicLabel.label("Person"), DynamicRelationshipType.withName("KNOWS"), Direction.OUTGOING);
    }

    @Override
    public String name(){
        return "triadic-knows";
    }

}
