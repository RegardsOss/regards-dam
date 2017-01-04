/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import java.util.Set;

import org.springframework.validation.Errors;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

/**
 * Entity common services
 *
 * @author Marc Sordi
 * @author Sylvain Vissiere-Guerinet
 */
public interface IEntityService {

    void validate(AbstractEntity pAbstractEntity, Errors pErrors, boolean pManageAlterable) throws ModuleException;

    /**
     * handle association of source to a set of targets represented by their ipIds
     *
     * @param pSource
     *            {@link Collection} which ipId is to be added into the Set of Tags of the targets
     * @param pTargetsUrn
     *            {@link Set} of {@link UniformResourceName} to identify the {@link AbstractEntity} that should be
     *            linked to pSource
     * @return Updated pSource
     */
    AbstractEntity associate(Collection pSource, Set<UniformResourceName> pTargetsUrn);
}
