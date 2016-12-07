/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;

/**
 * Entity common services
 *
 * @author Marc Sordi
 *
 */
public interface IEntityService {

    boolean validate(AbstractEntity pAbstractEntity) throws ModuleException;
}
