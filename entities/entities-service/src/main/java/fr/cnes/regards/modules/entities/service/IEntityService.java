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
 * @author oroussel
 */
@Transactional
public interface IEntityService {

    void validate(AbstractEntity pAbstractEntity, Errors pErrors, boolean pManageAlterable) throws ModuleException;

    /**
     * Associate a set of URNs to an entity. Depending on entity types, association results in tags, groups or nothing.
     * @param pEntityId entity source id
     * @param pToAssociates URNs of entities to be associated by source entity
     * @throws EntityNotFoundException
     */
    AbstractEntity associate(Long pEntityId, Set<UniformResourceName> pToAssociates) throws EntityNotFoundException;

    /**
     * Dissociate a set of URNs from an entity. Depending on entity types, dissociation impacts tags, groups or nothing.
     * @param pEntityId entity source id
     * @param pToAssociates URNs of entities to be dissociated from source entity
     * @throws EntityNotFoundException
     */
    AbstractEntity dissociate(Long pEntityId, Set<UniformResourceName> pToBeDissociated) throws EntityNotFoundException;

    /**
     * Create entity
     * @param pEntity entity to create
     * @return updated entity from database
     * @throws ModuleException
     */
    <T extends AbstractEntity> T create(T pEntity) throws ModuleException;

    /**
     * Update entity of id pEntityId according to pEntity
     * @param pEntityId id of entity to update
     * @param pEntity "content" of entity to update
     * @return updated entity from database
     * @throws ModuleException
     */
    <T extends AbstractEntity> T update(Long pEntityId, T pEntity) throws ModuleException;

    /**
     * Update entity of ipId pEntityUrn according to pEntity
     * @param pEntityUrn ipId of entity to update
     * @param pEntity "content" of entity to update
     * @return updated entity from database
     * @throws ModuleException
     */
    <T extends AbstractEntity> T update(UniformResourceName pEntityUrn, T pEntity) throws ModuleException;

    /**
     * Update given entity identified by its id property (ie. getId() method) OR identified by its ipId property
     * if id is null
     * @param pEntity entity to update
     * @return updated entity from database
     * @throws ModuleException
     */
    default <T extends AbstractEntity> T update(T pEntity) throws ModuleException {
        if (pEntity.getId() != null) {
            return this.update(pEntity.getId(), pEntity);
        } else {
            return this.update(pEntity.getIpId(), pEntity);
        }
    }

    /**
     * Delete entity identified by its id.
     * A deleted entity is "logged" into "deleted_entity" table
     * @param pEntityId id of entity to delete
     * @return the deleted entity
     * @throws EntityNotFoundException
     */
    AbstractEntity delete(Long pEntityId) throws EntityNotFoundException;
}