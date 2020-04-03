package it.unibz.inf.ontop.spec.dbschema.impl;

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.dbschema.DBMetadata;
import it.unibz.inf.ontop.dbschema.RelationDefinition;
import it.unibz.inf.ontop.dbschema.RelationID;
import it.unibz.inf.ontop.spec.dbschema.MetadataProvider;

public class EmptyMetadataProvider implements MetadataProvider {

    @Override
    public ImmutableList<RelationID> getRelationIDs() {
        return ImmutableList.of();
    }

    @Override
    public ImmutableList<RelationDefinition.AttributeListBuilder> getRelationAttributes(RelationID relationID) {
        return ImmutableList.of();
    }

    @Override
    public void insertIntegrityConstraints(DBMetadata md) {
    }
}
