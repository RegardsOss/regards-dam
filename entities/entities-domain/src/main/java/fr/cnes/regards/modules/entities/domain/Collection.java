/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.util.UUID;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import fr.cnes.regards.modules.entities.urn.OAISIdentifier;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

/**
 *
 * @author Léo Mieulet
 * @author Sylvain Vissiere-Guerinet
 * @author oroussel
 */
@Entity
@DiscriminatorValue("COLLECTION")
public class Collection extends AbstractDescEntity { // NOSONAR

    public Collection(final Model pModel, final String pTenant, final String pLabel) {
        super(pModel, new UniformResourceName(OAISIdentifier.AIP, EntityType.COLLECTION, pTenant, UUID.randomUUID(), 1),
              pLabel);
    }

    public Collection() {
        // we use super and not this because at deserialization we need a ipId null at the object creation which is then
        // replaced by the attribute if present or added by creation method
        super(null, null, null);
    }

    @Override
    public String getType() {
        return EntityType.COLLECTION.toString();
    }
}
