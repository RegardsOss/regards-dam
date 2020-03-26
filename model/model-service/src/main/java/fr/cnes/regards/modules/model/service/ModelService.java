/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.model.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.google.common.collect.Collections2;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.EntityAlreadyExistsException;
import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginMetaData;
import fr.cnes.regards.framework.modules.plugins.domain.parameter.IPluginParam;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.urn.EntityType;
import fr.cnes.regards.modules.model.dao.IModelAttrAssocRepository;
import fr.cnes.regards.modules.model.dao.IModelRepository;
import fr.cnes.regards.modules.model.domain.ComputationMode;
import fr.cnes.regards.modules.model.domain.ComputationPlugin;
import fr.cnes.regards.modules.model.domain.IComputedAttribute;
import fr.cnes.regards.modules.model.domain.Model;
import fr.cnes.regards.modules.model.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.model.domain.TypeMetadataConfMapping;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.model.domain.attributes.Fragment;
import fr.cnes.regards.modules.model.dto.event.ModelChangeEvent;
import fr.cnes.regards.modules.model.dto.properties.PropertyType;
import fr.cnes.regards.modules.model.service.event.ComputedAttributeModelEvent;
import fr.cnes.regards.modules.model.service.event.NewFragmentAttributeEvent;
import fr.cnes.regards.modules.model.service.exception.FragmentAttributeException;
import fr.cnes.regards.modules.model.service.exception.ImportException;
import fr.cnes.regards.modules.model.service.exception.UnexpectedModelAttributeException;
import fr.cnes.regards.modules.model.service.xml.IComputationPluginService;
import fr.cnes.regards.modules.model.service.xml.XmlExportHelper;
import fr.cnes.regards.modules.model.service.xml.XmlImportHelper;

/**
 * Manage model lifecycle
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

    /**
     * {@link IPluginService} instance
     */
    private final IPluginService pluginService;

    private final IComputationPluginService computationPluginService;

    /**
     * Application Event publisher to publish event inside the microservice
     */
    private final ApplicationEventPublisher appPublisher;

    /**
     * Publish for model changes
     */
    private final IPublisher publisher;

    public ModelService(IModelRepository modelRepository, IModelAttrAssocRepository modelAttrAssocRepository,
            IAttributeModelService attributeModelService, IPluginService pluginService,
            ApplicationEventPublisher appPublisher, IPublisher publisher,
            IComputationPluginService computationPluginService) {
        this.modelRepository = modelRepository;
        modelAttributeRepository = modelAttrAssocRepository;
        this.attributeModelService = attributeModelService;
        this.pluginService = pluginService;
        this.appPublisher = appPublisher;
        this.publisher = publisher;
        this.computationPluginService = computationPluginService;
    }

    @Override
    public List<Model> getModels(EntityType type) {
        Iterable<Model> models;
        if (type == null) {
            models = modelRepository.findAll();
        } else {
            models = modelRepository.findByType(type);
        }
        return models != null ? ImmutableList.copyOf(models) : Collections.emptyList();
    }

    @Override
    public Model createModel(Model model) throws ModuleException {
        if (model.isIdentifiable()) {
            throw new EntityInvalidException(
                    String.format("Model with name \"%s\" cannot have an identifier!", model.getName()));
        }
        Model modelFromDb = modelRepository.findByName(model.getName());
        if (modelFromDb != null) {
            throw new EntityAlreadyExistsException(
                    String.format("Model with name \"%s\" already exists!", model.getName()));
        }
        return modelRepository.save(model);
    }

    @Override
    public Model getModel(Long id) throws ModuleException {
        Optional<Model> modelOpt = modelRepository.findById(id);
        if (!modelOpt.isPresent()) {
            throw new EntityNotFoundException(id, Model.class);
        }
        return modelOpt.get();
    }

    @Override
    public Model getModelByName(String modelName) throws ModuleException {
        Model model = modelRepository.findByName(modelName);
        if (model == null) {
            throw new EntityNotFoundException(modelName, Model.class);
        }
        return model;
    }

    @Override
    public Model updateModel(String modelName, Model model) throws ModuleException {
        if (!model.isIdentifiable()) {
            throw new EntityNotFoundException(String.format("Unknown identifier for model \"%s\"", model.getName()));
        }
        if (!modelName.equals(model.getName())) {
            throw new EntityInconsistentIdentifierException(model.getId(), model.getId(), model.getClass());
        }
        if (!modelRepository.existsById(model.getId())) {
            throw new EntityNotFoundException(model.getId(), Model.class);
        }
        return modelRepository.save(model);
    }

    @Override
    public void deleteModel(String modelName) throws ModuleException {

        Model model = getModelByName(modelName);

        // Delete attribute associations
        List<ModelAttrAssoc> modelAttrAssocs = modelAttributeRepository.findByModelId(model.getId());
        modelAttributeRepository.deleteAll(modelAttrAssocs);
        modelRepository.deleteById(model.getId());
    }

    @Override
    public Model duplicateModel(String modelName, Model model) throws ModuleException {
        return duplicateModelAttrAssocs(modelName, createModel(model));
    }

    @Override
    public List<ModelAttrAssoc> getModelAttrAssocs(String modelName) {
        Iterable<ModelAttrAssoc> modelAttributes = modelAttributeRepository.findByModelName(modelName);
        if (modelAttributes != null) {
            return ImmutableList.copyOf(modelAttributes);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Page<AttributeModel> getAttributeModels(Set<String> modelNames, Pageable pageable) {
        if (modelNames.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        Page<ModelAttrAssoc> assocs = modelAttributeRepository.findAllByModelNameIn(modelNames, pageable);
        List<AttributeModel> atts = assocs.getContent().stream().map(assoc -> assoc.getAttribute())
                .collect(Collectors.toList());
        return new PageImpl<>(atts, pageable, assocs.getTotalElements());
    }

    @Override
    public ModelAttrAssoc bindAttributeToModel(String modelName, ModelAttrAssoc modelAttrAssoc) throws ModuleException {
        Model model = getModelByName(modelName);
        if (modelAttrAssoc.isIdentifiable()) {
            throw new EntityNotFoundException(modelAttrAssoc.getId(), ModelAttrAssoc.class);
        }
        // Do not bind attribute that is part of a fragment
        if (attributeModelService.isFragmentAttribute(modelAttrAssoc.getAttribute().getId())) {
            throw new FragmentAttributeException(modelAttrAssoc.getAttribute().getId());
        }
        // Do not rebind an attribute
        Iterable<ModelAttrAssoc> existingModelAtts = modelAttributeRepository.findByModelName(modelName);
        if (existingModelAtts != null) {
            for (ModelAttrAssoc modAtt : existingModelAtts) {
                if (modAtt.equals(modelAttrAssoc)) {
                    throw new EntityAlreadyExistsException(
                            String.format("Attribute %s already exists in model %s!", modAtt.getAttribute().getName(),
                                          model.getName()));
                }
            }
        }

        if (modelAttrAssoc.getComputationConf() != null) {
            eventualyCreateComputationConfiguration(modelAttrAssoc.getComputationConf());
        }

        modelAttrAssoc.setModel(model);
        // Publish model change
        publisher.publish(ModelChangeEvent.build(model.getName()));
        return modelAttributeRepository.save(modelAttrAssoc);
    }

    @Override
    public ModelAttrAssoc getModelAttrAssoc(String modelName, Long modelAttributeId) throws ModuleException {
        Optional<ModelAttrAssoc> modelAttOpt = modelAttributeRepository.findById(modelAttributeId);
        if (!modelAttOpt.isPresent()) {
            throw new EntityNotFoundException(modelAttributeId, ModelAttrAssoc.class);
        }
        if (!modelName.equals(modelAttOpt.get().getModel().getName())) {
            throw new UnexpectedModelAttributeException(modelAttOpt.get().getModel().getId(), modelAttributeId);
        }
        return modelAttOpt.get();
    }

    @Override
    public ModelAttrAssoc getModelAttrAssoc(Long modelId, AttributeModel attribute) {
        return modelAttributeRepository.findByModelIdAndAttribute(modelId, attribute);
    }

    @Override
    public ModelAttrAssoc updateModelAttribute(String modelName, Long attributeId, ModelAttrAssoc modelAttrAssoc)
            throws ModuleException {
        if (!modelAttrAssoc.isIdentifiable()) {
            throw new EntityNotFoundException(
                    String.format("Unknown identifier for model attribute \"%s\"", modelAttrAssoc.getId()));
        }
        if (!modelAttrAssoc.getId().equals(attributeId)) {
            throw new EntityInconsistentIdentifierException(attributeId, modelAttrAssoc.getId(), ModelAttrAssoc.class);
        }
        if (!modelAttributeRepository.existsById(attributeId)) {
            throw new EntityNotFoundException(attributeId, ModelAttrAssoc.class);
        }
        // Publish model change
        publisher.publish(ModelChangeEvent.build(modelAttrAssoc.getModel().getName()));
        modelAttributeRepository.save(modelAttrAssoc);
        // If associated attribute is a computed one, send and event (to compute all datasets values)
        if (modelAttrAssoc.getComputationConf() != null) {
            appPublisher.publishEvent(new ComputedAttributeModelEvent(modelAttrAssoc));
        }
        return modelAttrAssoc;
    }

    @Override
    public void unbindAttributeFromModel(String modelName, Long attributeId) throws ModuleException {
        ModelAttrAssoc modelAtt = getModelAttrAssoc(modelName, attributeId);
        // Do not bind attribute that is part of a fragment
        if (attributeModelService.isFragmentAttribute(modelAtt.getAttribute().getId())) {
            throw new FragmentAttributeException(modelAtt.getAttribute().getId());
        }
        modelAttributeRepository.deleteById(attributeId);
        // Publish model change
        publisher.publish(ModelChangeEvent.build(modelName));
    }

    @Override
    public List<ModelAttrAssoc> bindNSAttributeToModel(String modelName, Fragment fragment) throws ModuleException {
        List<ModelAttrAssoc> modAtts = new ArrayList<>();
        Model model = getModelByName(modelName);
        Iterable<ModelAttrAssoc> existingModelAtts = modelAttributeRepository.findByModelName(modelName);
        Long pFragmentId = fragment.getId();

        // Check if fragment not already bound
        if (!isBoundFragment(existingModelAtts, pFragmentId)) {

            // Retrieve fragment attributes
            List<AttributeModel> attModels = attributeModelService.findByFragmentId(pFragmentId);

            if (attModels != null) {
                for (AttributeModel attModel : attModels) {
                    // Create model attributes to link base attributes
                    ModelAttrAssoc modelAtt = new ModelAttrAssoc();
                    modelAtt.setAttribute(attModel);
                    modelAtt.setModel(model);
                    modelAttributeRepository.save(modelAtt);
                    modAtts.add(modelAtt);
                }
            }
        } else {
            LOGGER.warn("Fragment {} already bound to model {}", pFragmentId, modelName);
        }

        // Publish model change
        publisher.publish(ModelChangeEvent.build(modelName));
        return modAtts;
    }

    /**
     * Check if fragment is bounded to the model
     * @param modelAtts model attributes
     * @param fragmentId fragment identifier
     * @return true if fragment is bound
     */
    private boolean isBoundFragment(Iterable<ModelAttrAssoc> modelAtts, Long fragmentId) {
        if (modelAtts != null) {
            for (ModelAttrAssoc modelAtt : modelAtts) {
                if (fragmentId.equals(modelAtt.getAttribute().getFragment().getId())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void unbindNSAttributeToModel(String modelName, Long fragmentId) {
        Iterable<ModelAttrAssoc> modelAtts = modelAttributeRepository.findByModelName(modelName);
        if (modelAtts != null) {
            for (ModelAttrAssoc modelAtt : modelAtts) {
                if (fragmentId.equals(modelAtt.getAttribute().getFragment().getId())) {
                    modelAttributeRepository.delete(modelAtt);
                }
            }
        }
        // Publish model change
        publisher.publish(ModelChangeEvent.build(modelName));
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
            // Publish model change
            publisher.publish(ModelChangeEvent.build(model.getName()));
        }
    }

    @Override
    public Collection<ModelAttrAssoc> retrieveModelAttrAssocsByAttributeId(AttributeModel attr) {
        return modelAttributeRepository.findAllByAttributeId(attr.getId());
    }

    @Override
    public Model duplicateModelAttrAssocs(String sourceModelName, Model targetModel) throws ModuleException {
        // Retrieve all reference model attributes
        List<ModelAttrAssoc> modelAtts = getModelAttrAssocs(sourceModelName);
        if (modelAtts != null) {
            for (ModelAttrAssoc modelAtt : modelAtts) {
                // Computed model associations are not duplicated
                if (modelAtt.getMode() == ComputationMode.GIVEN) {
                    // Create model attributes to link base attributes
                    ModelAttrAssoc duplicatedModelAtt = new ModelAttrAssoc();
                    duplicatedModelAtt.setAttribute(modelAtt.getAttribute());
                    duplicatedModelAtt.setModel(targetModel);
                    modelAttributeRepository.save(duplicatedModelAtt);
                }
            }
        }
        return targetModel;
    }

    @Override
    public void exportModel(String modelName, OutputStream os) throws ModuleException {
        // Get model
        Model model = getModelByName(modelName);
        // Get all related attributes
        List<ModelAttrAssoc> modelAtts = getModelAttrAssocs(modelName);
        // Export fragment to output stream
        XmlExportHelper.exportModel(os, model, modelAtts);
    }

    @Override
    public Model importModel(InputStream is) throws ModuleException {
        // Import model from input stream
        List<ModelAttrAssoc> modelAtts = XmlImportHelper.importModel(is, computationPluginService);
        // Create model once
        Model newModel = createModel(modelAtts.get(0).getModel()); // List of model attributes cannot be empty here
        // Create or control model attributes
        addAllModelAttributes(modelAtts);
        // Return created model
        LOGGER.info("New model \"{}\" with version \"{}\" created", newModel.getName(), newModel.getVersion());
        return newModel;
    }

    private PluginConfiguration eventualyCreateComputationConfiguration(PluginConfiguration plgConf)
            throws ModuleException {
        // If plugin configuration already exists
        if (pluginService.exists(plgConf.getBusinessId())) {
            // New one must be consistent with existing one
            PluginConfiguration currentPlgConf = pluginService.getPluginConfiguration(plgConf.getBusinessId());
            // Lets check that we are talking about same plugin implementation
            if (!Objects.equals(plgConf, currentPlgConf)) {
                String msg = String.format("Computation plugin %s with businessId %s is inconsistent with existing one",
                                           plgConf.getLabel(), plgConf.getBusinessId());
                LOGGER.error(msg);
                throw new ImportException(msg);
            }
            // Now lets check that parameters are consistent
            for (IPluginParam param : plgConf.getParameters()) {
                String curValue = (String) currentPlgConf.getParameterValue(param.getName());
                // Plugin parameter found
                if (curValue != null) {
                    if (!Objects.equals(param.getValue(), curValue)) {
                        String msg = String
                                .format("Computation plugin %s whith businessId %s is inconsistent with existing one: "
                                        + "plugin parameter %s with value %s differs from existing value (%s)",
                                        plgConf.getLabel(), plgConf.getBusinessId(), param.getName(), curValue,
                                        param.getValue());
                        LOGGER.error(msg);
                        throw new ImportException(msg);
                    }
                } else { // Plugin parameter not found
                    String msg = String
                            .format("Computation plugin  %s with businessId %s is inconsistent with existing one: "
                                    + "no plugin parameter %s found", plgConf.getLabel(), plgConf.getBusinessId(),
                                    param.getName());
                    LOGGER.error(msg);
                    throw new ImportException(msg);
                }
            }
            // No need to save new one
            return currentPlgConf;
        } else {
            // Plugin is ok (new or consistent with previous one
            return pluginService.savePluginConfiguration(plgConf);
        }
    }

    /**
     * Add all {@link ModelAttrAssoc} related to a model
     * @param modelAtts list of {@link ModelAttrAssoc}
     * @throws ModuleException if error occurs!
     */
    @Override
    public void addAllModelAttributes(List<ModelAttrAssoc> modelAtts) throws ModuleException {

        // Keep fragment content to check fragment consistence
        Map<String, List<AttributeModel>> fragmentAttMap = new HashMap<>();

        for (ModelAttrAssoc modelAtt : modelAtts) {

            AttributeModel imported = modelAtt.getAttribute();

            AttributeModel existing = attributeModelService.findByNameAndFragmentName(imported.getName(),
                                                                                      imported.getFragment().getName());
            // Check if attribute already exists

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
                    modelAtt.setComputationConf(eventualyCreateComputationConfiguration(modelAtt.getComputationConf()));
                    break;
                default:
                    throw new IllegalArgumentException(modelAtt.getMode() + " is not a handled value of "
                            + ComputationMode.class.getName() + " in " + getClass().getName());
            }
            // we have to check if it already exists because of logic to add modelAttrAssocs when we are adding a new
            // attribute to a fragment
            modelAttributeRepository.save(modelAtt);

            addToFragment(fragmentAttMap, modelAtt.getAttribute());
        }

        for (Map.Entry<String, List<AttributeModel>> entry : fragmentAttMap.entrySet()) {
            if (!containsExactly(entry.getKey(), entry.getValue())) {
                String errorMessage = String.format("Imported fragment \"%s\" not compatible with existing one.",
                                                    entry.getKey());
                LOGGER.error(errorMessage);
                throw new ImportException(errorMessage);
            }
        }
    }

    /**
     * At the moment, compatibility check only compares {@link PropertyType}
     * @param imported imported {@link AttributeModel}
     * @param existing existing {@link AttributeModel}
     * @return true is {@link AttributeModel}s are compatible.
     */
    private boolean checkCompatibility(AttributeModel imported, AttributeModel existing) {
        return imported.getType().equals(existing.getType());
    }

    /**
     * Build fragment map
     * @param fragmentAttMap {@link Fragment} map
     * @param attributeModel {@link AttributeModel} to dispatch
     */
    private void addToFragment(Map<String, List<AttributeModel>> fragmentAttMap, AttributeModel attributeModel) {
        // Nothing to do for default fragment
        if (attributeModel.getFragment().isDefaultFragment()) {
            return;
        }

        String fragmentName = attributeModel.getFragment().getName();
        List<AttributeModel> fragmentAtts = fragmentAttMap.get(fragmentName);
        if (fragmentAtts != null) {
            fragmentAtts.add(attributeModel);
        } else {
            fragmentAtts = new ArrayList<>();
            fragmentAtts.add(attributeModel);
            fragmentAttMap.put(fragmentName, fragmentAtts);
        }
    }

    /**
     * Check if imported fragment contains the same attributes as existing one
     * @param fragmentName {@link Fragment} name
     * @param attModels list of imported fragment {@link AttributeModel}
     * @return true if existing fragment {@link AttributeModel} match with this ones.
     * @throws ModuleException if error occurs!
     */
    private boolean containsExactly(String fragmentName, List<AttributeModel> attModels) {
        // Get existing fragment attributes
        List<AttributeModel> existingAttModels = attributeModelService.findByFragmentName(fragmentName);

        // Check size
        if (attModels.size() != existingAttModels.size()) {
            LOGGER.error(String.format("Existing fragment \"%s\" contains exactly %s unique attributes (not %s).",
                                       fragmentName, existingAttModels.size(), attModels.size()));
            return false;
        }

        // Check attributes
        for (AttributeModel attMod : attModels) {
            if (!fragmentName.equals(attMod.getFragment().getName())) {
                LOGGER.error(String.format("Attribute \"%s\" not part of fragment \"%s\" but \"%s\".)",
                                           attMod.getName(), fragmentName, attMod.getFragment().getName()));
                return false;
            }

            if (!existingAttModels.contains(attMod)) {
                LOGGER.error(String.format("Unknown attribute \"%s\" in fragment \"%s\".", attMod.getName(),
                                           fragmentName));
                return false;
            }
        }

        return true;
    }

    @Override
    public Set<ModelAttrAssoc> getComputedAttributes(Long id) {
        List<ModelAttrAssoc> attributes = modelAttributeRepository.findByModelId(id);
        return attributes.stream().filter(attr -> ComputationMode.COMPUTED.equals(attr.getMode()))
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<ModelAttrAssoc> getModelAttrAssocsFor(EntityType type) {
        if (type == null) {
            return modelAttributeRepository.findAll();
        } else {
            Collection<Model> models = getModels(type);
            Collection<Long> modelsIds = Collections2.transform(models, model -> model.getId());
            return modelAttributeRepository.findAllByModelIdIn(modelsIds);
        }
    }

    @Override
    public void onApplicationEvent(NewFragmentAttributeEvent event) {
        updateNSBind((AttributeModel) event.getSource());
    }

    @Override
    public List<TypeMetadataConfMapping> retrievePossibleMappingsForComputed() {
        // For each attribute type lets determine which conf is usable, then lets handle metadata
        SetMultimap<PropertyType, PluginConfiguration> typeConfMappings = HashMultimap.create();
        // We want to return possible mappings for all types even if there is none
        PropertyType[] PropertyTypes = PropertyType.values();
        for (PropertyType type : PropertyTypes) {
            typeConfMappings.putAll(type, Sets.newHashSet());
        }
        // Lets get all confs for IComputedAttribute and split them according to there supported type
        List<PluginConfiguration> computationConfs = pluginService
                .getPluginConfigurationsByType(IComputedAttribute.class);
        for (PluginConfiguration conf : computationConfs) {
            // remove inactive configuration
            if (conf.isActive()) {
                try {
                    Class<?> computationPlugin = Class.forName(conf.getPluginClassName());
                    ComputationPlugin computationPluginAnnotation = computationPlugin
                            .getAnnotation(ComputationPlugin.class);
                    typeConfMappings.put(computationPluginAnnotation.supportedType(), conf);
                } catch (ClassNotFoundException e) {
                    // This is only possible in case a plugin is still configured but the implementation is no longer
                    // available
                    LOGGER.warn("Plugin class with name {} couldn't be found. Please check your available plugins or delete the configuration using it.",
                                conf.getPluginClassName());
                    LOGGER.debug(e.getMessage(), e);
                }
            }
        }

        SetMultimap<PropertyType, PluginMetaData> typeMetadataMappings = HashMultimap.create();
        List<PluginMetaData> pluginMetadata = pluginService.getPluginsByType(IComputedAttribute.class);
        // Now lets worry about metadata
        // We want to return possible mappings for all types even if there is none
        for (PropertyType type : PropertyTypes) {
            typeMetadataMappings.putAll(type, Sets.newHashSet());
        }
        // For each metadata, lets retrieve the right type by instantiating the plugin by hand
        for (PluginMetaData metaData : pluginMetadata) {
            try {
                Class<?> computationPlugin = Class.forName(metaData.getPluginClassName());
                ComputationPlugin computationPluginAnnotation = computationPlugin
                        .getAnnotation(ComputationPlugin.class);
                typeMetadataMappings.put(computationPluginAnnotation.supportedType(), metaData);
            } catch (ClassNotFoundException e) {
                // ClassNotFound is already covered by the getPluginsByType method
            }
        }

        // now lets merge those two Multimap into one ugly list containing the type
        return createMappingAsList(typeConfMappings, typeMetadataMappings);
    }

    private List<TypeMetadataConfMapping> createMappingAsList(
            SetMultimap<PropertyType, PluginConfiguration> typeConfMappings,
            SetMultimap<PropertyType, PluginMetaData> typeMetadataMappings) {
        List<TypeMetadataConfMapping> mapping = Lists.newArrayList();
        for (PropertyType type : PropertyType.values()) {
            mapping.add(new TypeMetadataConfMapping(type, typeConfMappings.get(type), typeMetadataMappings.get(type)));
        }
        return mapping;
    }

    @Override
    public boolean isDeletable(Model model) {
        // FIXME
        return false;
    }

}
