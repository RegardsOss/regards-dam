/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.adapters.gson;

import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.models.domain.EntityType;

/**
 * @author Marc Sordi
 *
 */
public class Car extends AbstractEntity {

    public Car() {
        super(null, null, null);
    }

    @Override
    public String getType() {
        return EntityType.DATA.toString();
    }
}
