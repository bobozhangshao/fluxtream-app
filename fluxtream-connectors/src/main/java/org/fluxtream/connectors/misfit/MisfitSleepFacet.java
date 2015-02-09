package org.fluxtream.connectors.misfit;

import org.fluxtream.core.connectors.annotations.ObjectTypeSpec;
import org.fluxtream.core.domain.AbstractFacet;

import javax.persistence.Entity;

/**
 * Created by candide on 09/02/15.
 */
@Entity(name="Facet_MisfitSleep")
@ObjectTypeSpec(name = "sleep", value = 4, prettyname = "Sleep", isDateBased = true)
public class MisfitSleepFacet extends AbstractFacet {

    public MisfitSleepFacet() {}

    @Override
    protected void makeFullTextIndexable() {

    }

}
