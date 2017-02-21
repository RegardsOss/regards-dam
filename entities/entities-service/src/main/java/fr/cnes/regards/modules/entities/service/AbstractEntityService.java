/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import java.io.IOException;
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

import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.base.Throwables;

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
import fr.cnes.regards.modules.entities.dao.IDataSetRepository;
import fr.cnes.regards.modules.entities.dao.deleted.IDeletedEntityRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.AbstractLinkEntity;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.DataSet;
import fr.cnes.regards.modules.entities.domain.DescriptionFile;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.ObjectAttribute;
import fr.cnes.regards.modules.entities.domain.deleted.DeletedEntity;
import fr.cnes.regards.modules.entities.service.validator.AttributeTypeValidator;
import fr.cnes.regards.modules.entities.service.validator.ComputationModeValidator;
import fr.cnes.regards.modules.entities.service.validator.NotAlterableAttributeValidator;
import fr.cnes.regards.modules.entities.service.validator.restriction.RestrictionValidatorFactory;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttribute;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;
import fr.cnes.regards.modules.models.service.IModelAttributeService;
import fr.cnes.regards.modules.models.service.IModelService;
import fr.cnes.regards.plugins.utils.PluginUtils;
import fr.cnes.regards.plugins.utils.PluginUtilsException;

/**
 * Entity service implementation
 *
 * @author Marc Sordi
 * @author Sylvain Vissiere-Guerinet
 *
 */
public abstract class AbstractEntityService implements IEntityService {

    /**
     * Class logger
     */
    private final Logger logger = getLogger();

    /**
     * Namespace separator
     */
    private static final String NAMESPACE_SEPARATOR = ".";

    /**
     * Attribute model service
     */
    private final IModelAttributeService modelAttributeService;

    private final IModelService modelService;

    private final IAbstractEntityRepository<AbstractEntity> entityRepository;

    protected final ICollectionRepository collectionRepository;

    protected final IDataSetRepository datasetRepository;

    private final IDeletedEntityRepository deletedEntityRepository;

    private final EntityManager em;

    public AbstractEntityService(IModelAttributeService pModelAttributeService,
            IAbstractEntityRepository<AbstractEntity> pEntityRepository, IModelService pModelService,
            IDeletedEntityRepository pDeletedEntityRepository, ICollectionRepository pCollectionRepository,
            IDataSetRepository pDatasetRepository, EntityManager pEm) {
        modelAttributeService = pModelAttributeService;
        entityRepository = pEntityRepository;
        modelService = pModelService;
        deletedEntityRepository = pDeletedEntityRepository;
        collectionRepository = pCollectionRepository;
        datasetRepository = pDatasetRepository;
        this.em = pEm;
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
        if (((modAtts == null) || modAtts.isEmpty()) && (pAbstractEntity.getAttributes() != null)) {
            pErrors.rejectValue("attributes", "error.no.attribute.defined.but.set",
                                "No attribute defined in corresponding model but trying to create.");
        }

        // Prepare attributes for validation check
        Map<String, AbstractAttribute<?>> attMap = new HashMap<>();

        // Build attribute map
        buildAttributeMap(attMap, Fragment.getDefaultName(), pAbstractEntity.getAttributes());

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
     * @param pAttMap
     *            attribue map
     * @param pModelAttribute
     *            model attribute
     * @param pErrors
     *            validation errors
     * @param pManageAlterable
     *            manage update or not
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
     * @param pEntityId a {@link AbstractEntity}
     * @param pToAssociate {@link Set} of {@link UniformResourceName}s representing {@link AbstractEntity} to associate
     * to pCollection
     * @throws EntityNotFoundException
     */
    @Override
    public AbstractEntity associate(Long pEntityId, Set<UniformResourceName> pToAssociate)
            throws EntityNotFoundException {
        final AbstractEntity entity = entityRepository.findById(pEntityId);
        if (entity == null) {
            throw new EntityNotFoundException(pEntityId);
        }
        // Adding new tags to detached entity
        em.detach(entity);
        entity.getTags().addAll(pToAssociate.stream().map(UniformResourceName::toString).collect(Collectors.toSet()));
        final AbstractEntity entityInDb = entityRepository.findById(pEntityId);
        // And detach it too because it is the over one that will be persisted
        em.detach(entityInDb);
        this.updateWithoutCheck(entity, entityInDb);
        return entity;
    }

    @Override
    public AbstractEntity dissociate(Long pEntityId, Set<UniformResourceName> pToBeDissociated)
            throws EntityNotFoundException {
        final AbstractEntity entity = entityRepository.findById(pEntityId);
        if (entity == null) {
            throw new EntityNotFoundException(pEntityId);
        }
        // Removing tags to detached entity
        em.detach(entity);
        entity.getTags()
                .removeAll(pToBeDissociated.stream().map(UniformResourceName::toString).collect(Collectors.toSet()));
        final AbstractEntity entityInDb = entityRepository.findById(pEntityId);
        // And detach it too because it is the over one that will be persisted
        em.detach(entityInDb);
        this.updateWithoutCheck(entity, entityInDb);
        return entity;
    }

    @Override
    public <T extends AbstractEntity> T create(T pEntity, MultipartFile file) throws ModuleException, IOException {
        T entity = check(pEntity);
        entity = setDescription(entity, file);
        this.manageGroups(entity);
        entity = beforeCreate(entity);
        entity.setCreationDate(LocalDateTime.now());
        entity = entityRepository.save(entity);
        entity = getStorageService().storeAIP(entity);
        return entity;
    }

    /**
     * If entity is a collection, find all tagged entities and retrieved their groups.
     * Then find all collections tagging this entity and recursively propagate entity group to them.
     * @param entity entity to manage the add of groups
     */
    private <T extends AbstractEntity> void manageGroups(T entity) {
        // If entity tags entities => retrieve all groups of tagged entities (only for collection)
        if (entity instanceof Collection) {
            if (!entity.getTags().isEmpty()) {
                List<AbstractEntity> taggedEntities = entityRepository.findByIpIdIn(extractUrns(entity.getTags()));
                final T finalEntity = entity;
                taggedEntities.forEach(e -> finalEntity.getGroups().addAll(e.getGroups()));
            }
        }
        UniformResourceName urn = entity.getIpId();
        // If entity contains groups => update all entities tagging this entity (recursively)
        // Need to manage groups one by one
        for (String group : entity.getGroups()) {
            Set<Collection> collectionsToUpdate = new HashSet<>();
            // Find all collections tagging this entity and try adding group
            manageGroup(group, collectionsToUpdate, urn);
            // Recursively continue to collections tagging updated collections and so on until no more collections
            // has to be updated
            while (!collectionsToUpdate.isEmpty()) {
                Collection firstColl = collectionsToUpdate.iterator().next();
                manageGroup(group, collectionsToUpdate, firstColl.getIpId());
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
     * @param pNewEntity
     *            entity being created
     * @param pFile
     *            the description of the entity
     * @return modified entity with the description properly set
     * @throws IOException
     * @throws EntityDescriptionUnacceptableCharsetException
     *             thrown if charset is not utf-8
     * @throws EntityDescriptionTooLargeException
     *             thrown if file is bigger than 10MB
     * @throws EntityDescriptionUnacceptableType
     *             thrown if file is not a PDF or markdown
     */
    private <T extends AbstractEntity> T setDescription(T newEntity, MultipartFile file)
            throws IOException, ModuleException {
        if ((newEntity instanceof AbstractLinkEntity) && (file != null) && !file.isEmpty()) {
            // collections and dataset only has a description which is a url or a file
            if (!acceptacleContentType(file)) {
                throw new EntityDescriptionUnacceptableType(file.getContentType());
            }
            // 10 000 000B=10MB
            if (file.getSize() > 10000000) {
                throw new EntityDescriptionTooLargeException(file.getOriginalFilename());
            }
            String charsetOfFile = this.getCharset(file);
            if ((charsetOfFile != null) && (charsetOfFile != "utf-8")) {
                throw new EntityDescriptionUnacceptableCharsetException(charsetOfFile);
            }
            // description or description file
            newEntity.setDescription(null);
            ((AbstractLinkEntity) newEntity)
                    .setDescriptionFile(new DescriptionFile(file.getBytes(), MediaType.valueOf(file.getContentType())));
        }
        return newEntity;
    }

    /**
     * For all collections tagging specified urn, try to add specified group.
     * If group was not already present, it is added and concerned collection is added to set, overwise it is removed
     * from set (means that collection has already been updated with the group)
     * @param group
     * @param collectionsToUpdate
     * @param urn
     */
    private void manageGroup(String group, Set<Collection> collectionsToUpdate, UniformResourceName urn) {
        List<AbstractEntity> taggingCollections = entityRepository.findByTags(urn.toString());
        for (AbstractEntity e : taggingCollections) {
            if (e instanceof Collection) {
                Collection coll = (Collection) e;
                // if adding a new group
                if (e.getGroups().add(group)) {
                    entityRepository.save(coll);
                    collectionsToUpdate.add(coll);
                } else { // Group has been already added, nothing more to do => remove collection from map
                    collectionsToUpdate.remove(coll);
                }
            }
        }
    }

    private boolean acceptacleContentType(MultipartFile pFile) {
        String fileContentTypeWithCharset = pFile.getContentType();
        int indexOfCharset = fileContentTypeWithCharset.indexOf(";charset");
        String contentType = (indexOfCharset == -1) ? fileContentTypeWithCharset
                : fileContentTypeWithCharset.substring(0, indexOfCharset);
        return contentType.equals(MediaType.APPLICATION_PDF_VALUE) || contentType.equals(MediaType.TEXT_MARKDOWN_VALUE);
    }

    /**
     * check if the file has a charset compliant with the application
     *
     * @param pFile
     *            description file from the user
     * @return true is charset is utf8 or not set(considered us-ascii) or if the file is a pdf, false otherwise
     */
    private String getCharset(MultipartFile pFile) {
        String contentType = pFile.getContentType();
        int charsetIndex = contentType.indexOf("charset=");
        return (charsetIndex == -1) ? null
                : contentType.substring(charsetIndex + 8, contentType.length() - 1).toLowerCase();
    }

    /**
     * Specific operations before creating entity
     * @param pNewEntity
     * @return
     */
    protected abstract <T extends AbstractEntity> T beforeCreate(T pNewEntity) throws ModuleException;

    private <T extends AbstractEntity> T check(T pEntity) throws ModuleException {
        checkModelExists(pEntity);
        pEntity = doCheck(pEntity);
        return pEntity;
    }

    /**
     * Specific check depending on entity type
     * @param pEntity
     * @return
     */
    protected abstract <T extends AbstractEntity> T doCheck(T pEntity) throws ModuleException;

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
        T toBeUpdated = doCheck(pEntity);
        return toBeUpdated;
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
        // Update entity
        T updated = beforeUpdate(pEntity);
        updated.setLastUpdate(LocalDateTime.now());
        updated = entityRepository.save(pEntity);
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
                // ... then manage concerned groups on all datasets containing therm
                List<DataSet> datasetsWithGroup = datasetRepository.findByGroups(group);
                datasetsWithGroup.forEach(this::manageGroups);
            }
            // Don't forget to manage groups for current entity too
            this.manageGroups(updated);
        }
        updated = getStorageService().updateAIP(updated);
        return updated;
    }

    @Override
    public AbstractEntity delete(Long pEntityId) throws EntityNotFoundException {
        final AbstractEntity toDelete = entityRepository.findById(pEntityId);
        if (toDelete == null) {
            throw new EntityNotFoundException(pEntityId);
        }
        getStorageService().deleteAIP(toDelete);
        return delete(toDelete);
    }

    private AbstractEntity delete(AbstractEntity pToDelete) {
        UniformResourceName urn = pToDelete.getIpId();

        // Manage tags (must be done before group managing to avoid bad propagation)
        // Retrieve all entities tagging the one to delete
        final List<AbstractEntity> taggingEntities = entityRepository.findByTags(pToDelete.getIpId().toString());
        // Manage tags
        for (AbstractEntity taggingEntity : taggingEntities) {
            // remove tag to ipId
            taggingEntity.getTags().remove(urn.toString());
        }
        // Save all these tagging entities
        entityRepository.save(taggingEntities);

        // datasets that contain one of the entity groups
        Set<DataSet> datasets = new HashSet<>();
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
        // Manage all impacted datasets groups from scratch
        datasets.forEach(this::manageGroups);

        deletedEntityRepository.save(createDeletedEntity(pToDelete));
        getStorageService().deleteAIP(pToDelete);
        return pToDelete;
    }

    /**
     * handles specific updates to perform according to the instanciation type of the entity
     *
     * @param pEntity
     * @return updated entity
     */
    protected abstract <T extends AbstractEntity> T beforeUpdate(T pEntity);

    /**
     * @param pSource {@link Set} of {@link UniformResourceName}
     * @param pOther {@link Set} of {@link UniformResourceName} to remove from pSource
     * @return a new {@link Set} of {@link UniformResourceName} containing only the elements present into pSource and
     *         not in pOther
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

    protected abstract Logger getLogger();

    private static DeletedEntity createDeletedEntity(AbstractEntity entity) {
        DeletedEntity delEntity = new DeletedEntity();
        delEntity.setDeletionDate(LocalDateTime.now());
        delEntity.setIpId(entity.getIpId());
        delEntity.setLastUpdate(entity.getLastUpdate());
        return delEntity;
    }

}