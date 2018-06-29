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
package fr.cnes.regards.modules.entities.service;

import java.io.OutputStream;
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
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.DataType;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.oais.urn.OAISIdentifier;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.framework.utils.plugins.PluginParametersFactory;
import fr.cnes.regards.framework.utils.plugins.PluginUtils;
import fr.cnes.regards.modules.entities.dao.EntitySpecifications;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.dao.ICollectionRepository;
import fr.cnes.regards.modules.entities.dao.IDatasetRepository;
import fr.cnes.regards.modules.entities.dao.deleted.IDeletedEntityRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.ObjectAttribute;
import fr.cnes.regards.modules.entities.domain.deleted.DeletedEntity;
import fr.cnes.regards.modules.entities.domain.event.BroadcastEntityEvent;
import fr.cnes.regards.modules.entities.domain.event.DatasetEvent;
import fr.cnes.regards.modules.entities.domain.event.EventType;
import fr.cnes.regards.modules.entities.domain.event.NotDatasetEntityEvent;
import fr.cnes.regards.modules.entities.service.validator.AttributeTypeValidator;
import fr.cnes.regards.modules.entities.service.validator.ComputationModeValidator;
import fr.cnes.regards.modules.entities.service.validator.NotAlterableAttributeValidator;
import fr.cnes.regards.modules.entities.service.validator.restriction.RestrictionValidatorFactory;
import fr.cnes.regards.modules.indexer.domain.DataFile;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;
import fr.cnes.regards.modules.models.service.IModelAttrAssocService;
import fr.cnes.regards.modules.models.service.IModelService;

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
    public Set<U> findAllBySipId(String sipId) {
        return repository.findAllBySipId(sipId);
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
     * @param entity cocnerned entity
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
    public U create(U inEntity, MultipartFile file) throws ModuleException {
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
        entity = repository.save(entity);
        updatedIpIds.add(entity.getIpId());
        entity = getStorageService().storeAIP(entity);
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

    /**
     * TODO make it possible to switch configuration dynamically between local and remote Dynamically get the storage
     * service
     * @return the storage service @
     */
    private IStorageService getStorageService() {
        List<PluginParameter> parameters = PluginParametersFactory.build().getParameters();
        return PluginUtils.getPlugin(parameters, LocalStoragePlugin.class,
                                     Arrays.asList(LocalStoragePlugin.class.getPackage().getName()), new HashMap<>());

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
    public U update(Long pEntityId, U pEntity, MultipartFile file) throws ModuleException {
        // checks
        U entityInDb = checkUpdate(pEntityId, pEntity);
        return updateWithoutCheck(pEntity, entityInDb);
    }

    @Override
    public U update(UniformResourceName pEntityUrn, U pEntity, MultipartFile file) throws ModuleException {
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
        updated = getStorageService().updateAIP(updated);
        // AMQP event publishing
        publishEvents(EventType.UPDATE, updatedIpIds);
        return updated;
    }

    @Override
    public U delete(Long pEntityId) throws ModuleException {
        Assert.notNull(pEntityId, "Entity identifier is required");
        final U toDelete = repository.findById(pEntityId);
        if (toDelete == null) {
            throw new EntityNotFoundException(pEntityId, this.getClass());
        }
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

        deletedEntityRepository.save(createDeletedEntity(toDelete));
        getStorageService().deleteAIP(toDelete);
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
    public U attachFiles(UniformResourceName urn, DataType dataType, MultipartFile[] attachments,
            String fileUriTemplate) throws ModuleException {

        U entity = load(urn);
        // Store files locally
        java.util.Collection<DataFile> files = localStorageService.attachFiles(entity, dataType, attachments,
                                                                               fileUriTemplate);
        // Merge previous files with new ones
        if (entity.getFiles().get(dataType) != null) {
            entity.getFiles().get(dataType).addAll(files);
        } else {
            entity.getFiles().putAll(dataType, files);
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
        localStorageService.getFileContent(checksum, output);
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
}
