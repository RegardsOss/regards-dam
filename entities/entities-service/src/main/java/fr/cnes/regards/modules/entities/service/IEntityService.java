/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MultipartFile;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.plugins.utils.PluginUtilsException;

/**
 * Parameterized entity service interface
 * @author Sylvain Vissiere-Guerinet
 * @author oroussel
 */
@MultitenantTransactional
public interface IEntityService<U extends AbstractEntity> {

    /**
     * Load entity by IpId without relations
     * @param ipId business id
     * @return entity without its relations (ie. groups, tags, ...) or null if entity doesn't exists
     *
     */
    U load(UniformResourceName ipId);

    /**
     * Load entity by id without relations
     * @param ip Database id
     * @return entity without its relations (ie. groups, tags, ...) or null if entity doesn't exists
     */
    U load(Long id);

    /**
     * Load entity by IpId with all its relations
     * @param ipId business id
     * @return entity with all its relations (ie. groups, tags, ...) or null if entity doesn't exists
     */
    U loadWithRelations(UniformResourceName ipId);

    /**
     * Load entities by IpId with all their relations
     * @param ipIds business ids
     * @return entities with all its relations (ie. groups, tags, ...) or empty list
     */
    List<U> loadAllWithRelations(UniformResourceName... ipIds);

    Page<U> findAll(Pageable pageRequest);

    List<U> findAll();

    void validate(U pAbstractEntity, Errors pErrors, boolean pManageAlterable) throws ModuleException;

    /**
     * Associate a set of URNs to an entity. Depending on entity types, association results in tags, groups or nothing.
     * @param pEntityId entity source id
     * @param pToAssociates URNs of entities to be associated by source entity
     * @throws EntityNotFoundException
     */
    U associate(Long pEntityId, Set<UniformResourceName> pToAssociates) throws EntityNotFoundException;

    /**
     * Dissociate a set of URNs from an entity. Depending on entity types, dissociation impacts tags, groups or nothing.
     * @param pEntityId entity source id
     * @param pToAssociates URNs of entities to be dissociated from source entity
     * @throws EntityNotFoundException
     */
    U dissociate(Long pEntityId, Set<UniformResourceName> pToBeDissociated) throws EntityNotFoundException;

    /**
     * Create entity
     * @param pEntity entity to create
     * @param pFile description file (or null)
     * @return updated entity from database
     * @throws ModuleException
     */
    U create(U pEntity, MultipartFile pFile) throws ModuleException, IOException;

    /**
     * Create entity without description file
     * @param pEntity entioty to create
     * @return updated entity from database
     * @throws ModuleException
     */
    default U create(U pEntity) throws ModuleException, IOException {
        return this.create(pEntity, null);
    }

    /**
     * Update entity of id pEntityId according to pEntity
     * @param pEntityId id of entity to update
     * @param pEntity "content" of entity to update
     * @return updated entity from database
     * @throws ModuleException
     * @throws PluginUtilsException
     */
    U update(Long pEntityId, U pEntity) throws ModuleException;

    /**
     * Update entity of ipId pEntityUrn according to pEntity
     * @param pEntityUrn ipId of entity to update
     * @param pEntity "content" of entity to update
     * @return updated entity from database
     * @throws ModuleException
     */
    U update(UniformResourceName pEntityUrn, U pEntity) throws ModuleException;

    /**
     * Update given entity identified by its id property (ie. getId() method) OR identified by its ipId property
     * if id is null
     * @param pEntity entity to update
     * @return updated entity from database
     * @throws ModuleException
     */
    default U update(U pEntity) throws ModuleException {
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
    U delete(Long pEntityId) throws EntityNotFoundException;
}