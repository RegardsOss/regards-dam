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

import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.DataType;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.oais.urn.OAISIdentifier;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.framework.utils.file.ChecksumUtils;
import fr.cnes.regards.framework.utils.plugins.PluginParametersFactory;
import fr.cnes.regards.framework.utils.plugins.PluginUtils;
import fr.cnes.regards.modules.dam.dao.entities.EntitySpecifications;
import fr.cnes.regards.modules.dam.dao.entities.IAbstractEntityRepository;
import fr.cnes.regards.modules.dam.dao.entities.ICollectionRepository;
import fr.cnes.regards.modules.dam.dao.entities.IDatasetRepository;
import fr.cnes.regards.modules.dam.dao.entities.IDeletedEntityRepository;
import fr.cnes.regards.modules.dam.domain.entities.AbstractEntity;
import fr.cnes.regards.modules.dam.domain.entities.Collection;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.dam.domain.entities.DeletedEntity;
import fr.cnes.regards.modules.dam.domain.entities.EntityAipState;
import fr.cnes.regards.modules.dam.domain.entities.attribute.AbstractAttribute;
import fr.cnes.regards.modules.dam.domain.entities.attribute.ObjectAttribute;
import fr.cnes.regards.modules.dam.domain.entities.event.BroadcastEntityEvent;
import fr.cnes.regards.modules.dam.domain.entities.event.DatasetEvent;
import fr.cnes.regards.modules.dam.domain.entities.event.EventType;
import fr.cnes.regards.modules.dam.domain.entities.event.NotDatasetEntityEvent;
import fr.cnes.regards.modules.dam.domain.models.Model;
import fr.cnes.regards.modules.dam.domain.models.ModelAttrAssoc;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModel;
import fr.cnes.regards.modules.dam.domain.models.attributes.Fragment;
import fr.cnes.regards.modules.dam.service.entities.exception.InvalidFileLocation;
import fr.cnes.regards.modules.dam.service.entities.validator.AttributeTypeValidator;
import fr.cnes.regards.modules.dam.service.entities.validator.ComputationModeValidator;
import fr.cnes.regards.modules.dam.service.entities.validator.NotAlterableAttributeValidator;
import fr.cnes.regards.modules.dam.service.entities.validator.restriction.RestrictionValidatorFactory;
import fr.cnes.regards.modules.dam.service.models.IModelAttrAssocService;
import fr.cnes.regards.modules.dam.service.models.IModelService;
import fr.cnes.regards.modules.indexer.domain.DataFile;

/**
 * Abstract parameterized entity service
 * @param <U> Entity type
 * @author oroussel
 */
public abstract class AbstractEntityService<U extends AbstractEntity<?>> extends AbstractValidationService<U>
        implements IEntityService<U> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEntityService.class);

    /**
     * {@link IModelService} instance
     */
    protected final IModelService modelService;

    @Autowired
    private ILocalStorageService localStorageService;

    /**
     * Parameterized entity repository
     */
    protected final IAbstractEntityRepository<U> repository;

    /**
     * Unparameterized entity repository
     */
    protected final IAbstractEntityRepository<AbstractEntity<?>> entityRepository;

    /**
     * Collection repository
     */
    protected final ICollectionRepository collectionRepository;

    /**
     * Dataset repository
     */
    protected final IDatasetRepository datasetRepository;

    private final IDeletedEntityRepository deletedEntityRepository;

    private final EntityManager em;

    /**
     * {@link IPublisher} instance
     */
    private final IPublisher publisher;

    /**
     * {@link IRuntimeTenantResolver} instance
     */
    private final IRuntimeTenantResolver runtimeTenantResolver;

    /**
     * If true the AIP entities are send to Storage module to be stored
     */
    @Value("${regards.dam.post.aip.entities.to.storage:true}")
    private Boolean postAipEntitiesToStorage;

    /**
     * The plugin's class name of type {@link IStorageService} used to store AIP entities
     */
    @Value("${regards.dam.post.aip.entities.to.storage.plugins:fr.cnes.regards.modules.dam.service.entities.plugins.AipStoragePlugin}")
    private String postAipEntitiesToStoragePlugin;

    public AbstractEntityService(IModelAttrAssocService modelAttrAssocService,
            IAbstractEntityRepository<AbstractEntity<?>> entityRepository, IModelService modelService,
            IDeletedEntityRepository deletedEntityRepository, ICollectionRepository collectionRepository,
            IDatasetRepository datasetRepository, IAbstractEntityRepository<U> repository, EntityManager em,
            IPublisher publisher, IRuntimeTenantResolver runtimeTenantResolver) {
        super(modelAttrAssocService);
        this.entityRepository = entityRepository;
        this.modelService = modelService;
        this.deletedEntityRepository = deletedEntityRepository;
        this.collectionRepository = collectionRepository;
        this.datasetRepository = datasetRepository;
        this.repository = repository;
        this.em = em;
        this.publisher = publisher;
        this.runtimeTenantResolver = runtimeTenantResolver;
    }

    @Override
    public U load(UniformResourceName ipId) throws ModuleException {
        U entity = repository.findOneByIpId(ipId);
        if (entity == null) {
            throw new EntityNotFoundException(ipId.toString(), this.getClass());
        }
        return entity;
    }

    @Override
    public U load(Long id) throws ModuleException {
        Assert.notNull(id, "Entity identifier is required");
        U entity = repository.findById(id);
        if (entity == null) {
            throw new EntityNotFoundException(id, this.getClass());
        }
        return entity;
    }

    @Override
    public U loadWithRelations(UniformResourceName ipId) throws ModuleException {
        U entity = repository.findByIpId(ipId);
        if (entity == null) {
            throw new EntityNotFoundException(ipId.toString(), this.getClass());
        }
        return entity;
    }

    @Override
    public List<U> loadAllWithRelations(UniformResourceName... ipIds) {
        return repository.findByIpIdIn(ImmutableSet.copyOf(ipIds));
    }

    @Override
    public Page<U> findAll(Pageable pageRequest) {
        return repository.findAll(pageRequest);
    }

    @Override
    public Set<U> findAllByProviderId(String providerId) {
        return repository.findAllByProviderId(providerId);
    }

    @Override
    public Page<U> search(String label, Pageable pageRequest) {
        EntitySpecifications<U> spec = new EntitySpecifications<>();
        return repository.findAll(spec.search(label), pageRequest);
    }

    @Override
    public List<U> findAll() {
        return repository.findAll();
    }

    /**
     * Check if model is loaded else load it then set it on entity.
     * @param entity concerned entity
     */
    @Override
    public void checkAndOrSetModel(U entity) throws ModuleException {
        Model model = entity.getModel();
        // Load model by name if id not specified
        if ((model.getId() == null) && (model.getName() != null)) {
            model = modelService.getModelByName(model.getName());
            entity.setModel(model);
        }
    }

    /**
     * Compute available validators
     * @param modelAttribute {@link ModelAttrAssoc}
     * @param attributeKey attribute key
     * @param manageAlterable manage update or not
     * @return {@link Validator} list
     */
    @Override
    protected List<Validator> getValidators(ModelAttrAssoc modelAttribute, String attributeKey, boolean manageAlterable,
            AbstractEntity<?> entity) {

        AttributeModel attModel = modelAttribute.getAttribute();

        List<Validator> validators = new ArrayList<>();
        // Check computation mode
        validators.add(new ComputationModeValidator(modelAttribute.getMode(), attributeKey));
        // Check alterable attribute
        // Update mode only :
        if (manageAlterable && !attModel.isAlterable()) {
            // lets retrieve the value of the property from db and check if its the same value.
            AbstractEntity<?> fromDb = entityRepository.findByIpId(entity.getIpId());
            AbstractAttribute<?> valueFromDb = extractProperty(fromDb, attModel);
            AbstractAttribute<?> valueFromEntity = extractProperty(entity, attModel);
            // retrieve entity from db, and then update the new one, but i do not have the entity here....
            validators.add(new NotAlterableAttributeValidator(attributeKey, attModel, valueFromDb, valueFromEntity));
        }
        // Check attribute type
        validators.add(new AttributeTypeValidator(attModel.getType(), attributeKey));
        // Check restriction
        if (attModel.hasRestriction()) {
            validators.add(RestrictionValidatorFactory.getValidator(attModel.getRestriction(), attributeKey));
        }
        return validators;
    }

    protected AbstractAttribute<?> extractProperty(AbstractEntity<?> entity, AttributeModel attribute) {
        Fragment fragment = attribute.getFragment();
        String attName = attribute.getName();
        String attPath = fragment.isDefaultFragment() ? attName : fragment.getName() + "." + attName;
        return entity.getProperty(attPath);
    }

    /**
     * Build real attribute map extracting namespace from {@link ObjectAttribute} (i.e. fragment name)
     * @param attMap Map to build
     * @param namespace namespace context
     * @param attributes {@link AbstractAttribute} list to analyze
     */
    protected void buildAttributeMap(Map<String, AbstractAttribute<?>> attMap, String namespace,
            Set<AbstractAttribute<?>> attributes) {
        if (attributes != null) {
            for (AbstractAttribute<?> att : attributes) {
                // Compute value
                if (ObjectAttribute.class.equals(att.getClass())) {
                    ObjectAttribute o = (ObjectAttribute) att;
                    buildAttributeMap(attMap, att.getName(), o.getValue());
                } else {
                    // Compute key
                    String key = att.getName();
                    if (!namespace.equals(Fragment.getDefaultName())) {
                        key = namespace.concat(".").concat(key);
                    }
                    logger.debug(String.format("Key \"%s\" -> \"%s\".", key, att.toString()));
                    attMap.put(key, att);
                }
            }
        }
    }

    /**
     * @param pEntityId an AbstractEntity identifier
     * @param ipIds UniformResourceName Set representing AbstractEntity to be associated to pCollection
     */
    @Override
    public void associate(Long pEntityId, Set<UniformResourceName> ipIds) throws EntityNotFoundException {
        final U entity = repository.findById(pEntityId);
        if (entity == null) {
            throw new EntityNotFoundException(pEntityId, this.getClass());
        }
        // Adding new tags to detached entity
        em.detach(entity);
        ipIds.forEach(ipId -> entity.addTags(ipId.toString()));
        final U entityInDb = repository.findById(pEntityId);
        // And detach it because it is the other one that will be persisted
        em.detach(entityInDb);
        this.updateWithoutCheck(entity, entityInDb);
    }

    @Override
    public U create(U inEntity) throws ModuleException {
        U entity = checkCreation(inEntity);

        // Set IpId
        if (entity.getIpId() == null) {
            entity.setIpId(new UniformResourceName(OAISIdentifier.AIP, EntityType.valueOf(entity.getType()),
                    runtimeTenantResolver.getTenant(), UUID.randomUUID(), 1));
        }

        // IpIds of entities that will need an AMQP event publishing
        Set<UniformResourceName> updatedIpIds = new HashSet<>();
        entity.setCreationDate(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC));
        this.manageGroups(entity, updatedIpIds);

        entity.setStateAip(EntityAipState.AIP_TO_CREATE);

        entity = repository.save(entity);
        updatedIpIds.add(entity.getIpId());

        // AMQP event publishing
        publishEvents(EventType.CREATE, updatedIpIds);
        return entity;
    }

    @Override
    public void dissociate(Long entityId, Set<UniformResourceName> ipIds) throws EntityNotFoundException {
        final U entity = repository.findById(entityId);
        if (entity == null) {
            throw new EntityNotFoundException(entityId, this.getClass());
        }
        // Removing tags to detached entity
        em.detach(entity);
        entity.removeTags(ipIds.stream().map(UniformResourceName::toString).collect(Collectors.toSet()));
        final U entityInDb = repository.findById(entityId);
        // And detach it too because it is the other one that will be persisted
        em.detach(entityInDb);
        this.updateWithoutCheck(entity, entityInDb);
    }

    /**
     * Publish events to AMQP, one event by IpId
     * @param eventType event type (CREATE, DELETE, ...)
     * @param ipIds ipId URNs of entities that need an Event publication onto AMQP
     */
    private void publishEvents(EventType eventType, Set<UniformResourceName> ipIds) {
        UniformResourceName[] datasetsIpIds = ipIds.stream().filter(ipId -> ipId.getEntityType() == EntityType.DATASET)
                .toArray(n -> new UniformResourceName[n]);
        if (datasetsIpIds.length > 0) {
            publisher.publish(new DatasetEvent(datasetsIpIds));
        }
        UniformResourceName[] notDatasetsIpIds = ipIds.stream()
                .filter(ipId -> ipId.getEntityType() != EntityType.DATASET).toArray(n -> new UniformResourceName[n]);
        if (notDatasetsIpIds.length > 0) {
            publisher.publish(new NotDatasetEntityEvent(notDatasetsIpIds));
        }
        publisher.publish(new BroadcastEntityEvent(eventType, ipIds.toArray(new UniformResourceName[ipIds.size()])));
    }

    /**
     * If entity is a collection or a dataset, recursively follow tags to add entity groups, then, if entity is a
     * collection, retrieve and add all groups from collections and datasets tagging this entity
     * @param entity entity to manage the add of groups
     */
    private <T extends AbstractEntity<?>> void manageGroups(final T entity, Set<UniformResourceName> updatedIpIds) {
        // Search Datasets and collections which tag this entity (if entity is a collection)
        if (entity instanceof Collection) {
            List<AbstractEntity<?>> taggingEntities = entityRepository.findByTags(entity.getIpId().toString());
            for (AbstractEntity<?> e : taggingEntities) {
                if ((e instanceof Dataset) || (e instanceof Collection)) {
                    entity.getGroups().addAll(e.getGroups());
                }
            }
        }

        // If entity is a collection or a dataset => propagate its groups to tagged collections (recursively)
        if (((entity instanceof Collection) || (entity instanceof Dataset)) && !entity.getTags().isEmpty()) {
            List<AbstractEntity<?>> taggedColls = entityRepository
                    .findByIpIdIn(extractUrnsOfType(entity.getTags(), EntityType.COLLECTION));
            for (AbstractEntity<?> coll : taggedColls) {
                if (coll.getGroups().addAll(entity.getGroups())) {
                    // If collection has already been updated, stop recursion !!! (else StackOverflow)
                    updatedIpIds.add(coll.getIpId());
                    this.manageGroups(coll, updatedIpIds);
                }
            }
        }
        entityRepository.save(entity);
    }

    private U checkCreation(U pEntity) throws ModuleException {
        checkModelExists(pEntity);
        doCheck(pEntity, null);
        return pEntity;
    }

    /**
     * Specific check depending on entity type
     */
    protected void doCheck(U pEntity, U entityInDB) throws ModuleException {
        // nothing by default
    }

    /**
     * checks if the entity requested exists and that it is modified according to one of it's former version( pEntity's
     * id is pEntityId)
     * @return current entity
     * @throws ModuleException thrown if the entity cannot be found or if entities' id do not match
     */
    private U checkUpdate(Long pEntityId, U pEntity) throws ModuleException {
        U entityInDb = repository.findById(pEntityId);
        em.detach(entityInDb);
        if ((entityInDb == null) || !entityInDb.getClass().equals(pEntity.getClass())) {
            throw new EntityNotFoundException(pEntityId, this.getClass());
        }
        if (!pEntityId.equals(pEntity.getId())) {
            throw new EntityInconsistentIdentifierException(pEntityId, pEntity.getId(), pEntity.getClass());
        }
        doCheck(pEntity, entityInDb);
        return entityInDb;
    }

    @Override
    public U update(Long pEntityId, U pEntity) throws ModuleException {
        // checks
        U entityInDb = checkUpdate(pEntityId, pEntity);
        return updateWithoutCheck(pEntity, entityInDb);
    }

    @Override
    public U update(UniformResourceName pEntityUrn, U pEntity) throws ModuleException {
        U entityInDb = repository.findOneByIpId(pEntityUrn);
        if (entityInDb == null) {
            throw new EntityNotFoundException(pEntity.getIpId().toString());
        }
        pEntity.setId(entityInDb.getId());
        // checks
        entityInDb = checkUpdate(entityInDb.getId(), pEntity);
        return updateWithoutCheck(pEntity, entityInDb);
    }

    /**
     * Really do the update of entities
     * @param entity updated entity to be saved
     * @param entityInDb only there for comparison for group management
     * @return updated entity with group set correclty
     */
    private U updateWithoutCheck(U entity, U entityInDb) {
        Set<UniformResourceName> oldLinks = extractUrns(entityInDb.getTags());
        Set<UniformResourceName> newLinks = extractUrns(entity.getTags());
        Set<String> oldGroups = entityInDb.getGroups();
        Set<String> newGroups = entity.getGroups();
        // IpId URNs of updated entities (those which need an AMQP event publish)
        Set<UniformResourceName> updatedIpIds = new HashSet<>();
        // Update entity, checks already assures us that everything which is updated can be updated so we can just put
        // pEntity into the DB.
        entity.setLastUpdate(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC));

        if (entityInDb.getStateAip() == null) {
            entity.setStateAip(EntityAipState.AIP_TO_CREATE);
        } else if (!entityInDb.getStateAip().equals(EntityAipState.AIP_TO_CREATE)) {
            entity.setStateAip(EntityAipState.AIP_TO_UPDATE);
        }

        U updated = repository.save(entity);

        updatedIpIds.add(updated.getIpId());
        // Compute tags to remove and tags to add
        if (!oldLinks.equals(newLinks) || !oldGroups.equals(newGroups)) {
            Set<UniformResourceName> tagsToRemove = getDiff(oldLinks, newLinks);
            // For all previously tagged entities, retrieve all groups...
            Set<String> groupsToRemove = new HashSet<>();
            List<AbstractEntity<?>> taggedEntitiesWithGroupsToRemove = entityRepository.findByIpIdIn(tagsToRemove);
            taggedEntitiesWithGroupsToRemove.forEach(e -> groupsToRemove.addAll(e.getGroups()));
            // ... delete all these groups on all collections...
            for (String group : groupsToRemove) {
                List<Collection> collectionsWithGroup = collectionRepository.findByGroups(group);
                collectionsWithGroup.forEach(c -> c.getGroups().remove(group));
                collectionsWithGroup.forEach(collectionRepository::save);
                // Add collections to IpIds to be published on AMQP
                collectionsWithGroup.forEach(c -> updatedIpIds.add(c.getIpId()));
                // ... then manage concerned groups on all datasets containing them
                List<Dataset> datasetsWithGroup = datasetRepository.findByGroups(group);
                datasetsWithGroup.forEach(ds -> this.manageGroups(ds, updatedIpIds));
                datasetsWithGroup.forEach(datasetRepository::save);
                // Add datasets to IpIds to be published on AMQP
                datasetsWithGroup.forEach(ds -> updatedIpIds.add(ds.getIpId()));
            }
            // Don't forget to manage groups for current entity too
            this.manageGroups(updated, updatedIpIds);
        }

        // AMQP event publishing
        publishEvents(EventType.UPDATE, updatedIpIds);
        return updated;
    }

    @Override
    public U save(U entity) {
        return repository.save(entity);
    }

    @Override
    public U delete(Long pEntityId) throws ModuleException {
        Assert.notNull(pEntityId, "Entity identifier is required");
        U toDelete = load(pEntityId);
        return delete(toDelete);
    }

    private U delete(U toDelete) throws ModuleException {
        UniformResourceName urn = toDelete.getIpId();
        // IpId URNs that will need an AMQP event publishing
        Set<UniformResourceName> updatedIpIds = new HashSet<>();
        // Manage tags (must be done before group managing to avoid bad propagation)
        // Retrieve all entities tagging the one to delete
        final List<AbstractEntity<?>> taggingEntities = entityRepository.findByTags(urn.toString());
        // Manage tags
        for (AbstractEntity<?> taggingEntity : taggingEntities) {
            // remove tag to ipId
            taggingEntity.removeTags(Arrays.asList(urn.toString()));
        }
        // Save all these tagging entities
        entityRepository.save(taggingEntities);
        taggingEntities.forEach(e -> updatedIpIds.add(e.getIpId()));

        // datasets that contain one of the entity groups
        Set<Dataset> datasets = new HashSet<>();
        // If entity contains groups => update all entities tagging this entity (recursively)
        // Need to manage groups one by one
        for (String group : ((AbstractEntity<?>) toDelete).getGroups()) {
            // Find all collections containing group.
            List<Collection> collectionsWithGroup = collectionRepository.findByGroups(group);
            // Remove group from collections groups
            collectionsWithGroup.stream().filter(c -> !c.equals(toDelete)).forEach(c -> c.getGroups().remove(group));
            // Find all datasets containing this group (to rebuild groups propagation later)
            datasets.addAll(datasetRepository.findByGroups(group));
        }
        // Remove dataset to delete from datasets (no need to manage its groups)
        datasets.remove(toDelete);
        // Remove relate files
        for (Map.Entry<DataType, DataFile> entry : toDelete.getFiles().entries()) {
            if (localStorageService.isFileLocallyStored(toDelete, entry.getValue())) {
                localStorageService.removeFile(toDelete, entry.getValue());
            }
        }
        // Delete the entity
        entityRepository.delete(toDelete);
        updatedIpIds.add(toDelete.getIpId());
        // Manage all impacted datasets groups from scratch
        datasets.forEach(ds -> this.manageGroups(ds, updatedIpIds));

        deleteAipStorage(toDelete);

        deletedEntityRepository.save(createDeletedEntity(toDelete));

        // Publish events to AMQP
        publishEvents(EventType.DELETE, updatedIpIds);

        return toDelete;
    }

    /**
     * @param pSource Set of UniformResourceName
     * @param pOther Set of UniformResourceName to remove from pSource
     * @return a new Set of UniformResourceName containing only the elements present into pSource and not in pOther
     */
    private Set<UniformResourceName> getDiff(Set<UniformResourceName> pSource, Set<UniformResourceName> pOther) {
        final Set<UniformResourceName> result = new HashSet<>();
        result.addAll(pSource);
        result.removeAll(pOther);
        return result;
    }

    public void checkModelExists(AbstractEntity<?> entity) throws ModuleException {
        // model must exist : EntityNotFoundException thrown if not
        modelService.getModel(entity.getModel().getId());
    }

    private static Set<UniformResourceName> extractUrns(Set<String> tags) {
        return tags.stream().filter(UniformResourceName::isValidUrn).map(UniformResourceName::fromString)
                .collect(Collectors.toSet());
    }

    private static Set<UniformResourceName> extractUrnsOfType(Set<String> tags, EntityType entityType) {
        return tags.stream().filter(UniformResourceName::isValidUrn).map(UniformResourceName::fromString)
                .filter(urn -> urn.getEntityType() == entityType).collect(Collectors.toSet());
    }

    private static DeletedEntity createDeletedEntity(AbstractEntity<?> entity) {
        DeletedEntity delEntity = new DeletedEntity();
        delEntity.setCreationDate(entity.getCreationDate());
        delEntity.setDeletionDate(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC));
        delEntity.setIpId(entity.getIpId());
        delEntity.setLastUpdate(entity.getLastUpdate());
        return delEntity;
    }

    @Override
    public U attachFiles(UniformResourceName urn, DataType dataType, MultipartFile[] attachments, List<DataFile> refs,
            String fileUriTemplate) throws ModuleException {

        U entity = loadWithRelations(urn);
        // Store files locally
        java.util.Collection<DataFile> files = localStorageService.attachFiles(entity, dataType, attachments,
                                                                               fileUriTemplate);
        // Merge previous files with new ones
        if (entity.getFiles().get(dataType) != null) {
            entity.getFiles().get(dataType).addAll(files);
        } else {
            entity.getFiles().putAll(dataType, files);
        }

        // Merge references
        if (refs != null) {
            for (DataFile ref : refs) {
                // Same logic as for normal file is applied to check format support
                ContentTypeValidator.supportsForReference(dataType, ref.getFilename(), ref.getMimeType().toString());
                // Compute checksum on URI for removal
                try {
                    ref.setChecksum(ChecksumUtils.computeHexChecksum(ref.getUri(),
                                                                     LocalStorageService.DIGEST_ALGORITHM));
                    ref.setDigestAlgorithm(LocalStorageService.DIGEST_ALGORITHM);
                } catch (NoSuchAlgorithmException | IOException e) {
                    String message = String.format("Error while computing checksum");
                    LOGGER.error(message, e);
                    throw new ModuleException(message, e);
                }
                if (entity.getFiles().get(dataType) != null) {
                    entity.getFiles().get(dataType).add(ref);
                } else {
                    entity.getFiles().put(dataType, ref);
                }
            }
        }

        return update(entity);
    }

    @Override
    public DataFile getFile(UniformResourceName urn, String checksum) throws ModuleException {

        U entity = load(urn);
        // Search data file
        Multimap<DataType, DataFile> files = entity.getFiles();
        for (Map.Entry<DataType, DataFile> entry : files.entries()) {
            if (checksum.equals(entry.getValue().getChecksum())) {
                return entry.getValue();
            }
        }

        String message = String.format("Data file with checksum \"%s\" in entity \"\" not found", checksum,
                                       urn.toString());
        LOGGER.error(message);
        throw new EntityNotFoundException(message);
    }

    @Override
    public void downloadFile(UniformResourceName urn, String checksum, OutputStream output) throws ModuleException {

        U entity = load(urn);
        // Retrieve data file
        DataFile dataFile = getFile(urn, checksum);
        if (localStorageService.isFileLocallyStored(entity, dataFile)) {
            localStorageService.getFileContent(checksum, output);
        } else {
            throw new InvalidFileLocation(dataFile.getFilename());
        }
    }

    @Override
    public U removeFile(UniformResourceName urn, String checksum) throws ModuleException {

        U entity = load(urn);
        // Retrieve data file
        DataFile dataFile = getFile(urn, checksum);
        // Try to remove the file if locally stored, otherwise the file is not stored on this microservice
        if (localStorageService.isFileLocallyStored(entity, dataFile)) {
            localStorageService.removeFile(entity, dataFile);
        }
        entity.getFiles().get(dataFile.getDataType()).remove(dataFile);
        return update(entity);
    }

    /**
     * @return a {@link Plugin} implementation of {@link IStorageService}
     */
    private IStorageService getStorageService() {
        if (postAipEntitiesToStorage == null) {
            return null;
        }

        List<PluginParameter> parameters = PluginParametersFactory.build().getParameters();
        Class<?> ttt;
        try {
            ttt = Class.forName(postAipEntitiesToStoragePlugin);
            return (IStorageService) PluginUtils.getPlugin(parameters, ttt, Arrays.asList(ttt.getPackage().getName()),
                                                           new HashMap<>());
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage());
        }

        return null;
    }

    private void deleteAipStorage(U entity) {
        if ((postAipEntitiesToStorage == null) || !postAipEntitiesToStorage) {
            return;
        }

        IStorageService storageService = getStorageService();

        if (storageService == null) {
            return;
        }

        getStorageService().deleteAIP(entity);
    }
}