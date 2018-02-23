/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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

import javax.persistence.EntityManager;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.ImmutableSet;
import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.oais.urn.OAISIdentifier;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.framework.utils.plugins.PluginParametersFactory;
import fr.cnes.regards.framework.utils.plugins.PluginUtils;
import fr.cnes.regards.modules.entities.dao.EntitySpecifications;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.dao.ICollectionRepository;
import fr.cnes.regards.modules.entities.dao.IDatasetRepository;
import fr.cnes.regards.modules.entities.dao.IDescriptionFileRepository;
import fr.cnes.regards.modules.entities.dao.deleted.IDeletedEntityRepository;
import fr.cnes.regards.modules.entities.domain.AbstractDescEntity;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.DescriptionFile;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.ObjectAttribute;
import fr.cnes.regards.modules.entities.domain.deleted.DeletedEntity;
import fr.cnes.regards.modules.entities.domain.event.BroadcastEntityEvent;
import fr.cnes.regards.modules.entities.domain.event.DatasetEvent;
import fr.cnes.regards.modules.entities.domain.event.EventType;
import fr.cnes.regards.modules.entities.domain.event.NotDatasetEntityEvent;
import fr.cnes.regards.modules.entities.service.exception.EntityDescriptionTooLargeException;
import fr.cnes.regards.modules.entities.service.exception.EntityDescriptionUnacceptableCharsetException;
import fr.cnes.regards.modules.entities.service.exception.EntityDescriptionUnacceptableType;
import fr.cnes.regards.modules.entities.service.validator.AttributeTypeValidator;
import fr.cnes.regards.modules.entities.service.validator.ComputationModeValidator;
import fr.cnes.regards.modules.entities.service.validator.NotAlterableAttributeValidator;
import fr.cnes.regards.modules.entities.service.validator.restriction.RestrictionValidatorFactory;
import fr.cnes.regards.modules.models.domain.ComputationMode;
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
public abstract class AbstractEntityService<U extends AbstractEntity> implements IEntityService<U> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Namespace separator
     */
    private static final String NAMESPACE_SEPARATOR = ".";

    /**
     * Max description file acceptable byte size
     */
    private static final int MAX_DESC_FILE_SIZE = 10_000_000;

    /**
     * Attribute model service
     */
    protected final IModelAttrAssocService modelAttributeService;

    /**
     * {@link IModelService} instance
     */
    protected final IModelService modelService;

    /**
     * Parameterized entity repository
     */
    protected final IAbstractEntityRepository<U> repository;

    /**
     * Unparameterized entity repository
     */
    protected final IAbstractEntityRepository<AbstractEntity> entityRepository;

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
     * {@link IDescriptionFileRepository} instance
     */
    protected final IDescriptionFileRepository descriptionFileRepository;

    public AbstractEntityService(IModelAttrAssocService pModelAttributeService,
            IAbstractEntityRepository<AbstractEntity> pEntityRepository, IModelService pModelService,
            IDeletedEntityRepository pDeletedEntityRepository, ICollectionRepository pCollectionRepository,
            IDatasetRepository pDatasetRepository, IAbstractEntityRepository<U> pRepository, EntityManager pEm,
            IPublisher pPublisher, IRuntimeTenantResolver runtimeTenantResolver,
            IDescriptionFileRepository descriptionFileRepository) {
        modelAttributeService = pModelAttributeService;
        entityRepository = pEntityRepository;
        modelService = pModelService;
        deletedEntityRepository = pDeletedEntityRepository;
        collectionRepository = pCollectionRepository;
        datasetRepository = pDatasetRepository;
        repository = pRepository;
        em = pEm;
        publisher = pPublisher;
        this.runtimeTenantResolver = runtimeTenantResolver;
        this.descriptionFileRepository = descriptionFileRepository;
    }

    @Override
    public U load(UniformResourceName ipId) {
        return repository.findOneByIpId(ipId);
    }

    @Override
    public U load(Long id) {
        return repository.findById(id);
    }

    @Override
    public U loadWithRelations(UniformResourceName ipId) {
        return repository.findByIpId(ipId);
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

    @Override
    public void validate(U entity, Errors inErrors, boolean manageAlterable) throws ModuleException {
        Assert.notNull(entity, "Entity must not be null.");

        Model model = entity.getModel();
        // Load model by name if id not specified
        if ((model.getId() == null) && (model.getName() != null)) {
            model = modelService.getModelByName(model.getName());
            entity.setModel(model);
        }

        Assert.notNull(model, "Model must be set on entity in order to be validated.");
        Assert.notNull(model.getId(), "Model identifier must be specified.");

        // Retrieve model attributes
        List<ModelAttrAssoc> modAtts = modelAttributeService.getModelAttrAssocs(model.getName());

        // Check model not empty
        if (((modAtts == null) || modAtts.isEmpty())
                && ((entity.getProperties() != null) && (!entity.getProperties().isEmpty()))) {
            inErrors.rejectValue("properties", "error.no.properties.defined.but.set",
                                 "No properties defined in corresponding model but trying to create.");
        }

        // Prepare attributes for validation check
        Map<String, AbstractAttribute<?>> attMap = new HashMap<>();

        // Build attribute map
        buildAttributeMap(attMap, Fragment.getDefaultName(), entity.getProperties());

        // Loop over model attributes ... to validate each attribute
        for (ModelAttrAssoc modelAtt : modAtts) {
            checkModelAttribute(attMap, modelAtt, inErrors, manageAlterable, entity);
        }

        if (inErrors.hasErrors()) {
            List<String> errors = new ArrayList<>();
            for (ObjectError error : inErrors.getAllErrors()) {
                String errorMessage = error.getDefaultMessage();
                logger.error(errorMessage);
                errors.add(errorMessage);
            }
            throw new EntityInvalidException(errors);
        }
    }

    /**
     * Validate an attribute with its corresponding model attribute
     * @param attMap attribue map
     * @param modelAttribute model attribute
     * @param errors validation errors
     * @param manageAlterable manage update or not
     */
    protected void checkModelAttribute(Map<String, AbstractAttribute<?>> attMap, ModelAttrAssoc modelAttribute,
            Errors errors, boolean manageAlterable, AbstractEntity entity) {

        // only validate attribute that have a ComputationMode of GIVEN. Otherwise the attribute will most likely be
        // missing and is added during the crawling process
        if (ComputationMode.GIVEN.equals(modelAttribute.getMode())) {
            AttributeModel attModel = modelAttribute.getAttribute();
            String key = attModel.getName();
            if (!attModel.getFragment().isDefaultFragment()) {
                key = attModel.getFragment().getName().concat(NAMESPACE_SEPARATOR).concat(key);
            }
            logger.debug(String.format("Computed key : \"%s\"", key));

            // Retrieve attribute
            AbstractAttribute<?> att = attMap.get(key);

            // Null value check
            if (att == null) {
                String messageKey = "error.missing.required.attribute.message";
                String defaultMessage = String.format("Missing required attribute \"%s\".", key);
                // if (pManageAlterable && attModel.isAlterable() && !attModel.isOptional()) {
                if (!attModel.isOptional()) {
                    errors.reject(messageKey, defaultMessage);
                    return;
                }
                logger.debug(String.format("Attribute \"%s\" not required in current context.", key));
                return;
            }

            // Do validation
            for (Validator validator : getValidators(modelAttribute, key, manageAlterable, entity)) {
                if (validator.supports(att.getClass())) {
                    validator.validate(att, errors);
                } else {
                    String defaultMessage = String.format("Unsupported validator \"%s\" for attribute \"%s\"",
                                                          validator.getClass().getName(), key);
                    errors.reject("error.unsupported.validator.message", defaultMessage);
                }
            }
        }
    }

    /**
     * Compute available validators
     * @param modelAttribute {@link ModelAttrAssoc}
     * @param attributeKey attribute key
     * @param manageAlterable manage update or not
     * @return {@link Validator} list
     */
    protected List<Validator> getValidators(ModelAttrAssoc modelAttribute, String attributeKey, boolean manageAlterable,
            AbstractEntity entity) {

        AttributeModel attModel = modelAttribute.getAttribute();

        List<Validator> validators = new ArrayList<>();
        // Check computation mode
        validators.add(new ComputationModeValidator(modelAttribute.getMode(), attributeKey));
        // Check alterable attribute
        // Update mode only :
        if (manageAlterable && !attModel.isAlterable()) {
            // lets retrieve the value of the property from db and check if its the same value.
            AbstractEntity fromDb = entityRepository.findByIpId(entity.getIpId());
            Optional<AbstractAttribute<?>> propertyFromDb = extractProperty(fromDb, attModel);
            Optional<AbstractAttribute<?>> property = extractProperty(entity, attModel);
            // retrieve entity from db, and then update the new one, but i do not have the entity here....
            validators.add(new NotAlterableAttributeValidator(attributeKey, attModel, propertyFromDb, property));
        }
        // Check attribute type
        validators.add(new AttributeTypeValidator(attModel.getType(), attributeKey));
        // Check restriction
        if (attModel.hasRestriction()) {
            validators.add(RestrictionValidatorFactory.getValidator(attModel.getRestriction(), attributeKey));
        }
        return validators;
    }

    protected Optional<AbstractAttribute<?>> extractProperty(AbstractEntity entity, AttributeModel attribute) { // NOSONAR
        if (attribute.getFragment().isDefaultFragment()) {
            // the attribute is in the default fragment so it has at the root level of properties
            return entity.getProperties().stream().filter(p -> p.getName().equals(attribute.getName())).findFirst();
        }
        // the attribute is in a fragment so :
        // filter the fragment property then filter the right property on fragment properties
        return entity.getProperties().stream()
                .filter(p -> (p instanceof ObjectAttribute) && p.getName().equals(attribute.getFragment().getName()))
                .limit(1) // Only one fragment with searched name
                .flatMap(fragment -> ((ObjectAttribute) fragment).getValue().stream())
                .filter(p -> p.getName().equals(attribute.getName())).findFirst();
    }

    /**
     * Build real attribute map extracting namespace from {@link ObjectAttribute} (i.e. fragment name)
     * @param attMap Map to build
     * @param namespace namespace context
     * @param attributes {@link AbstractAttribute} list to analyze
     */
    protected void buildAttributeMap(Map<String, AbstractAttribute<?>> attMap, String namespace,
            final Set<AbstractAttribute<?>> attributes) {
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
                        key = namespace.concat(NAMESPACE_SEPARATOR).concat(key);
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
        entity.getTags().addAll(ipIds.stream().map(UniformResourceName::toString).collect(Collectors.toSet()));
        final U entityInDb = repository.findById(pEntityId);
        // And detach it because it is the other one that will be persisted
        em.detach(entityInDb);
        this.updateWithoutCheck(entity, entityInDb);
    }

    @Override
    public U create(U inEntity, MultipartFile file) throws ModuleException, IOException {
        U entity = checkCreation(inEntity);

        // Set IpId
        if (entity.getIpId() == null) {
            entity.setIpId(new UniformResourceName(OAISIdentifier.AIP, EntityType.valueOf(entity.getType()),
                    runtimeTenantResolver.getTenant(), UUID.randomUUID(), 1));
        }
        // Set description
        if (entity instanceof AbstractDescEntity) {
            this.setDescription((AbstractDescEntity) entity, file, null);
        }

        // IpIds of entities that will need an AMQP event publishing
        Set<UniformResourceName> updatedIpIds = new HashSet<>();
        this.manageGroups(entity, updatedIpIds);
        entity.setCreationDate(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC));
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
        entity.getTags().removeAll(ipIds.stream().map(UniformResourceName::toString).collect(Collectors.toSet()));
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
    private <T extends AbstractEntity> void manageGroups(final T entity, Set<UniformResourceName> updatedIpIds) {
        /*
         * // If entity tags entities => retrieve all groups of tagged entities (only for collection)
         * if ((entity instanceof Collection) && !entity.getTags().isEmpty()) {
         * List<AbstractEntity> taggedEntities = entityRepository.findByIpIdIn(extractUrns(entity.getTags()));
         * final T finalEntity = entity;
         * taggedEntities.forEach(e -> finalEntity.getGroups().addAll(e.getGroups()));
         * updatedIpIds.add(finalEntity.getIpId());
         * }
         * UniformResourceName urn = entity.getIpId();
         * // If entity contains groups => update all entities tagging this entity (recursively)
         * // Need to manage groups one by one
         * for (String group : entity.getGroups()) {
         * Set<Collection> collectionsToUpdate = new HashSet<>();
         * // Find all collections tagging this entity and try adding group
         * manageGroup(group, collectionsToUpdate, urn, entity, updatedIpIds);
         * // Recursively continue to collections tagging updated collections and so on until no more collections
         * // has to be updated
         * while (!collectionsToUpdate.isEmpty()) {
         * Collection firstColl = collectionsToUpdate.iterator().next();
         * manageGroup(group, collectionsToUpdate, firstColl.getIpId(), entity, updatedIpIds);
         * collectionsToUpdate.remove(firstColl);
         * }
         * }
         */

        // Search Datasets and collections which tag this entity (if entity is a collection)
        if (entity instanceof Collection) {
            List<AbstractEntity> taggingEntities = entityRepository.findByTags(entity.getIpId().toString());
            for (AbstractEntity e : taggingEntities) {
                if ((e instanceof Dataset) || (e instanceof Collection)) {
                    entity.getGroups().addAll(e.getGroups());
                }
            }
        }

        // If entity is a collection or a dataset => propagate its groups to tagged collections (recursively)
        if (((entity instanceof Collection) || (entity instanceof Dataset)) && !entity.getTags().isEmpty()) {
            List<AbstractEntity> taggedColls = entityRepository
                    .findByIpIdIn(extractUrnsOfType(entity.getTags(), EntityType.COLLECTION));
            for (AbstractEntity coll : taggedColls) {
                coll.getGroups().addAll(entity.getGroups());
                updatedIpIds.add(coll.getIpId());
                this.manageGroups(coll, updatedIpIds);
            }
        }
        /*
         * UniformResourceName urn = entity.getIpId();
         * // If entity contains groups => update all entities tagging this entity (recursively)
         * // Need to manage groups one by one
         * for (String group : entity.getGroups()) {
         * Set<Collection> collectionsToUpdate = new HashSet<>();
         * // Find all collections tagging this entity and try adding group
         * manageGroup(group, collectionsToUpdate, urn, entity, updatedIpIds);
         * // Recursively continue to collections tagging updated collections and so on until no more collections
         * // has to be updated
         * while (!collectionsToUpdate.isEmpty()) {
         * Collection firstColl = collectionsToUpdate.iterator().next();
         * manageGroup(group, collectionsToUpdate, firstColl.getIpId(), entity, updatedIpIds);
         * collectionsToUpdate.remove(firstColl);
         * }
         * }
         */

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

    /**
     * @param <T> one of {@link AbstractDescEntity} : {@link Dataset} or {@link Collection}
     * @param updatedEntity entity being created/updated
     * @param pFile the description of the entity
     * @param oldOne previous description file of updatedEntity
     * @throws IOException if description cannot be read
     * @throws ModuleException if description not conform to REGARDS requirements
     */
    private <T extends AbstractDescEntity> void setDescription(T updatedEntity, MultipartFile pFile,
            DescriptionFile oldOne) throws IOException, ModuleException {
        // we are updating/creating a description
        if ((updatedEntity.getDescriptionFile() != null) && !updatedEntity.getDescriptionFile().equals(oldOne)) {
            // this is a description file
            if ((pFile != null) && !pFile.isEmpty()) {
                // collections and dataset only have a description which is a url or a file
                if (!isContentTypeAcceptable(updatedEntity)) {
                    throw new EntityDescriptionUnacceptableType(pFile.getContentType());
                }
                // 10MB
                if (pFile.getSize() > MAX_DESC_FILE_SIZE) {
                    EntityDescriptionTooLargeException e = new EntityDescriptionTooLargeException(
                            pFile.getOriginalFilename());
                    logger.error("DescriptionFile is too big", e);
                    throw e;
                }
                String fileCharset = getCharset(pFile);
                if ((fileCharset != null) && !fileCharset.equals(StandardCharsets.UTF_8.toString())) {
                    throw new EntityDescriptionUnacceptableCharsetException(fileCharset);
                }
                // description file, change the old one because if we don't we accumulate tones of description
                if (oldOne != null) {
                    oldOne.setType(updatedEntity.getDescriptionFile().getType());
                    oldOne.setContent(pFile.getBytes());
                    oldOne.setUrl(null);
                    updatedEntity.setDescriptionFile(oldOne);
                } else {
                    // if there is no descriptionFile existing then lets create one
                    updatedEntity.setDescriptionFile(new DescriptionFile(pFile.getBytes(),
                            updatedEntity.getDescriptionFile().getType()));
                }
            } else { // pFile is null
                // this is an url
                if (oldOne != null) {
                    oldOne.setType(null);
                    oldOne.setContent(null);
                    oldOne.setUrl(updatedEntity.getDescriptionFile().getUrl());
                    updatedEntity.setDescriptionFile(oldOne);
                } else {
                    // if there is no description existing then lets create one
                    updatedEntity.setDescriptionFile(new DescriptionFile(updatedEntity.getDescriptionFile().getUrl()));
                }
            }
        } else { // No description file provided on entity to update : keep the current one
            updatedEntity.setDescriptionFile(oldOne);
        }
    }

    /**
     * Return true if file content type is acceptable (PDF or MARKDOWN). We are checking content type saved in the
     * entity and not the multipart file content type because markdown is not yet a standardized MIMEType and
     * our front cannot modify the content type of the corresponding part
     * @return true or false
     */
    private <T extends AbstractDescEntity> boolean isContentTypeAcceptable(T pEntity) {
        if (pEntity.getDescriptionFile() != null) {
            String fileContentType = pEntity.getDescriptionFile().getType().toString();
            int charsetIdx = fileContentType.indexOf(";charset");
            String contentType = (charsetIdx == -1) ? fileContentType : fileContentType.substring(0, charsetIdx);
            return contentType.equals(MediaType.APPLICATION_PDF_VALUE)
                    || contentType.equals(MediaType.TEXT_MARKDOWN_VALUE);
        }
        return false;
    }

    /**
     * Retrieve file charset
     * @param pFile description file from the user
     * @return file charset
     */
    private static String getCharset(MultipartFile pFile) {
        String contentType = pFile.getContentType();
        int charsetIdx = contentType.indexOf("charset=");
        return (charsetIdx == -1) ? null : contentType.substring(charsetIdx + 8).toUpperCase();
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
    public U update(Long pEntityId, U pEntity, MultipartFile file) throws ModuleException, IOException {
        // checks
        U entityInDb = checkUpdate(pEntityId, pEntity);
        if (pEntity instanceof AbstractDescEntity) {
            // If the entity already has a descriptionFile lets get it ....
            DescriptionFile descriptionFileFromDb = ((AbstractDescEntity) entityInDb).getDescriptionFile();
            // ...and eventually override it if file != null (see this.setDescription() method)
            this.setDescription((AbstractDescEntity) pEntity, file, descriptionFileFromDb);
        }
        return updateWithoutCheck(pEntity, entityInDb);
    }

    @Override
    public U update(UniformResourceName pEntityUrn, U pEntity, MultipartFile file) throws ModuleException, IOException {
        U entityInDb = repository.findOneByIpId(pEntityUrn);
        if (entityInDb == null) {
            throw new EntityNotFoundException(pEntity.getIpId().toString());
        }
        pEntity.setId(entityInDb.getId());
        // checks
        entityInDb = checkUpdate(entityInDb.getId(), pEntity);
        if (pEntity instanceof AbstractDescEntity) {
            // If the entity already has a descriptionFile lets set it ....
            DescriptionFile descriptionFileFromDb = ((AbstractDescEntity) entityInDb).getDescriptionFile();
            // ...and eventually override it if file != null (see this.setDescription() method)
            this.setDescription((AbstractDescEntity) pEntity, file, descriptionFileFromDb);
        }

        return updateWithoutCheck(pEntity, entityInDb);
    }

    /**
     * Really do the update of entities
     * @param pEntity updated entity to be saved
     * @param entityInDb only there for comparison for group management
     * @return updated entity with group set correclty
     */
    private U updateWithoutCheck(U pEntity, U entityInDb) {
        Set<UniformResourceName> oldLinks = extractUrns(entityInDb.getTags());
        Set<UniformResourceName> newLinks = extractUrns(pEntity.getTags());
        Set<String> oldGroups = entityInDb.getGroups();
        Set<String> newGroups = pEntity.getGroups();
        // IpId URNs of updated entities (those which need an AMQP event publish)
        Set<UniformResourceName> updatedIpIds = new HashSet<>();
        // Update entity, checks already assures us that everything which is updated can be updated so we can just put
        // pEntity into the DB.
        pEntity.setLastUpdate(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC));
        U updated = repository.save(pEntity);
        updatedIpIds.add(updated.getIpId());
        // Compute tags to remove and tags to add
        if (!oldLinks.equals(newLinks) || !oldGroups.equals(newGroups)) {
            Set<UniformResourceName> tagsToRemove = getDiff(oldLinks, newLinks);
            // For all previously tagged entities, retrieve all groups...
            Set<String> groupsToRemove = new HashSet<>();
            List<AbstractEntity> taggedEntitiesWithGroupsToRemove = entityRepository.findByIpIdIn(tagsToRemove);
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
    public U delete(Long pEntityId) throws EntityNotFoundException {
        Assert.notNull(pEntityId, "Entity identifier is required");
        final U toDelete = repository.findById(pEntityId);
        if (toDelete == null) {
            throw new EntityNotFoundException(pEntityId, this.getClass());
        }
        getStorageService().deleteAIP(toDelete);
        return delete(toDelete);
    }

    private U delete(U toDelete) {
        UniformResourceName urn = toDelete.getIpId();
        // IpId URNs that will need an AMQP event publishing
        Set<UniformResourceName> updatedIpIds = new HashSet<>();
        // Manage tags (must be done before group managing to avoid bad propagation)
        // Retrieve all entities tagging the one to delete
        final List<AbstractEntity> taggingEntities = entityRepository.findByTags(urn.toString());
        // Manage tags
        for (AbstractEntity taggingEntity : taggingEntities) {
            // remove tag to ipId
            taggingEntity.getTags().remove(urn.toString());
        }
        // Save all these tagging entities
        entityRepository.save(taggingEntities);
        taggingEntities.forEach(e -> updatedIpIds.add(e.getIpId()));

        // datasets that contain one of the entity groups
        Set<Dataset> datasets = new HashSet<>();
        // If entity contains groups => update all entities tagging this entity (recursively)
        // Need to manage groups one by one
        for (String group : toDelete.getGroups()) {
            // Find all collections containing group.
            List<Collection> collectionsWithGroup = collectionRepository.findByGroups(group);
            // Remove group from collections groups
            collectionsWithGroup.stream().filter(c -> !c.equals(toDelete)).forEach(c -> c.getGroups().remove(group));
            // Find all datasets containing this group (to rebuild groups propagation later)
            datasets.addAll(datasetRepository.findByGroups(group));
        }
        // Remove dataset to delete from datasets (no need to manage its groups)
        datasets.remove(toDelete);
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

    public void checkModelExists(AbstractEntity entity) throws ModuleException {
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

    private static DeletedEntity createDeletedEntity(AbstractEntity entity) {
        DeletedEntity delEntity = new DeletedEntity();
        delEntity.setCreationDate(entity.getCreationDate());
        delEntity.setDeletionDate(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC));
        delEntity.setIpId(entity.getIpId());
        delEntity.setLastUpdate(entity.getLastUpdate());
        return delEntity;
    }
}
