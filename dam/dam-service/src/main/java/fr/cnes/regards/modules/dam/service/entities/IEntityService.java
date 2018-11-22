/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.dam.service.entities;

import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.oais.urn.DataType;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.modules.dam.domain.entities.AbstractEntity;
import fr.cnes.regards.modules.indexer.domain.DataFile;

/**
 * Parameterized entity service interface
 *
 * @author Sylvain Vissiere-Guerinet
 * @author oroussel
 * @param <U> extends {@link AbstractEntity}
 */
@MultitenantTransactional
public interface IEntityService<U extends AbstractEntity<?>> extends IValidationService<U> {

    /**
     * Load entity by IpId without relations
     *
     * @param ipId business id
     * @return entity without its relations (ie. groups, tags, ...) or null if entity doesn't exists
     * @throws ModuleException
     */
    U load(UniformResourceName ipId) throws ModuleException;

    /**
     * Load entity by id without relations
     *
     * @param id Database id
     * @return entity without its relations (ie. groups, tags, ...) or null if entity doesn't exists
     * @throws ModuleException
     */
    U load(Long id) throws ModuleException;

    /**
     * Load entity by IpId with all its relations
     *
     * @param ipId business id
     * @return entity with all its relations (ie. groups, tags, ...) or null if entity doesn't exists
     * @throws ModuleException
     */
    U loadWithRelations(UniformResourceName ipId) throws ModuleException;

    /**
     * Load entities by IpId with all their relations
     *
     * @param ipIds business ids
     * @return entities with all its relations (ie. groups, tags, ...) or empty list
     * @throws ModuleException
     */
    List<U> loadAllWithRelations(UniformResourceName... ipIds) throws ModuleException;

    Page<U> findAll(Pageable pageRequest);

    List<U> findAll();

    Set<U> findAllByProviderId(String providerId);

    Page<U> search(String label, Pageable pageRequest);

    /**
     * Check if model is loaded else load it then set it on entity.
     * @param entity cocnerned entity
     * @throws ModuleException
     */
    void checkAndOrSetModel(U entity) throws ModuleException;

    /**
     * Associate a set of URNs to an entity. Depending on entity types, association results in tags, groups or nothing.
     *
     * @param entityId entity source id
     * @param toAssociates tags to be associated by source entity (may be entity URNs)
     * @throws EntityNotFoundException
     */
    void associate(Long entityId, Set<String> toAssociates) throws EntityNotFoundException;

    /**
     * Dissociate a set of URNs from an entity. Depending on entity types, dissociation impacts tags, groups or nothing.
     *
     * @param entityId entity source id
     * @param toBeDissociated tags to be dissociated from source entity (may be entity URNs)
     * @throws EntityNotFoundException
     */
    void dissociate(Long entityId, Set<String> toBeDissociated) throws EntityNotFoundException;

    /**
     * Create entity
     *
     * @param entity entity to create
     * @return updated entity from database
     * @throws ModuleException
     */
    U create(U entity) throws ModuleException;

    /**
     * Update entity of id pEntityId according to pEntity
     *
     * @param entityId id of entity to update
     * @param entity "content" of entity to update
     * @return updated entity from database
     * @throws ModuleException
     */
    U update(Long entityId, U entity) throws ModuleException;

    /**
     * Update entity of ipId entityUrn according to pEntity
     *
     * @param entityUrn ipId of entity to update
     * @param entity "content" of entity to update
     * @return updated entity from database
     * @throws ModuleException
     */
    U update(UniformResourceName entityUrn, U entity) throws ModuleException;

    /**
     * Save an entity.
     *
     * @param entity the entity t saved
     * @return the saved entity
     */
    U save(U entity);

    /**
     * Update given entity identified by its id property (ie. getId() method) OR identified by its ipId property if id
     * is null
     *
     * @param entity entity to update
     * @return updated entity from database
     * @throws ModuleException
     */
    default U update(U entity) throws ModuleException {
        if (entity.getId() != null) {
            return this.update(entity.getId(), entity);
        } else {
            return this.update(entity.getIpId(), entity);
        }
    }

    /**
     * Delete entity identified by its id. A deleted entity is "logged" into "deleted_entity" table
     * @param pEntityId
     * @return <U>
     * @throws ModuleException
     */
    U delete(Long pEntityId) throws ModuleException;

    /**
     * Attach files to given entity
     * @param urn  {@link UniformResourceName}
     * @param dataType  {@link DataType}
     * @param attachments {@link MultipartFile}
     * @param refs
     * @param fileUriTemplate
     * @return  {@link AbstractEntity}
     * @throws ModuleException
     *
     */
    AbstractEntity<?> attachFiles(UniformResourceName urn, DataType dataType, MultipartFile[] attachments,
            List<DataFile> refs, String fileUriTemplate) throws ModuleException;

    /**
     * Retrieve a {@link DataFile} attached to the specified entity with the specified checksum
     * @param urn {@link UniformResourceName}
     * @param checksum
     * @return {@link DataFile}
     * @throws ModuleException
     */
    DataFile getFile(UniformResourceName urn, String checksum) throws ModuleException;

    /**
     * Write related file content to output stream.<br/>
     * {@link OutputStream} has to be flush after this method completes.
     * @param urn  {@link UniformResourceName}
     * @param checksum
     * @param output {@link OutputStream}
     * @throws ModuleException
     */
    void downloadFile(UniformResourceName urn, String checksum, OutputStream output) throws ModuleException;

    /**
     * Remove file
     * @param urn {@link UniformResourceName}
     * @param checksum
     * @return {@link AbstractEntity}
     * @throws ModuleException
     */
    AbstractEntity<?> removeFile(UniformResourceName urn, String checksum) throws ModuleException;
}