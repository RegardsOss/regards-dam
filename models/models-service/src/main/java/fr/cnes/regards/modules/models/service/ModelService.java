/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.google.common.collect.*;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.*;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginMetaData;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.modules.entities.domain.StaticProperties;
import fr.cnes.regards.modules.models.dao.IModelAttrAssocRepository;
import fr.cnes.regards.modules.models.dao.IModelRepository;
import fr.cnes.regards.modules.models.domain.*;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;
import fr.cnes.regards.modules.models.service.event.NewFragmentAttributeEvent;
import fr.cnes.regards.modules.models.service.exception.FragmentAttributeException;
import fr.cnes.regards.modules.models.service.exception.ImportException;
import fr.cnes.regards.modules.models.service.exception.UnexpectedModelAttributeException;
import fr.cnes.regards.modules.models.service.xml.XmlExportHelper;
import fr.cnes.regards.modules.models.service.xml.XmlImportHelper;

/**
 * Manage model lifecycle
 *
 * @author Marc Sordi
 */
@Service
@MultitenantTransactional
public class ModelService implements IModelService, IModelAttrAssocService {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ModelService.class);

    /**
     * Model repository
     */
    private final IModelRepository modelRepository;

    /**
     * Model attribute repository
     */
    private final IModelAttrAssocRepository modelAttributeRepository;

    /**
     * Attribute model service
     */
    private final IAttributeModelService attributeModelService;

    private final IPluginService pluginService;

    // CHECKSTYLE:OFF
    public ModelService(IModelRepository pModelRepository, IModelAttrAssocRepository pModelAttributeRepository,
            IAttributeModelService pAttributeModelService, IPluginService pPluginService) {
        modelRepository = pModelRepository;
        modelAttributeRepository = pModelAttributeRepository;
        attributeModelService = pAttributeModelService;
        pluginService = pPluginService;
        pluginService.addPluginPackage(IComputedAttribute.class.getPackage().getName());
    }
    // CHECKSTYLE:ON

    @Override
    public List<Model> getModels(EntityType pType) {
        Iterable<Model> models;
        if (pType == null) {
            models = modelRepository.findAll();
        } else {
            models = modelRepository.findByType(pType);
        }
        return (models != null) ? ImmutableList.copyOf(models) : Collections.emptyList();
    }

    @Override
    public Model createModel(Model pModel) throws ModuleException {
        if (pModel.isIdentifiable()) {
            throw new EntityUnexpectedIdentifierException(pModel.getId(), Model.class);
        }
        final Model model = modelRepository.findByName(pModel.getName());
        if (model != null) {
            throw new EntityAlreadyExistsException(
                    String.format("Model with name \"%s\" already exists!", pModel.getName()));
        }
        return modelRepository.save(pModel);
    }

    @Override
    public Model getModel(Long pModelId) throws ModuleException {
        final Model model = modelRepository.findOne(pModelId);
        if (model == null) {
            throw new EntityNotFoundException(pModelId, Model.class);
        }
        return model;
    }

    @Override
    public Model getModelByName(String pModelName) throws ModuleException {
        return modelRepository.findByName(pModelName);
    }

    @Override
    public Model updateModel(Long pModelId, Model pModel) throws ModuleException {
        if (!pModel.isIdentifiable()) {
            throw new EntityNotIdentifiableException(
                    String.format("Unknown identifier for model \"%s\"", pModel.getName()));
        }
        if (!pModelId.equals(pModel.getId())) {
            throw new EntityInconsistentIdentifierException(pModelId, pModel.getId(), pModel.getClass());
        }
        if (!modelRepository.exists(pModelId)) {
            throw new EntityNotFoundException(pModel.getId(), Model.class);
        }
        return modelRepository.save(pModel);
    }

    @Override
    public void deleteModel(Long pModelId) throws ModuleException {
        List<ModelAttrAssoc> modelAttrAssocs = modelAttributeRepository.findByModelId(pModelId);
        modelAttributeRepository.delete(modelAttrAssocs);
        if (modelRepository.exists(pModelId)) {
            modelRepository.delete(pModelId);
        }
    }

    @Override
    public Model duplicateModel(Long pModelId, Model pModel) throws ModuleException {
        if (!modelRepository.exists(pModelId)) {
            throw new EntityNotFoundException(pModel.getId(), Model.class);
        }
        return duplicateModelAttrAssocs(pModelId, createModel(pModel));
    }

    @Override
    public List<ModelAttrAssoc> getModelAttrAssocs(Long pModelId) throws ModuleException {
        Iterable<ModelAttrAssoc> modelAttributes = modelAttributeRepository.findByModelId(pModelId);

        if (modelAttributes != null) {
            modelAttributes.forEach(modelAttr -> modelAttr.getAttribute().buildJsonPath(StaticProperties.PROPERTIES));
            return ImmutableList.copyOf(modelAttributes);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Page<AttributeModel> getAttributeModels(List<Long> pModelIds, Pageable pPageable) {
        if (pModelIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pPageable, 0);
        }
        return modelAttributeRepository.findAllAttributeByModelIdIn(pModelIds, pPageable);
    }

    @Override
    public ModelAttrAssoc bindAttributeToModel(Long pModelId, ModelAttrAssoc pModelAttribute) throws ModuleException {
        final Model model = getModel(pModelId);
        if (pModelAttribute.isIdentifiable()) {
            throw new EntityUnexpectedIdentifierException(pModelAttribute.getId(), ModelAttrAssoc.class);
        }
        // Do not bind attribute that is part of a fragment
        if (attributeModelService.isFragmentAttribute(pModelAttribute.getAttribute().getId())) {
            throw new FragmentAttributeException(pModelAttribute.getAttribute().getId());
        }
        // Do not rebind an attribute
        final Iterable<ModelAttrAssoc> existingModelAtts = modelAttributeRepository.findByModelId(pModelId);
        if (existingModelAtts != null) {
            for (ModelAttrAssoc modAtt : existingModelAtts) {
                if (modAtt.equals(pModelAttribute)) {
                    throw new EntityAlreadyExistsException(
                            String.format("Attribute %s already exists in model %s!", modAtt.getAttribute().getName(),
                                          model.getName()));
                }
            }
        }

        pModelAttribute.setModel(model);
        return modelAttributeRepository.save(pModelAttribute);
    }

    @Override
    public ModelAttrAssoc getModelAttrAssoc(Long pModelId, Long pModelAttributeId) throws ModuleException {
        final ModelAttrAssoc modelAtt = modelAttributeRepository.findOne(pModelAttributeId);
        if (modelAtt == null) {
            throw new EntityNotFoundException(pModelAttributeId, ModelAttrAssoc.class);
        }
        if (!pModelId.equals(modelAtt.getModel().getId())) {
            throw new UnexpectedModelAttributeException(pModelId, pModelAttributeId);
        }
        return modelAtt;
    }

    @Override
    public ModelAttrAssoc getModelAttrAssoc(Long pModelId, AttributeModel pAttribute) {
        return modelAttributeRepository.findByModelIdAndAttribute(pModelId, pAttribute);
    }

    @Override
    public ModelAttrAssoc updateModelAttribute(Long pModelId, Long pAttributeId, ModelAttrAssoc pModelAttribute)
            throws ModuleException {
        if (!pModelAttribute.isIdentifiable()) {
            throw new EntityNotIdentifiableException(
                    String.format("Unknown identifier for model attribute \"%s\"", pModelAttribute.getId()));
        }
        if (!pModelAttribute.getId().equals(pAttributeId)) {
            throw new EntityInconsistentIdentifierException(pAttributeId, pModelAttribute.getId(),
                                                            ModelAttrAssoc.class);
        }
        if (!modelAttributeRepository.exists(pAttributeId)) {
            throw new EntityNotFoundException(pAttributeId, ModelAttrAssoc.class);
        }
        return modelAttributeRepository.save(pModelAttribute);
    }

    @Override
    public void unbindAttributeFromModel(Long pModelId, Long pAttributeId) throws ModuleException {
        final ModelAttrAssoc modelAtt = getModelAttrAssoc(pModelId, pAttributeId);
        // Do not bind attribute that is part of a fragment
        if (attributeModelService.isFragmentAttribute(modelAtt.getAttribute().getId())) {
            throw new FragmentAttributeException(modelAtt.getAttribute().getId());
        }
        modelAttributeRepository.delete(pAttributeId);
    }

    @Override
    public List<ModelAttrAssoc> bindNSAttributeToModel(Long pModelId, Fragment pFragment) throws ModuleException {
        final List<ModelAttrAssoc> modAtts = new ArrayList<>();
        final Model model = getModel(pModelId);
        final Iterable<ModelAttrAssoc> existingModelAtts = modelAttributeRepository.findByModelId(pModelId);
        final Long pFragmentId = pFragment.getId();

        // Check if fragment not already bound
        if (!isBoundFragment(existingModelAtts, pFragmentId)) {

            // Retrieve fragment attributes
            final List<AttributeModel> attModels = attributeModelService.findByFragmentId(pFragmentId);

            if (attModels != null) {
                for (AttributeModel attModel : attModels) {
                    // Create model attributes to link base attributes
                    final ModelAttrAssoc modelAtt = new ModelAttrAssoc();
                    modelAtt.setAttribute(attModel);
                    modelAtt.setModel(model);
                    modelAttributeRepository.save(modelAtt);
                    modAtts.add(modelAtt);
                }
            }
        } else {
            LOGGER.warn("Fragment {} already bound to model {}", pFragmentId, pModelId);
        }
        return modAtts;
    }

    /**
     * Check if fragment is bounded to the model
     *
     * @param pModelAtts model attributes
     * @param pFragmentId fragment identifier
     * @return true if fragment is bound
     */
    private boolean isBoundFragment(final Iterable<ModelAttrAssoc> pModelAtts, Long pFragmentId) {
        if (pModelAtts != null) {
            for (ModelAttrAssoc modelAtt : pModelAtts) {
                if (pFragmentId.equals(modelAtt.getAttribute().getFragment().getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void unbindNSAttributeToModel(Long pModelId, Long pFragmentId) throws ModuleException {
        final Iterable<ModelAttrAssoc> modelAtts = modelAttributeRepository.findByModelId(pModelId);
        if (modelAtts != null) {
            for (ModelAttrAssoc modelAtt : modelAtts) {
                if (pFragmentId.equals(modelAtt.getAttribute().getFragment().getId())) {
                    modelAttributeRepository.delete(modelAtt);
                }
            }
        }
    }

    @Override
    public void updateNSBind(AttributeModel added) {
        List<AttributeModel> attributes = attributeModelService.findByFragmentName(added.getFragment().getName());
        Set<Model> modelsToBeUpdated = Sets.newHashSet();
        for (AttributeModel attr : attributes) {
            retrieveModelAttrAssocsByAttributeId(attr)
                    .forEach(modelAttrAssoc -> modelsToBeUpdated.add(modelAttrAssoc.getModel()));
        }
        for (Model model : modelsToBeUpdated) {
            ModelAttrAssoc modelAtt = new ModelAttrAssoc();
            modelAtt.setAttribute(added);
            modelAtt.setModel(model);
            modelAttributeRepository.save(modelAtt);
        }
    }

    @Override
    public Collection<ModelAttrAssoc> retrieveModelAttrAssocsByAttributeId(AttributeModel attr) {
        return modelAttributeRepository.findAllByAttributeId(attr.getId());
    }

    @Override
    public Model duplicateModelAttrAssocs(Long pSourceModelId, Model pTargetModel) throws ModuleException {
        // Retrieve all reference model attributes
        final List<ModelAttrAssoc> modelAtts = getModelAttrAssocs(pSourceModelId);
        if (modelAtts != null) {
            for (ModelAttrAssoc modelAtt : modelAtts) {
                // Computed model associations are not duplicated
                if (modelAtt.getMode() == ComputationMode.GIVEN) {
                    // Create model attributes to link base attributes
                    final ModelAttrAssoc duplicatedModelAtt = new ModelAttrAssoc();
                    duplicatedModelAtt.setAttribute(modelAtt.getAttribute());
                    duplicatedModelAtt.setModel(pTargetModel);
                    modelAttributeRepository.save(duplicatedModelAtt);
                }
            }
        }
        return pTargetModel;
    }

    @Override
    public void exportModel(Long pModelId, OutputStream pOutputStream) throws ModuleException {
        // Get model
        final Model model = getModel(pModelId);
        // Get all related attributes
        final List<ModelAttrAssoc> modelAtts = getModelAttrAssocs(pModelId);
        // Export fragment to output stream
        XmlExportHelper.exportModel(pOutputStream, model, modelAtts);
    }

    @Override
    public Model importModel(InputStream pInputStream) throws ModuleException {
        List<PluginConfiguration> plgConfigurations = new ArrayList<>();
        // Import model from input stream
        final List<ModelAttrAssoc> modelAtts = XmlImportHelper.importModel(pInputStream, plgConfigurations);
        // Create/update PLuginConfigurations if the are some
        if (!plgConfigurations.isEmpty()) {
            this.eventualyCreatePluginConfigurations(plgConfigurations);
        }

        // Create model once
        Model newModel = createModel(modelAtts.get(0).getModel()); // List of model attributes cannot be empty here
        // Create or control model attributes
        addAllModelAttributes(modelAtts);
        // Return created model
        LOGGER.info("New model \"{}\" with version \"{}\" created", newModel.getName(), newModel.getVersion());
        return newModel;
    }

    private void eventualyCreatePluginConfigurations(List<PluginConfiguration> plgConfigurations)
            throws ModuleException {
        for (PluginConfiguration plgConf : plgConfigurations) {
            // If plugin configuration already exists
            if (pluginService.existsByLabel(plgConf.getLabel())) {
                // New one must be consistent with existing one
                PluginConfiguration currentPlgConf = pluginService.getPluginConfigurationByLabel(plgConf.getLabel());
                // Most of plugin configuration attributes come from plugin Metadata which have been retrieved from database
                // so only plugin parameters should be consistently checked
                for (PluginParameter param : plgConf.getParameters()) {
                    String curValue = currentPlgConf.getParameterValue(param.getName());
                    // Plugin parameter found
                    if (curValue != null) {
                        if (!Objects.equals(param.getValue(), curValue)) {
                            String msg = String
                                    .format("Compute plugin with label %s is inconsistent with existing one : "
                                                    + "plugin parameter %s with value %s differs from existing "
                                                    + "value (%s)", plgConf.getLabel(), param.getName(),
                                            param.getValue(), curValue);
                            LOGGER.error(msg);
                            throw new ImportException(msg);
                        }
                    } else { // Plugin parameter not found
                        String msg = String.format("Compute plugin with label %s is inconsistent with existing one : "
                                                           + "no plugin parameter %s found", plgConf.getLabel(),
                                                   param.getName());
                        LOGGER.error(msg);
                        throw new ImportException(msg);
                    }
                }
                // No need to save new one
            } else {
                // Plugin is ok (new or consistent with previous one
                pluginService.savePluginConfiguration(plgConf);
            }
        }
    }

    /**
     * Add all {@link ModelAttrAssoc} related to a model
     *
     * @param pModelAtts list of {@link ModelAttrAssoc}
     * @throws ModuleException if error occurs!
     */
    private void addAllModelAttributes(List<ModelAttrAssoc> pModelAtts) throws ModuleException {

        // Keep fragment content to check fragment consistence
        Map<String, List<AttributeModel>> fragmentAttMap = new HashMap<>();

        for (ModelAttrAssoc modelAtt : pModelAtts) {

            AttributeModel imported = modelAtt.getAttribute();

            // Check if attribute already exists
            final AttributeModel existing = attributeModelService
                    .findByNameAndFragmentName(imported.getName(), imported.getFragment().getName());

            if (existing != null) {
                // Check compatibility if attribute already exists
                if (checkCompatibility(imported, existing)) {
                    LOGGER.info("Attribute model \"{}\" already exists and is compatible with imported one.",
                                imported.getName());
                    // Replace with existing
                    modelAtt.setAttribute(existing);
                } else {
                    String format = "Attribute model \"%s\" already exists but is not compatible with imported one.";
                    String errorMessage = String.format(format, imported.getName());
                    LOGGER.error(errorMessage);
                    throw new ImportException(errorMessage);
                }
            } else {
                // Create attribute
                attributeModelService.addAttribute(modelAtt.getAttribute(), true);
            }
            // Bind attribute to model
            // but before lets check correctness because of PluginConfiguration
            switch (modelAtt.getMode()) {
                case GIVEN:
                    modelAtt.setComputationConf(null);
                    break;
                case COMPUTED:
                    modelAtt.setComputationConf(
                            pluginService.getPluginConfigurationByLabel(modelAtt.getComputationConf().getLabel()));
                    break;
                default:
                    throw new IllegalArgumentException(
                            modelAtt.getMode() + " is not a handled value of " + ComputationMode.class.getName()
                                    + " in " + getClass().getName());
            }
            //we have to check if it already exists because of logic to add modelAttrAssocs when we are adding a new attribute to a fragment
            modelAttributeRepository.save(modelAtt);

            addToFragment(fragmentAttMap, modelAtt.getAttribute());
        }

        for (Map.Entry<String, List<AttributeModel>> entry : fragmentAttMap.entrySet()) {
            if (!containsExactly(entry.getKey(), entry.getValue())) {
                String errorMessage = String
                        .format("Imported fragment \"%s\" not compatible with existing one.", entry.getKey());
                LOGGER.error(errorMessage);
                throw new ImportException(errorMessage);
            }
        }
    }

    /**
     * At the moment, compatibility check only compares {@link AttributeType}
     *
     * @param pImported imported {@link AttributeModel}
     * @param pExisting existing {@link AttributeModel}
     * @return true is {@link AttributeModel}s are compatible.
     */
    private boolean checkCompatibility(AttributeModel pImported, AttributeModel pExisting) {
        return pImported.getType().equals(pExisting.getType());
    }

    /**
     * Build fragment map
     *
     * @param pFragmentAttMap {@link Fragment} map
     * @param pAttributeModel {@link AttributeModel} to dispatch
     */
    private void addToFragment(Map<String, List<AttributeModel>> pFragmentAttMap, AttributeModel pAttributeModel) {
        // Nothing to do for default fragment
        if (pAttributeModel.getFragment().isDefaultFragment()) {
            return;
        }

        String fragmentName = pAttributeModel.getFragment().getName();
        List<AttributeModel> fragmentAtts = pFragmentAttMap.get(fragmentName);
        if (fragmentAtts != null) {
            fragmentAtts.add(pAttributeModel);
        } else {
            fragmentAtts = new ArrayList<>();
            fragmentAtts.add(pAttributeModel);
            pFragmentAttMap.put(fragmentName, fragmentAtts);
        }
    }

    /**
     * Check if imported fragment contains the same attributes as existing one
     *
     * @param pFragmentName {@link Fragment} name
     * @param pAttModels list of imported fragment {@link AttributeModel}
     * @return true if existing fragment {@link AttributeModel} match with this ones.
     * @throws ModuleException if error occurs!
     */
    private boolean containsExactly(String pFragmentName, List<AttributeModel> pAttModels) throws ModuleException {
        // Get existing fragment attributes
        List<AttributeModel> existingAttModels = attributeModelService.findByFragmentName(pFragmentName);

        // Check size
        if (pAttModels.size() != existingAttModels.size()) {
            LOGGER.error(String.format("Existing fragment \"%s\" contains exactly %s unique attributes (not %s).",
                                       pFragmentName, existingAttModels.size(), pAttModels.size()));
            return false;
        }

        // Check attributes
        for (AttributeModel attMod : pAttModels) {
            if (!pFragmentName.equals(attMod.getFragment().getName())) {
                LOGGER.error(
                        String.format("Attribute \"%s\" not part of fragment \"%s\" but \"%s\".)", attMod.getName(),
                                      pFragmentName, attMod.getFragment().getName()));
                return false;
            }

            if (!existingAttModels.contains(attMod)) {
                LOGGER.error(
                        String.format("Unknown attribute \"%s\" in fragment \"%s\".", attMod.getName(), pFragmentName));
                return false;
            }
        }

        return true;
    }

    @Override
    public Set<ModelAttrAssoc> getComputedAttributes(Long pId) {
        List<ModelAttrAssoc> attributes = modelAttributeRepository.findByModelId(pId);
        return attributes.stream().filter(attr -> ComputationMode.COMPUTED.equals(attr.getMode()))
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<ModelAttrAssoc> getModelAttrAssocsFor(EntityType pType) {
        if (pType != null) {
            Collection<Model> models = getModels(pType);
            Collection<Long> modelsIds = Collections2.transform(models, (model -> model.getId()));
            return modelAttributeRepository.findAllByModelIdIn(modelsIds);
        } else {
            return modelAttributeRepository.findAll();
        }
    }

    @Override
    public void onApplicationEvent(NewFragmentAttributeEvent event) {
        updateNSBind((AttributeModel) event.getSource());
    }

    @Override
    public List<TypeMetadataConfMapping> retrievePossibleMappingsForComputed() {
        // For each attribute type lets determine which conf is usable, then lets handle metadata
        SetMultimap<AttributeType, PluginConfiguration> typeConfMappings = HashMultimap.create();
        // We want to return possible mappings for all types even if there is none
        AttributeType[] attributeTypes = AttributeType.values();
        for (AttributeType type : attributeTypes) {
            typeConfMappings.putAll(type, Sets.newHashSet());
        }
        // Lets get all confs for IComputedAttribute and split them according to there supported type
        List<PluginConfiguration> computationConfs = pluginService
                .getPluginConfigurationsByType(IComputedAttribute.class);
        for (PluginConfiguration conf : computationConfs) {
            try {
                IComputedAttribute plugin = pluginService.getPlugin(conf.getId());
                typeConfMappings.put(plugin.getSupported(), conf);
            } catch (ModuleException e) {
                // thrown if no configuration with id: conf.getId() exists: CANNOT BE THE CASE FOR US. And even if it happens we don't care here
            }
        }

        SetMultimap<AttributeType, PluginMetaData> typeMetadataMappings = HashMultimap.create();
        List<PluginMetaData> pluginMetadata = pluginService.getPluginsByType(IComputedAttribute.class);
        // Now let worry about metadata
        // We want to return possible mappings for all types even if there is none
        for (AttributeType type : attributeTypes) {
            typeMetadataMappings.putAll(type, Sets.newHashSet());
        }
        // For each metadata, lets retrieve the right type by instantiating the plugin by hand
        for (PluginMetaData metaData : pluginMetadata) {
            try {
                IComputedAttribute plugin = (IComputedAttribute) Class.forName(metaData.getPluginClassName())
                        .newInstance();
                typeMetadataMappings.put(plugin.getSupported(), metaData);
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                // ClassNotFound is already covered by the getPluginsByType method
                LOGGER.warn("Plugin class with name {} couldn't be instantiated", metaData.getPluginClassName());
            }
        }

        //now lets merge those two Multimap into one ugly list containing the type
        return createMappingAsList(typeConfMappings, typeMetadataMappings);
    }

    private List<TypeMetadataConfMapping> createMappingAsList(
            SetMultimap<AttributeType, PluginConfiguration> typeConfMappings,
            SetMultimap<AttributeType, PluginMetaData> typeMetadataMappings) {
        List<TypeMetadataConfMapping> mapping = Lists.newArrayList();
        for (AttributeType type : AttributeType.values()) {
            mapping.add(new TypeMetadataConfMapping(type, typeConfMappings.get(type), typeMetadataMappings.get(type)));
        }
        return mapping;
    }
}
