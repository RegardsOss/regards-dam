/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.validation.Errors;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Transactional
public interface IEntityService {

    void validate(AbstractEntity pAbstractEntity, Errors pErrors, boolean pManageAlterable) throws ModuleException;

    //    <T extends AbstractEntity> T dissociate(T pSource, Set<UniformResourceName> pTargetsUrn);

    //    <T extends AbstractEntity> T dissociate(T pSource, List<AbstractEntity> pEntityToDissociate);

    /**
     * @param pEntityId a {@link AbstractEntity}
     * @param pToAssociate {@link Set} of {@link UniformResourceName}s representing {@link AbstractEntity} to associate to
     *        pCollection
     * @throws EntityNotFoundException
     */
    AbstractEntity associate(Long pEntityId, Set<UniformResourceName> pToAssociate) throws EntityNotFoundException;

    AbstractEntity dissociate(Long pEntityId, Set<UniformResourceName> pToBeDissociated) throws EntityNotFoundException;

    <T extends AbstractEntity> T create(T pEntity) throws ModuleException;

    /**
     * updates entity of id pEntityId according to pEntity
     *
     * @param pEntityId
     * @param pEntity
     * @return updated entity
     * @throws ModuleException
     */
    // FIXME: should i use a clone of the parameter instead of modifying it?
    <T extends AbstractEntity> T update(Long pEntityId, T pEntity) throws ModuleException;

    AbstractEntity delete(Long pEntityId) throws EntityNotFoundException;

    //    AbstractEntity delete(String pEntityIpId) throws EntityNotFoundException;

    void checkModelExists(AbstractEntity pEntity) throws ModuleException;

}