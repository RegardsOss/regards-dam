/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.util.UUID;

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
public class Collection extends AbstractDescEntity { // NOSONAR

    public Collection(Model pModel, String pTenant, String pLabel) {
        super(pModel, new UniformResourceName(OAISIdentifier.AIP, EntityType.COLLECTION, pTenant, UUID.randomUUID(), 1),
              pLabel);
    }

    public Collection() {
        this(null, null, null);
    }

    @Override
    public String getType() {
        return EntityType.COLLECTION.toString();
    }
}
