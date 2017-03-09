/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.base.Throwables;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.module.rest.exception.EntityDescriptionTooLargeException;
import fr.cnes.regards.framework.module.rest.exception.EntityDescriptionUnacceptableCharsetException;
import fr.cnes.regards.framework.module.rest.exception.EntityDescriptionUnacceptableType;
import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.dao.ICollectionRepository;
import fr.cnes.regards.modules.entities.dao.IDatasetRepository;
import fr.cnes.regards.modules.entities.dao.deleted.IDeletedEntityRepository;
import fr.cnes.regards.modules.entities.domain.AbstractDescEntity;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.DescriptionFile;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.ObjectAttribute;
import fr.cnes.regards.modules.entities.domain.deleted.DeletedEntity;
import fr.cnes.regards.modules.entities.domain.event.EntityEvent;
import fr.cnes.regards.modules.entities.service.validator.AttributeTypeValidator;
import fr.cnes.regards.modules.entities.service.validator.ComputationModeValidator;
import fr.cnes.regards.modules.entities.service.validator.NotAlterableAttributeValidator;
import fr.cnes.regards.modules.entities.service.validator.restriction.RestrictionValidatorFactory;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttribute;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;
import fr.cnes.regards.modules.models.service.IModelAttributeService;
import fr.cnes.regards.modules.models.service.IModelService;
import fr.cnes.regards.plugins.utils.PluginUtils;
import fr.cnes.regards.plugins.utils.PluginUtilsException;

/**
 * Entity service implementation. This class isn't abstract in order to permit to be used directly.
 * But it is also used as a base for specific services (CollectionService, ...).
 *
 * @author Marc Sordi
 * @author Sylvain Vissiere-Guerinet
 * @author oroussel
 */
@Service(value = "entityService")
public class EntityService implements IEntityService {

    /**
     * Class logger
     */
    private final Logger logger = getLogger();

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
    private final IModelAttributeService modelAttributeService;

    private final IModelService modelService;

    private final IAbstractEntityRepository<AbstractEntity> entityRepository;

    protected final ICollectionRepository collectionRepository;

    protected final IDatasetRepository datasetRepository;

    private final IDeletedEntityRepository deletedEntityRepository;

    private final EntityManager em;

    private IPublisher publisher;

    public EntityService(IModelAttributeService pModelAttributeService,
            IAbstractEntityRepository<AbstractEntity> pEntityRepository, IModelService pModelService,
            IDeletedEntityRepository pDeletedEntityRepository, ICollectionRepository pCollectionRepository,
            IDatasetRepository pDatasetRepository, EntityManager pEm, IPublisher pPublisher) {
        modelAttributeService = pModelAttributeService;
        entityRepository = pEntityRepository;
        modelService = pModelService;
        deletedEntityRepository = pDeletedEntityRepository;
        collectionRepository = pCollectionRepository;
        datasetRepository = pDatasetRepository;
        em = pEm;
        publisher = pPublisher;
    }

    @Override
    public AbstractEntity load(UniformResourceName pIpId) {
        return entityRepository.findOneByIpId(pIpId);
    }

    @Override
    public AbstractEntity loadWithRelations(UniformResourceName pIpId) {
        // Particular case on datasets which contains more relations
        if (pIpId.getEntityType() == EntityType.DATASET) {
            Dataset dataset = datasetRepository.findByIpId(pIpId);
            if (dataset != null) {
                Hibernate.initialize(dataset.getPluginConfigurationIds());
            }
            return dataset;
        }
        return entityRepository.findByIpId(pIpId);
    }

    @Override
    public List<AbstractEntity> loadAllWithRelations(UniformResourceName... pIpIds) {
        List<AbstractEntity> entities = new ArrayList<>(pIpIds.length);
        Set<UniformResourceName> dsUrns = Arrays.stream(pIpIds)
                .filter(ipId -> ipId.getEntityType() == EntityType.DATASET).collect(Collectors.toSet());
        if (!dsUrns.isEmpty()) {
            List<Dataset> datasets = datasetRepository.findByIpIdIn(dsUrns);
            datasets.forEach(ds -> Hibernate.initialize(ds.getPluginConfigurationIds()));
            entities.addAll(datasets);
        }
        Set<UniformResourceName> otherUrns = Arrays.stream(pIpIds)
                .filter(ipId -> ipId.getEntityType() != EntityType.DATASET).collect(Collectors.toSet());
        if (!otherUrns.isEmpty()) {
            entities.addAll(entityRepository.findByIpIdIn(otherUrns));
        }
        return entities;
    }

    @Override
    public void validate(AbstractEntity pAbstractEntity, Errors pErrors, boolean pManageAlterable)
            throws ModuleException {
        Assert.notNull(pAbstractEntity, "Entity must not be null.");

        Model model = pAbstractEntity.getModel();
        Assert.notNull(model, "Model must be set on entity in order to be validated.");
        Assert.notNull(model.getId(), "Model identifier must be specified.");

        // Retrieve model attributes
        List<ModelAttribute> modAtts = modelAttributeService.getModelAttributes(model.getId());

        // Check model not empty
        if (((modAtts == null) || modAtts.isEmpty()) && (pAbstractEntity.getProperties() != null)) {
            pErrors.rejectValue("properties", "error.no.properties.defined.but.set",
                                "No properties defined in corresponding model but trying to create.");
        }

        // Prepare attributes for validation check
        Map<String, AbstractAttribute<?>> attMap = new HashMap<>();

        // Build attribute map
        buildAttributeMap(attMap, Fragment.getDefaultName(), pAbstractEntity.getProperties());

        // Loop over model attributes ... to validate each attribute
        for (ModelAttribute modelAtt : modAtts) {
            checkModelAttribute(attMap, modelAtt, pErrors, pManageAlterable);
        }

        if (pErrors.hasErrors()) {
            List<String> errors = new ArrayList<>();
            for (ObjectError error : pErrors.getAllErrors()) {
                String errorMessage = error.getDefaultMessage();
                logger.error(errorMessage);
                errors.add(errorMessage);
            }
            throw new EntityInvalidException(errors);
        }
    }

    /**
     * Validate an attribute with its corresponding model attribute
     *
     * @param pAttMap attribue map
     * @param pModelAttribute model attribute
     * @param pErrors validation errors
     * @param pManageAlterable manage update or not
     */
    protected void checkModelAttribute(Map<String, AbstractAttribute<?>> pAttMap, ModelAttribute pModelAttribute,
            Errors pErrors, boolean pManageAlterable) {

        AttributeModel attModel = pModelAttribute.getAttribute();
        String key = attModel.getFragment().getName().concat(NAMESPACE_SEPARATOR).concat(attModel.getName());
        logger.debug(String.format("Computed key : \"%s\"", key));

        // Retrieve attribute
        AbstractAttribute<?> att = pAttMap.get(key);

        // Null value check
        if (att == null) {
            String messageKey = "error.missing.required.attribute.message";
            String defaultMessage = String.format("Missing required attribute \"%s\".", key);
            if (pManageAlterable && attModel.isAlterable() && !attModel.isOptional()) {
                pErrors.reject(messageKey, defaultMessage);
                return;
            }
            if (!pManageAlterable && !attModel.isOptional()) {
                pErrors.reject(messageKey, defaultMessage);
                return;
            }
            logger.debug(String.format("Attribute \"%s\" not required in current context.", key));
            return;
        }

        // Do validation
        for (Validator validator : getValidators(pModelAttribute, key, pManageAlterable)) {
            if (validator.supports(att.getClass())) {
                validator.validate(att, pErrors);
            } else {
                String defaultMessage = String.format("Unsupported validator \"%s\" for attribute \"%s\"",
                                                      validator.getClass().getName(), key);
                pErrors.reject("error.unsupported.validator.message", defaultMessage);
            }
        }
    }

    /**
     * Compute available validators
     *
     * @param pModelAttribute {@link ModelAttribute}
     * @param pAttributeKey attribute key
     * @param pManageAlterable manage update or not
     * @return {@link Validator} list
     */
    protected List<Validator> getValidators(ModelAttribute pModelAttribute, String pAttributeKey,
            boolean pManageAlterable) {

        AttributeModel attModel = pModelAttribute.getAttribute();

        List<Validator> validators = new ArrayList<>();
        // Check computation mode
        validators.add(new ComputationModeValidator(pModelAttribute.getMode(), pAttributeKey));
        // Check alterable attribute
        // Update mode only :
        // FIXME retrieve not alterable attribute from database before update
        if (pManageAlterable && !attModel.isAlterable()) {
            validators.add(new NotAlterableAttributeValidator(pAttributeKey));
        }
        // Check attribute type
        validators.add(new AttributeTypeValidator(attModel.getType(), pAttributeKey));
        // Check restriction
        if (attModel.hasRestriction()) {
            validators.add(RestrictionValidatorFactory.getValidator(attModel.getRestriction(), pAttributeKey));
        }
        return validators;
    }

    /**
     * Build real attribute map extracting namespace from {@link ObjectAttribute} (i.e. fragment name)
     *
     * @param pAttMap {@link Map} to build
     * @param pNamespace namespace context
     * @param pAttributes {@link AbstractAttribute} list to analyze
     */
    protected void buildAttributeMap(Map<String, AbstractAttribute<?>> pAttMap, String pNamespace,
            final List<AbstractAttribute<?>> pAttributes) {
        if (pAttributes != null) {
            for (AbstractAttribute<?> att : pAttributes) {
                // Compute value
                if (ObjectAttribute.class.equals(att.getClass())) {
                    ObjectAttribute o = (ObjectAttribute) att;
                    buildAttributeMap(pAttMap, att.getName(), o.getValue());
                } else {
                    // Compute key
                    String key = pNamespace.concat(NAMESPACE_SEPARATOR).concat(att.getName());
                    logger.debug(String.format("Key \"%s\" -> \"%s\".", key, att.toString()));
                    pAttMap.put(key, att);
                }
            }
        }
    }

    /**
     * @param pEntityId an AbstractEntity identifier
     * @param pToAssociate UniformResourceName Set representing AbstractEntity to be associated to pCollection
     * @throws EntityNotFoundException
     */
    @Override
    public AbstractEntity associate(Long pEntityId, Set<UniformResourceName> pIpIds) throws EntityNotFoundException {
        final AbstractEntity entity = entityRepository.findById(pEntityId);
        if (entity == null) {
            throw new EntityNotFoundException(pEntityId);
        }
        // Adding new tags to detached entity
        em.detach(entity);
        entity.getTags().addAll(pIpIds.stream().map(UniformResourceName::toString).collect(Collectors.toSet()));
        final AbstractEntity entityInDb = entityRepository.findById(pEntityId);
        // And detach it too because it is the over one that will be persisted
        em.detach(entityInDb);
        this.updateWithoutCheck(entity, entityInDb);
        return entity;
    }

    @Override
    public AbstractEntity dissociate(Long pEntityId, Set<UniformResourceName> pIpIds) throws EntityNotFoundException {
        final AbstractEntity entity = entityRepository.findById(pEntityId);
        if (entity == null) {
            throw new EntityNotFoundException(pEntityId);
        }
        // Removing tags to detached entity
        em.detach(entity);
        entity.getTags().removeAll(pIpIds.stream().map(UniformResourceName::toString).collect(Collectors.toSet()));
        final AbstractEntity entityInDb = entityRepository.findById(pEntityId);
        // And detach it too because it is the over one that will be persisted
        em.detach(entityInDb);
        this.updateWithoutCheck(entity, entityInDb);
        return entity;
    }

    @Override
    public <T extends AbstractEntity> T create(T pEntity, MultipartFile file) throws ModuleException, IOException {
        T entity = check(pEntity);
        this.setDescription(entity, file);
        // IpIds of entities that will need an AMQP event publishing
        Set<UniformResourceName> updatedIpIds = new HashSet<>();
        this.manageGroups(entity, updatedIpIds);
        entity.setCreationDate(LocalDateTime.now());
        entity = entityRepository.save(entity);
        updatedIpIds.add(entity.getIpId());
        entity = getStorageService().storeAIP(entity);
        // AMQP event publishing
        this.publishEvents(updatedIpIds);
        return entity;
    }

    /**
     * Publish events to AMQP, one event by IpId
     * @param pIpIds ipId URNs of entities that need an Event publication onto AMQP
     */
    private void publishEvents(Set<UniformResourceName> pIpIds) {
        publisher.publish(new EntityEvent(pIpIds.toArray(new UniformResourceName[pIpIds.size()])));
    }

    /**
     * If entity is a collection, find all tagged entities and retrieved their groups.
     * Then find all collections tagging this entity and recursively propagate entity group to them.
     * @param entity entity to manage the add of groups
     */
    private <T extends AbstractEntity> void manageGroups(T entity, Set<UniformResourceName> pUpdatedIpIds) {
        // If entity tags entities => retrieve all groups of tagged entities (only for collection)
        if ((entity instanceof Collection) && !entity.getTags().isEmpty()) {
            List<AbstractEntity> taggedEntities = entityRepository.findByIpIdIn(extractUrns(entity.getTags()));
            final T finalEntity = entity;
            taggedEntities.forEach(e -> finalEntity.getGroups().addAll(e.getGroups()));
            pUpdatedIpIds.add(finalEntity.getIpId());
        }
        UniformResourceName urn = entity.getIpId();
        // If entity contains groups => update all entities tagging this entity (recursively)
        // Need to manage groups one by one
        for (String group : entity.getGroups()) {
            Set<Collection> collectionsToUpdate = new HashSet<>();
            // Find all collections tagging this entity and try adding group
            manageGroup(group, collectionsToUpdate, urn, entity, pUpdatedIpIds);
            // Recursively continue to collections tagging updated collections and so on until no more collections
            // has to be updated
            while (!collectionsToUpdate.isEmpty()) {
                Collection firstColl = collectionsToUpdate.iterator().next();
                manageGroup(group, collectionsToUpdate, firstColl.getIpId(), entity, pUpdatedIpIds);
                collectionsToUpdate.remove(firstColl);
            }
        }
    }

    /**
     * TODO make it possible to switch configuration dynamically between local and remote
     * Dynamically get the storage service
     * @return the storage service
     * @throws PluginUtilsException
     */
    private IStorageService getStorageService() {
        try {
            List<PluginParameter> parameters = PluginParametersFactory.build().getParameters();
            return PluginUtils.getPlugin(parameters, LocalStoragePlugin.class,
                                         Arrays.asList(LocalStoragePlugin.class.getPackage().getName()));
        } catch (PluginUtilsException pue) {
            throw Throwables.propagate(pue);
        }
    }

    /**
     * @param pEntity entity being created
     * @param pFile the description of the entity
     * @return modified entity with the description properly set
     * @throws IOException
     * @throws EntityDescriptionUnacceptableCharsetException thrown if charset is not utf-8
     * @throws EntityDescriptionTooLargeException thrown if file is bigger than 10MB
     * @throws EntityDescriptionUnacceptableType thrown if file is not a PDF or markdown
     */
    private <T extends AbstractEntity> void setDescription(T pEntity, MultipartFile pFile)
            throws IOException, ModuleException {
        if ((pEntity instanceof AbstractDescEntity) && (pFile != null) && !pFile.isEmpty()) {
            // collections and dataset only have a description which is a url or a file
            if (!isContentTypeAcceptable(pFile)) {
                throw new EntityDescriptionUnacceptableType(pFile.getContentType());
            }
            // 10MB
            if (pFile.getSize() > MAX_DESC_FILE_SIZE) {
                throw new EntityDescriptionTooLargeException(pFile.getOriginalFilename());
            }
            String fileCharset = getCharset(pFile);
            if ((fileCharset != null) && !fileCharset.equals(StandardCharsets.UTF_8.toString())) {
                throw new EntityDescriptionUnacceptableCharsetException(fileCharset);
            }
            // description or description file
            pEntity.setDescription(null);
            ((AbstractDescEntity) pEntity).setDescriptionFile(new DescriptionFile(pFile.getBytes(),
                    MediaType.valueOf(pFile.getContentType())));
        }
    }

    /**
     * For all collections tagging specified urn, try to add specified group.
     * If group was not already present, it is added and concerned collection is added to set, overwise it is removed
     * from set (means that collection has already been updated with the group)
     * @param group
     * @param collectionsToUpdate
     * @param urn
     */
    private void manageGroup(String group, Set<Collection> collectionsToUpdate, UniformResourceName urn,
            AbstractEntity rootEntity, Set<UniformResourceName> pUpdatedIpIds) {
        for (AbstractEntity e : entityRepository.findByTags(urn.toString())) {
            if (e instanceof Collection) {
                Collection coll = (Collection) e;
                // To be sure the root entity object is updated instead of a copy from Hb9n
                if (coll.getIpId().equals(rootEntity.getIpId())) {
                    em.detach(e);
                    e = rootEntity;
                }
                // if adding a new group
                if (e.getGroups().add(group)) {
                    coll = entityRepository.save(coll);
                    // add entity IpId to AMQP publishing events
                    pUpdatedIpIds.add(coll.getIpId());
                    collectionsToUpdate.add(coll);
                } else { // Group has been already added, nothing more to do => remove collection from map
                    collectionsToUpdate.remove(coll);
                }
            }
        }
    }

    /**
     * Return true if file content type is acceptable (PDF or MARKDOWN)
     * @param pFile file
     * @return true or false
     */
    private static boolean isContentTypeAcceptable(MultipartFile pFile) {
        String fileContentType = pFile.getContentType();
        int charsetIdx = fileContentType.indexOf(";charset");
        String contentType = (charsetIdx == -1) ? fileContentType : fileContentType.substring(0, charsetIdx);
        return contentType.equals(MediaType.APPLICATION_PDF_VALUE) || contentType.equals(MediaType.TEXT_MARKDOWN_VALUE);
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

    private <T extends AbstractEntity> T check(T pEntity) throws ModuleException {
        checkModelExists(pEntity);
        return doCheck(pEntity);
    }

    /**
     * Specific check depending on entity type
     * @param pEntity
     * @return
     */
    protected <T extends AbstractEntity> T doCheck(T pEntity) throws ModuleException {
        // nothing by default
        return pEntity;
    }

    /**
     * checks if the entity requested exists and that it is modified according to one of it's former version( pEntity's
     * id is pEntityId)
     *
     * @param pEntityId
     * @param pEntity
     * @return current entity
     * @throws ModuleException thrown if the entity cannot be found or if entities' id do not match
     */
    private <T extends AbstractEntity> T checkUpdate(Long pEntityId, T pEntity) throws ModuleException {
        AbstractEntity entityInDb = entityRepository.findById(pEntityId);
        if ((entityInDb == null) || !entityInDb.getClass().equals(pEntity.getClass())) {
            throw new EntityNotFoundException(pEntityId);
        }
        if (!pEntityId.equals(pEntity.getId())) {
            throw new EntityInconsistentIdentifierException(pEntityId, pEntity.getId(), pEntity.getClass());
        }
        return doCheck(pEntity);
    }

    @Override
    public <T extends AbstractEntity> T update(Long pEntityId, T pEntity) throws ModuleException {
        // checks
        T entityInDb = checkUpdate(pEntityId, pEntity);
        return updateWithoutCheck(pEntity, entityInDb);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends AbstractEntity> T update(UniformResourceName pEntityUrn, T pEntity) throws ModuleException {
        AbstractEntity entityInDb = entityRepository.findOneByIpId(pEntityUrn);
        if (entityInDb == null) {
            throw new EntityNotFoundException(pEntity.getIpId().toString());
        }
        pEntity.setId(entityInDb.getId());
        // checks
        entityInDb = checkUpdate(entityInDb.getId(), pEntity);
        return updateWithoutCheck(pEntity, (T) entityInDb);
    }

    private <T extends AbstractEntity> T updateWithoutCheck(T pEntity, T entityInDb) {
        Set<UniformResourceName> oldLinks = extractUrns(entityInDb.getTags());
        Set<UniformResourceName> newLinks = extractUrns(pEntity.getTags());
        // IpId URNs of updated entities (those which need an AMQP event publish)
        Set<UniformResourceName> updatedIpIds = new HashSet<>();
        // Update entity
        pEntity.setLastUpdate(LocalDateTime.now());
        T updated = entityRepository.save(pEntity);
        updatedIpIds.add(updated.getIpId());
        // Compute tags to remove and tags to add
        if (!oldLinks.equals(newLinks)) {
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
        this.publishEvents(updatedIpIds);
        return updated;
    }

    @Override
    public AbstractEntity delete(Long pEntityId) throws EntityNotFoundException {
        Assert.notNull(pEntityId);
        final AbstractEntity toDelete = entityRepository.findById(pEntityId);
        if (toDelete == null) {
            throw new EntityNotFoundException(pEntityId);
        }
        getStorageService().deleteAIP(toDelete);
        return delete(toDelete);
    }

    private AbstractEntity delete(AbstractEntity pToDelete) {
        UniformResourceName urn = pToDelete.getIpId();
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
        for (String group : pToDelete.getGroups()) {
            // Find all collections containing group.
            List<Collection> collectionsWithGroup = collectionRepository.findByGroups(group);
            // Remove group from collections groups
            collectionsWithGroup.stream().filter(c -> !c.equals(pToDelete)).forEach(c -> c.getGroups().remove(group));
            // Find all datasets containing group and adding new group on all collections tagging
            datasets.addAll(datasetRepository.findByGroups(group));
        }
        // Delete the entity
        entityRepository.delete(pToDelete);
        updatedIpIds.add(pToDelete.getIpId());
        // Manage all impacted datasets groups from scratch
        datasets.forEach(ds -> this.manageGroups(ds, updatedIpIds));

        deletedEntityRepository.save(createDeletedEntity(pToDelete));
        getStorageService().deleteAIP(pToDelete);
        // Publish events to AMQP
        this.publishEvents(updatedIpIds);
        return pToDelete;
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

    public void checkModelExists(AbstractEntity pEntity) throws ModuleException {
        // model must exist : EntityNotFoundException thrown if not
        modelService.getModel(pEntity.getModel().getId());
    }

    private static Set<UniformResourceName> extractUrns(Set<String> pTags) {
        return pTags.stream().filter(UniformResourceName::isValidUrn).map(UniformResourceName::fromString)
                .collect(Collectors.toSet());
    }

    protected static Logger getLogger() {
        return LoggerFactory.getLogger(EntityService.class);
    }

    private static DeletedEntity createDeletedEntity(AbstractEntity entity) {
        DeletedEntity delEntity = new DeletedEntity();
        delEntity.setDeletionDate(LocalDateTime.now());
        delEntity.setIpId(entity.getIpId());
        delEntity.setLastUpdate(entity.getLastUpdate());
        return delEntity;
    }

}