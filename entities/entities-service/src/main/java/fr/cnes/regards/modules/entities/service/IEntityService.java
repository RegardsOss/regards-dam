/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import java.util.Set;

import javax.persistence.Entity;

import org.springframework.validation.Errors;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.DataSet;
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
     *            one of {@link AbstractEntity} instanciable class which ipId is to be added into the Set of Tags of the
     *            targets
     * @param pTargetsUrn
     *            {@link Set} of {@link UniformResourceName} to identify the {@link AbstractEntity} that should be
     *            linked to pSource
     * @return Updated pSource (tag of all targets has been added to pSource)
     */
    <T extends AbstractEntity> T associate(T pSource, Set<UniformResourceName> pTargetsUrn);

    /**
     * handle dissociation of source from a set of targets represented by their ipIds
     *
     * @param pSource
     *            {@link DataSet} which ipId is to be added into the Set of Tags of the targets
     * @param pTargetsUrn
     *            {@link Set} of {@link UniformResourceName} to identify the {@link AbstractEntity} that should be
     *            linked to pSource
     * @return Updated pSource (tag of all targets has been removed from pSource)
     */
    <T extends AbstractEntity> T dissociate(T pSource, Set<UniformResourceName> pTargetsUrn);

    /**
     *
     * method responsible for checking if every linked {@link Entity} are already present in database
     */
    void checkLinkedEntity(AbstractEntity pEntity) throws EntityNotFoundException;

}
