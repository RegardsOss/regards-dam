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
 * @author Sylvain Vissiere-Guerinet
 * @author Marc Sordi
 *
 */
@Entity
@DiscriminatorValue("DOCUMENT")
public class Document extends AbstractDataEntity {

    public Document(Model pModel, String pTenant, String pLabel) {
        super(pModel, new UniformResourceName(OAISIdentifier.AIP, EntityType.DOCUMENT, pTenant, UUID.randomUUID(), 1),
              pLabel);
    }

    public Document() {
        this(null, null, null);
    }

    @Override
    public String getType() {
        return EntityType.DOCUMENT.toString();
    }
}
