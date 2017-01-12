/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * @author lmieulet
 * @author Marc Sordi
 *
 */
public class DataObject extends AbstractDataEntity {

    public DataObject(Model pModel, UniformResourceName pIpId, String pLabel) {
        super(pModel, pIpId, pLabel);
    }

    public DataObject() {
        this(null, null, null);
    }

    @Override
    public String getType() {
        return EntityType.DATA.toString();
    }

}
