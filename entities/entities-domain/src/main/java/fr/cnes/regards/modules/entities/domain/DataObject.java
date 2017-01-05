/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.modules.models.domain.EntityType;

/**
 * @author lmieulet
 *
 */
public class DataObject extends AbstractDataEntity implements IIdentifiable<Long> {

    /**
     *
     */
    public DataObject() {
        super(EntityType.DATA);
    }

}
