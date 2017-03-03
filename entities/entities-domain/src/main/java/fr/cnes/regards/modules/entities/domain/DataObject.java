/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.util.UUID;

import fr.cnes.regards.modules.entities.urn.OAISIdentifier;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * @author lmieulet
 * @author Marc Sordi
 *
 */
public class DataObject extends AbstractDataEntity {

    public DataObject(Model pModel, String pTenant, String pLabel) {
        super(pModel, new UniformResourceName(OAISIdentifier.AIP, EntityType.DATA, pTenant, UUID.randomUUID(), 1),
              pLabel);
    }

    public DataObject() {
        this(null, null, null);
    }

    @Override
    public String getType() {
        return EntityType.DATA.toString();
    }

}
