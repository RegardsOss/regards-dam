/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import java.util.Set;

import org.springframework.validation.Errors;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.entities.domain.AbstractDataEntity;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Collection;
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
     *            {@link Collection} which ipId is to be added into the Set of Tags of the targets
     * @param pTargetsUrn
     *            {@link Set} of {@link UniformResourceName} to identify the {@link AbstractEntity} that should be
     *            linked to pSource
     * @return Updated pSource (tag of all targets has been added to pSource)
     */
    Collection associate(Collection pSource, Set<UniformResourceName> pTargetsUrn);

    /**
     * handle association of source to a set of targets represented by their ipIds
     *
     * @param pSource
     *            {@link AbstractDataEntity} which ipId is to be added into the Set of Tags of the targets
     * @param pTargetsUrn
     *            {@link Set} of {@link UniformResourceName} to identify the {@link AbstractEntity} that should be
     *            linked to pSource
     * @return Updated pSource (tag of all targets has been added to pSource)
     */
    AbstractDataEntity associate(AbstractDataEntity pSource, Set<UniformResourceName> pTargetsUrn);

    /**
     * handle association of source to a set of targets represented by their ipIds
     *
     * @param pSource
     *            {@link DataSet} which ipId is to be added into the Set of Tags of the targets
     * @param pTargetsUrn
     *            {@link Set} of {@link UniformResourceName} to identify the {@link AbstractEntity} that should be
     *            linked to pSource
     * @return Updated pSource (tag of all targets has been added to pSource)
     */
    DataSet associate(DataSet pSource, Set<UniformResourceName> pTargetsUrn);

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

}
