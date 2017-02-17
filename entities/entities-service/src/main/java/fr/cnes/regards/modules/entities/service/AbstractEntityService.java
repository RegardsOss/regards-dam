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

import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
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
import fr.cnes.regards.modules.entities.domain.AbstractDataEntity;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.AbstractLinkEntity;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.DataSet;
import fr.cnes.regards.modules.entities.domain.DescriptionFile;
import fr.cnes.regards.modules.entities.domain.Document;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.ObjectAttribute;
import fr.cnes.regards.modules.entities.service.identification.IdentificationService;
import fr.cnes.regards.modules.entities.service.validator.AttributeTypeValidator;
import fr.cnes.regards.modules.entities.service.validator.ComputationModeValidator;
import fr.cnes.regards.modules.entities.service.validator.NotAlterableAttributeValidator;
import fr.cnes.regards.modules.entities.service.validator.restriction.RestrictionValidatorFactory;
import fr.cnes.regards.modules.entities.urn.OAISIdentifier;
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
 * Entity service implementation
 *
 * @author Marc Sordi
 * @author Sylvain Vissiere-Guerinet
 *
 */
public abstract class AbstractEntityService implements IEntityService {

    /**
     * bean toward the module responsible to contact, or not, the archival storage
     */
    private IStorageService storageService;

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

    private final IAbstractEntityRepository<AbstractEntity> entitiesRepository;

    /**
     * Service managing identifier
     */
    private final IdentificationService idService;

    public AbstractEntityService(IModelAttributeService pModelAttributeService,
            IAbstractEntityRepository<AbstractEntity> pEntitiesRepository, IModelService pModelService,
            IdentificationService pIdService) {
        modelAttributeService = pModelAttributeService;
        entitiesRepository = pEntitiesRepository;
        modelService = pModelService;
        idService = pIdService;
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
     * @param pModelAttribute
     *            {@link ModelAttribute}
     * @param pAttributeKey
     *            attribute key
     * @param pManageAlterable
     *            manage update or not
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
     * @param pAttMap
     *            {@link Map} to build
     * @param pNamespace
     *            namespace context
     * @param pAttributes
     *            {@link AbstractAttribute} list to analyze
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

    private Collection associateCollection(Collection pSource, Set<UniformResourceName> pTargetsUrn) {
        final List<AbstractEntity> entityToAssociate = entitiesRepository.findByIpIdIn(pTargetsUrn);
        for (AbstractEntity target : entityToAssociate) {
            if (!(target instanceof Document)) {
                // Documents cannot be tagged into Collections
                pSource.getTags().add(target.getIpId().toString());
            }
            // bidirectional association if it's a collection or dataset
            if (target instanceof AbstractLinkEntity) {
                target.getTags().add(pSource.getIpId().toString());
                entitiesRepository.save(target);
            }
        }

        return entitiesRepository.save(pSource);
    }

    private AbstractDataEntity associateDataEntity(AbstractDataEntity pSource, Set<UniformResourceName> pTargetsUrn) {
        final List<AbstractEntity> entityToAssociate = entitiesRepository.findByIpIdIn(pTargetsUrn);
        for (AbstractEntity target : entityToAssociate) {
            if (target instanceof AbstractLinkEntity) {
                // only Collections(and DataSets) can only be associated with DataObjects
                pSource.getTags().add(target.getIpId().toString());
            }
        }
        return entitiesRepository.save(pSource);
    }

    private DataSet associateDataSet(DataSet pSource, Set<UniformResourceName> pTargetsUrn) {
        return pSource;
    }

    /**
     * dissociates specified entity from all associated entities
     *
     * @param pToDelete
     */
    @Override
    public void dissociate(AbstractEntity pToDelete) {
        final List<AbstractEntity> linkedToToDelete = entitiesRepository.findByTags(pToDelete.getIpId().toString());
        dissociate(pToDelete, linkedToToDelete);
    }

    @Override
    public <T extends AbstractEntity> T dissociate(T pSource, Set<UniformResourceName> pTargetsUrn) {
        final List<AbstractEntity> entityToDissociate = entitiesRepository.findByIpIdIn(pTargetsUrn);
        return dissociate(pSource, entityToDissociate);
    }

    @Override
    public <T extends AbstractEntity> T dissociate(T pSource, List<AbstractEntity> pEntityToDissociate) {
        final Set<String> toDissociateAssociations = pSource.getTags();
        for (AbstractEntity toBeDissociated : pEntityToDissociate) {
            toDissociateAssociations.remove(toBeDissociated.getIpId().toString());
            toBeDissociated.getTags().remove(pSource.getIpId().toString());
            entitiesRepository.save(toBeDissociated);
        }
        pSource.setTags(toDissociateAssociations);
        return entitiesRepository.save(pSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends AbstractEntity> T associate(T pSource, Set<UniformResourceName> pTargetsUrn) {
        if (pSource instanceof Collection) {
            return (T) associateCollection((Collection) pSource, pTargetsUrn);
        }
        if (pSource instanceof AbstractDataEntity) {
            return (T) associateDataEntity((AbstractDataEntity) pSource, pTargetsUrn);
        }
        if (pSource instanceof DataSet) {
            return (T) associateDataSet((DataSet) pSource, pTargetsUrn);
        }
        throw new UnsupportedOperationException("routing for " + pSource.getClass() + " is not implemented");
    }

    protected Set<UniformResourceName> extractUrns(Set<String> pTags) {
        return pTags.parallelStream().filter(t -> UniformResourceName.isValidUrn(t))
                .map(t -> UniformResourceName.fromString(t)).collect(Collectors.toSet());
    }

    /**
     * @param pEntityId
     *            a {@link AbstractEntity}
     * @param pToAssociate
     *            {@link Set} of {@link UniformResourceName}s representing {@link AbstractEntity} to associate to
     *            pCollection
     * @throws EntityNotFoundException
     */
    @Override
    public AbstractEntity associate(Long pEntityId, Set<UniformResourceName> pToAssociate)
            throws EntityNotFoundException {
        final AbstractEntity entity = entitiesRepository.findOne(pEntityId);
        if (entity == null) {
            throw new EntityNotFoundException(pEntityId);
        }
        return associate(entity, pToAssociate);
    }

    @Override
    public <T extends AbstractEntity> T associate(T pEntity) {
        final Set<String> tags = pEntity.getTags();
        final Set<UniformResourceName> toAssociateIpIds = extractUrns(tags);
        return associate(pEntity, toAssociateIpIds);
    }

    @Override
    public AbstractEntity dissociate(Long pEntityId, Set<UniformResourceName> pToBeDissociated)
            throws EntityNotFoundException {
        final AbstractEntity dissociatedEntity = entitiesRepository.findOne(pEntityId);
        if (dissociatedEntity == null) {
            throw new EntityNotFoundException(pEntityId);
        }
        return dissociate(dissociatedEntity, pToBeDissociated);
    }

    @Override
    @MultitenantTransactional
    public <T extends AbstractEntity> T create(T pEntity, MultipartFile file)
            throws ModuleException, IOException, PluginUtilsException {
        T newEntity = check(pEntity);
        // Generate ip_id
        newEntity.setIpId(idService.getRandomUrn(OAISIdentifier.AIP, EntityType.COLLECTION));
        newEntity = setDescription(newEntity, file);
        newEntity = doCreate(newEntity);
        if (!newEntity.getTags().isEmpty()) {
            newEntity = associate(newEntity);
        } else {
            // associate already do the save so if the method is not called, it has to be done
            newEntity = entitiesRepository.save(newEntity);
        }
        storageService = getStorageService();
        newEntity = storageService.storeAIP(newEntity);
        return newEntity;
    }

    /**
     * TODO make it possible to switch configuration dynamically between local and remote
     *
     * @return
     * @throws PluginUtilsException
     */
    private IStorageService getStorageService() throws PluginUtilsException {

        List<PluginParameter> parameters;

        parameters = PluginParametersFactory.build().getParameters();
        return PluginUtils.getPlugin(parameters, LocalStoragePlugin.class,
                                     Arrays.asList(LocalStoragePlugin.class.getPackage().getName()));
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
            String charsetOfFile = getCharset(file);
            if ((charsetOfFile != null) && (charsetOfFile != "utf-8")) {
                throw new EntityDescriptionUnacceptableCharsetException(charsetOfFile);
            }
            newEntity.setDescription(null);
            ((AbstractLinkEntity) newEntity)
                    .setDescriptionFile(new DescriptionFile(file.getBytes(), MediaType.valueOf(file.getContentType())));
        }
        return newEntity;
    }

    /**
     * @param pFile
     * @return
     */
    private boolean acceptacleContentType(MultipartFile pFile) {
        String fileContentTypeWithCharset = pFile.getContentType();
        int indexOfCharset = fileContentTypeWithCharset.indexOf(";charset");
        String contentType = indexOfCharset == -1 ? fileContentTypeWithCharset
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
        return charsetIndex == -1 ? null
                : contentType.substring(charsetIndex + 8, contentType.length() - 1).toLowerCase();
    }

    /**
     * @param pNewEntity
     * @return
     */
    protected abstract <T extends AbstractEntity> T doCreate(T pNewEntity) throws ModuleException;

    /**
     * @param pEntity
     * @return
     * @throws ModuleException
     */
    private <T extends AbstractEntity> T check(T pEntity) throws ModuleException {
        checkLinkedEntity(pEntity);
        pEntity = doCheck(pEntity);
        return pEntity;
    }

    /**
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
     * @throws ModuleException
     *             thrown if the entity cannot be found or if entities' id do not match
     */
    private <T extends AbstractEntity> T checkUpdate(Long pEntityId, T pEntity) throws ModuleException {
        AbstractEntity toBeUpdatedEntity = entitiesRepository.findOne(pEntityId);
        if ((toBeUpdatedEntity == null) || !toBeUpdatedEntity.getClass().equals(pEntity.getClass())) {
            throw new EntityNotFoundException(pEntityId);
        }
        if (!pEntity.getId().equals(pEntityId)) {
            throw new EntityInconsistentIdentifierException(pEntityId, pEntity.getId(), pEntity.getClass());
        }
        T toBeUpdated = doCheck(pEntity);
        return toBeUpdated;
    }

    /**
     * updates entity of id pEntityId according to pEntity
     *
     * @param pEntityId
     * @param pEntity
     * @return updated entity
     * @throws ModuleException
     * @throws PluginUtilsException
     */
    // FIXME: should i use a clone of the parameter instead of modifying it?
    @Override
    @MultitenantTransactional
    public <T extends AbstractEntity> T update(Long pEntityId, T pEntity) throws ModuleException, PluginUtilsException {
        // checks
        T toBeUpdated = checkUpdate(pEntityId, pEntity);
        // update fields
        Set<UniformResourceName> oldLinks = extractUrns(toBeUpdated.getTags());
        Set<UniformResourceName> newLinks = extractUrns(toBeUpdated.getTags());
        if (!oldLinks.equals(newLinks)) {
            final Set<UniformResourceName> toDissociate = getDiff(oldLinks, newLinks);
            dissociate(toBeUpdated, toDissociate);
            final Set<UniformResourceName> toAssociate = getDiff(newLinks, oldLinks);
            associate(toBeUpdated, toAssociate);
        }
        pEntity = doUpdate(pEntity);
        pEntity.setLastUpdate(LocalDateTime.now());
        T updated = entitiesRepository.save(pEntity);
        storageService = getStorageService();
        storageService.updateAIP(updated);
        return updated;
    }

    @Override
    public AbstractEntity delete(Long pEntityId) throws EntityNotFoundException, PluginUtilsException {
        final AbstractEntity toDelete = entitiesRepository.findOne(pEntityId);
        if (toDelete == null) {
            throw new EntityNotFoundException(pEntityId);
        }
        return delete(toDelete);
    }

    /**
     * @param pToDelete
     * @return
     * @throws PluginUtilsException
     */
    @MultitenantTransactional
    private AbstractEntity delete(AbstractEntity pToDelete) throws PluginUtilsException {
        dissociate(pToDelete);
        // FIXME: repo.delete and then persist.delete? ou c'est que storage qui gère le delete?
        pToDelete.setDeletionDate(LocalDateTime.now());
        pToDelete.setDeleted(true);
        AbstractEntity deleted = entitiesRepository.save(pToDelete);
        storageService = getStorageService();
        storageService.deleteAIP(pToDelete);
        return deleted;
    }

    @Override
    public AbstractEntity delete(String pEntityIpId) throws EntityNotFoundException, PluginUtilsException {
        final AbstractEntity toDelete = entitiesRepository.findOneByIpId(UniformResourceName.fromString(pEntityIpId));
        if (toDelete == null) {
            throw new EntityNotFoundException(pEntityIpId);
        }
        return delete(toDelete);
    }

    /**
     * handles specific updates to perform according to the instanciation type of the entity
     *
     * @param pEntity
     * @return updated entity
     */
    protected abstract <T extends AbstractEntity> T doUpdate(T pEntity);

    /**
     * @param pSource
     *            {@link Set} of {@link UniformResourceName}
     * @param pOther
     *            {@link Set} of {@link UniformResourceName} to remove from pSource
     * @return a new {@link Set} of {@link UniformResourceName} containing only the elements present into pSource and
     *         not in pOther
     */
    private Set<UniformResourceName> getDiff(Set<UniformResourceName> pSource, Set<UniformResourceName> pOther) {
        final Set<UniformResourceName> result = new HashSet<>();
        result.addAll(pSource);
        result.removeAll(pOther);
        return result;
    }

    @Override
    public void checkLinkedEntity(AbstractEntity pEntity) throws ModuleException {
        modelService.getModel(pEntity.getModel().getId());
    }

    protected abstract Logger getLogger();

}