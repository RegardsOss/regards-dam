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
package fr.cnes.regards.modules.datasources.plugins;

import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedResources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.google.common.base.Joiner;
import fr.cnes.regards.framework.feign.security.FeignSecurityManager;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInit;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.framework.utils.RsRuntimeException;
import fr.cnes.regards.framework.utils.plugins.PluginUtilsRuntimeException;
import fr.cnes.regards.modules.datasources.plugins.exception.DataSourceException;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IAipDataSourcePlugin;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.StaticProperties;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.ObjectAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.builder.AttributeBuilder;
import fr.cnes.regards.modules.indexer.domain.DataFile;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.service.IModelAttrAssocService;
import fr.cnes.regards.modules.models.service.IModelService;
import fr.cnes.regards.modules.storage.client.IAipClient;
import fr.cnes.regards.modules.storage.domain.AIP;
import fr.cnes.regards.modules.storage.domain.AIPState;
import fr.cnes.regards.modules.storage.domain.AipDataFiles;
import fr.cnes.regards.modules.storage.domain.DataFileDto;

/**
 * @author oroussel
 */
@Plugin(id = "aip-storage-datasource", version = "1.0-SNAPSHOT",
        description = "Allows data extraction from AIP storage", author = "REGARDS Team", contact = "regards@c-s.fr",
        licence = "LGPLv3.0", owner = "CSSI", url = "https://github.com/RegardsOss")
public class AipDataSourcePlugin implements IAipDataSourcePlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(AipDataSourcePlugin.class);

    public static final String SUBSETTING_TAGS = "subsettingTags";

    @PluginParameter(name = MODEL_NAME_PARAM, label = "model name", description = "Associated data source model name")
    private String modelName;

    @PluginParameter(name = SUBSETTING_TAGS, label = "Subsetting tags", optional = true,
            description = "The plugin will fetch data storage to find AIPs tagged with these specified tags to obtain an AIP subset. If no tag is specified, plugin will fetch all the available AIPs.")
    private Set<String> subsettingTags;

    @PluginParameter(name = BINDING_MAP, keylabel = "Model property path", label = "AIP property path",
            description = "Binding map between model and AIP (i.e. Property chain from model and its associated property chain from AIP format")
    private Map<String, String> bindingMap;

    @PluginParameter(name = TAGS, label = "data objects common tags", optional = true,
            description = "Common tags to be put on all data objects created by the data source")
    private final Collection<String> commonTags = Collections.emptyList();

    @Autowired
    private IModelService modelService;

    @Autowired
    private IModelAttrAssocService modelAttrAssocService;

    @Autowired
    private IAipClient aipClient;

    // FIXME resolving circular dependency
    // /**
    // * Unparameterized entity repository
    // */
    // @Autowired
    // protected AbstractEntityService<AbstractEntity> entityService;

    private Model model;

    /**
     * Association table between JSON path property and its type from model
     */
    private final Map<String, AttributeType> modelMappingMap = new HashMap<>();

    /**
     * Association table between JSON path property and its mapping values.<br/>
     * In general, single value is mapped.<br/>
     * For interval, two values has to be mapped.
     */
    private final Map<String, List<String>> modelBindingMap = new HashMap<>();

    /**
     * Ingestion refresh rate in seconds
     */
    @PluginParameter(name = REFRESH_RATE, defaultValue = REFRESH_RATE_DEFAULT_VALUE_AS_STRING, optional = true,
            label = "refresh rate",
            description = "Ingestion refresh rate in seconds (minimum delay between two consecutive ingestions)")
    private Integer refreshRate;

    /**
     * Initialize AIP properties resolver
     */
    private final PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();

    /**
     * Init method
     */
    @PluginInit
    private void initPlugin() throws ModuleException {
        this.model = modelService.getModelByName(modelName);
        if (this.model == null) {
            throw new ModuleException(String.format("Model '%s' does not exist.", modelName));
        }

        List<ModelAttrAssoc> modelAttrAssocs = modelAttrAssocService.getModelAttrAssocs(modelName);
        // Fill map { "properties.titi.tutu", AttributeType.STRING }
        for (ModelAttrAssoc assoc : modelAttrAssocs) {
            modelMappingMap.put(assoc.getAttribute().buildJsonPath(StaticProperties.PROPERTIES),
                                assoc.getAttribute().getType());
        }

        // Build binding map considering interval double mapping
        for (Map.Entry<String, String> entry : bindingMap.entrySet()) {
            if (entry.getKey().startsWith(PROPERTY_PREFIX)) {
                String doPropertyPath = entry.getKey();
                // Manage dynamic properties
                if (doPropertyPath.endsWith(LOWER_BOUND_SUFFIX)) {
                    // - interval lower bound
                    String modelKey = entry.getKey()
                            .substring(0, doPropertyPath.length() - LOWER_BOUND_SUFFIX.length());
                    if (modelBindingMap.containsKey(modelKey)) {
                        // Add lower bound value at index 0
                        modelBindingMap.get(modelKey).add(0, entry.getValue());
                    } else {
                        List<String> values = new ArrayList<>();
                        values.add(entry.getValue());
                        modelBindingMap.put(modelKey, values);
                    }
                } else if (doPropertyPath.endsWith(UPPER_BOUND_SUFFIX)) {
                    // - interval upper bound
                    String modelKey = entry.getKey()
                            .substring(0, doPropertyPath.length() - UPPER_BOUND_SUFFIX.length());
                    if (modelBindingMap.containsKey(modelKey)) {
                        // Add upper bound value at index 1
                        modelBindingMap.get(modelKey).add(entry.getValue());
                    } else {
                        List<String> values = new ArrayList<>();
                        values.add(entry.getValue());
                        modelBindingMap.put(modelKey, values);
                    }
                } else {
                    // - others : propagate properties
                    modelBindingMap.put(doPropertyPath, Arrays.asList(entry.getValue()));
                }
            } else {
                // Propagate properties
                modelBindingMap.put(entry.getKey(), Arrays.asList(entry.getValue()));
            }
        }

        // All bindingMap values should be JSON path properties from model so each of them starting with PROPERTY_PREFIX
        // must exist as a value into modelMappingMap
        Set<String> notInModelProperties = modelBindingMap.keySet().stream()
                .filter(name -> name.startsWith(PROPERTY_PREFIX)).filter(name -> !modelMappingMap.containsKey(name))
                .collect(Collectors.toSet());
        if (!notInModelProperties.isEmpty()) {
            throw new ModuleException(
                    "Following properties don't exist into model : " + Joiner.on(", ").join(notInModelProperties));
        }
        DataObject forIntrospection = new DataObject();
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        Set<String> notInModelStaticProperties = modelBindingMap.keySet().stream()
                .filter(name -> !name.startsWith(PROPERTY_PREFIX))
                .filter(name -> !propertyUtilsBean.isWriteable(forIntrospection, name)).collect(Collectors.toSet());
        if (!notInModelStaticProperties.isEmpty()) {
            throw new ModuleException(
                    "Following static properties don't exist : " + Joiner.on(", ").join(notInModelProperties));
        }

        // Check number of values mapped for each type
        for (Map.Entry<String, List<String>> entry : modelBindingMap.entrySet()) {
            if (entry.getKey().startsWith(PROPERTY_PREFIX)) {
                AttributeType attributeType = modelMappingMap.get(entry.getKey());
                if (attributeType.isInterval()) {
                    if (entry.getValue().size() != 2) {
                        throw new ModuleException(attributeType + " properties " + entry.getKey()
                                                          + " has to be mapped to exactly 2 values");
                    }
                } else {
                    if (entry.getValue().size() != 1) {
                        throw new ModuleException(attributeType + " properties " + entry.getKey()
                                                          + " has to be mapped to a single value");
                    }
                }
            }
        }
    }

    @Override
    public int getRefreshRate() {
        return refreshRate;
    }

    @Override
    public Page<DataObject> findAll(String tenant, Pageable pageable, OffsetDateTime date) throws DataSourceException {
        FeignSecurityManager.asSystem();
        ResponseEntity<PagedResources<AipDataFiles>> responseEntity = aipClient
                .retrieveAipDataFiles(AIPState.STORED, subsettingTags, date, pageable.getPageNumber(),
                                      pageable.getPageSize());
        FeignSecurityManager.reset();
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            List<DataObject> list = new ArrayList<>();
            for (AipDataFiles aipDataFiles : responseEntity.getBody().getContent()) {
                try {
                    list.add(createDataObject(aipDataFiles));
                } catch (URISyntaxException e) {
                    throw new DataSourceException("AIP dataObject url cannot be transformed in URI", e);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new PluginUtilsRuntimeException(e);
                }
            }
            return new PageImpl<>(list, pageable, list.size());
        } else {
            throw new DataSourceException(
                    "Error while calling storage client (HTTP STATUS : " + responseEntity.getStatusCode());
        }
    }

    private DataObject createDataObject(AipDataFiles aipDataFiles)
            throws URISyntaxException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        AIP aip = aipDataFiles.getAip();
        DataObject obj = new DataObject();
        // Mandatory properties
        obj.setModel(this.model);
        obj.setIpId(aip.getId());
        obj.setSipId(aip.getSipId());

        // Data files
        for (DataFileDto dataFileDto : aipDataFiles.getDataFiles()) {
            DataFile dataFile = new DataFile();
            // Cannot use BeanUtils.copyProperty because names or types are different....(thank you)
            dataFile.setUri(dataFileDto.getUrl().toURI());
            dataFile.setOnline(dataFileDto.isOnline());
            dataFile.setSize(dataFileDto.getFileSize());
            dataFile.setName(dataFileDto.getName());
            dataFile.setMimeType(dataFileDto.getMimeType());
            dataFile.setDigestAlgorithm(dataFileDto.getAlgorithm());
            dataFile.setChecksum(dataFileDto.getChecksum());
            dataFile.setImageHeight(dataFileDto.getHeight());
            dataFile.setImageWidth(dataFileDto.getWidth());
            obj.getFiles().put(dataFileDto.getDataType(), dataFile);
        }

        // Tags
        obj.getTags().addAll(commonTags);
        obj.getTags().addAll(translateTags(aip.getTags()));

        // Binded properties
        for (Map.Entry<String, List<String>> entry : modelBindingMap.entrySet()) {
            String doPropertyPath = entry.getKey();

            // Does property refers to a dynamic ("properties....") or static property ?
            if (!doPropertyPath.startsWith(PROPERTY_PREFIX)) {
                // Value from AIP
                Object value = getNestedProperty(aip, entry.getValue().get(0));
                // Static, use propertyUtilsBean
                propertyUtilsBean.setNestedProperty(obj, doPropertyPath, value);
            } else { // Dynamic
                String dynamicPropertyPath = doPropertyPath.substring(doPropertyPath.indexOf('.') + 1);
                // Property name in all cases (fragment or not)
                String propName = dynamicPropertyPath.substring(dynamicPropertyPath.indexOf('.') + 1);
                // Retrieve attribute type to manage interval specific value
                AttributeType attributeType = modelMappingMap.get(doPropertyPath);
                AbstractAttribute<?> propAtt;
                if (attributeType.isInterval()) {
                    // Values from AIP
                    String lowerBoundPropertyPath = entry.getValue().get(0);
                    Object lowerBound = getNestedProperty(aip, lowerBoundPropertyPath);
                    String upperBoundPropertyPath = entry.getValue().get(1);
                    Object upperBound = getNestedProperty(aip, upperBoundPropertyPath);
                    try {
                        propAtt = AttributeBuilder.forType(attributeType, propName, lowerBound, upperBound);
                    } catch (ClassCastException e) {
                        String msg = String.format("Cannot map %s and to %s (values %s and %s)", lowerBoundPropertyPath,
                                                   upperBoundPropertyPath, propName, lowerBound, upperBound);
                        throw new RsRuntimeException(msg, e);
                    }
                } else {
                    // Value from AIP
                    String propertyPath = entry.getValue().get(0);
                    Object value = getNestedProperty(aip, propertyPath);
                    try {
                        propAtt = AttributeBuilder.forType(attributeType, propName, value);
                    } catch (ClassCastException e) {
                        String msg = String.format("Cannot map %s to %s (value %s)", propertyPath, propName, value);
                        throw new RsRuntimeException(msg, e);
                    }
                }

                // If it contains another '.', there is a fragment
                if (dynamicPropertyPath.contains(".")) {
                    String fragmentName = dynamicPropertyPath.substring(0, dynamicPropertyPath.indexOf('.'));

                    Optional<AbstractAttribute<?>> opt = obj.getProperties().stream()
                            .filter(p -> p.getName().equals(fragmentName)).findAny();
                    ObjectAttribute fragmentAtt = opt.isPresent() ? (ObjectAttribute) opt.get() : null;
                    if (fragmentAtt == null) {
                        fragmentAtt = AttributeBuilder.buildObject(fragmentName, propAtt);
                    } else {
                        fragmentAtt.getValue().add(propAtt);
                    }
                    obj.addProperty(fragmentAtt);
                } else {
                    obj.addProperty(propAtt);
                }
            }
        }

        return obj;
    }

    /**
     * Get nested property managing null value
     */
    private Object getNestedProperty(AIP aip, String propertyJsonPath)
            throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Object value = null;
        try {
            value = propertyUtilsBean.getNestedProperty(aip, propertyJsonPath.trim());
        } catch (NestedNullException e) {
            LOGGER.debug("Property \"{}\" not found in AIP \"{}\"", propertyJsonPath, aip.getId());
        }
        return value;
    }

    /**
     * Translate AIP tags in entity tags if found!
     */
    private Collection<String> translateTags(Collection<String> aipTags) {
        return aipTags;
        // FIXME resolving circular dependency
        // Set<String> translatedTags = new java.util.HashSet<>();
        // if (aipTags != null) {
        // for (String tag : aipTags) {
        // Set<AbstractEntity> entities = entityService.findAllBySipId(tag);
        // if (entities.isEmpty()) {
        // // Propagate tag
        // translatedTags.add(tag);
        // } else {
        // // Translate tag
        // entities.forEach(entity -> translatedTags.add(entity.getIpId().toString()));
        // }
        // }
        // }
        // return translatedTags;
    }
}
