/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import java.util.List;
import java.util.Set;

import org.springframework.validation.Errors;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
public interface IEntityService {

    void validate(AbstractEntity pAbstractEntity, Errors pErrors, boolean pManageAlterable) throws ModuleException;

    /**
     * dissociates specified entity from all associated entities
     *
     * @param pToDelete
     */
    void dissociate(AbstractEntity pToDelete);

    <T extends AbstractEntity> T dissociate(T pSource, Set<UniformResourceName> pTargetsUrn);

    <T extends AbstractEntity> T dissociate(T pSource, List<AbstractEntity> pEntityToDissociate);

    <T extends AbstractEntity> T associate(T pSource, Set<UniformResourceName> pTargetsUrn);

    /**
     * @param pEntityId
     *            a {@link AbstractEntity}
     * @param pToAssociate
     *            {@link Set} of {@link UniformResourceName}s representing {@link AbstractEntity} to associate to
     *            pCollection
     * @throws EntityNotFoundException
     */
    AbstractEntity associate(Long pEntityId, Set<UniformResourceName> pToAssociate) throws EntityNotFoundException;

    <T extends AbstractEntity> T associate(T pEntity);

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

    AbstractEntity delete(String pEntityIpId) throws EntityNotFoundException;

    void checkLinkedEntity(AbstractEntity pEntity) throws ModuleException;

}