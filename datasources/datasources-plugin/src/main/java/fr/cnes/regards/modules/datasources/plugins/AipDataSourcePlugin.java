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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
import fr.cnes.regards.framework.utils.plugins.PluginUtilsRuntimeException;
import fr.cnes.regards.modules.datasources.plugins.exception.DataSourceException;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourcePlugin;
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
public class AipDataSourcePlugin implements IDataSourcePlugin {

    public final static String MODEL_NAME_PARAM = "model name";

    public final static String BINDING_MAP = "binding map";

    private static final Logger LOGGER = LoggerFactory.getLogger(AipDataSourcePlugin.class);

    @PluginParameter(name = MODEL_NAME_PARAM, label = "model name", description = "Associated data source model name")
    private String modelName;

    @PluginParameter(name = BINDING_MAP, keylabel = "AIP property path", label = "Attribute path",
            description = "Binding map betwwen AIP and model ie property chain from AIP format and its associated property chain from model")
    private Map<String, String> bindingMap;

    @Autowired
    private IModelService modelService;

    @Autowired
    private IModelAttrAssocService modelAttrAssocService;

    @Autowired
    private IAipClient aipClient;

    private Model model;

    /**
     * Association table between json path property and its type from model
     */
    private final Map<String, AttributeType> modelMappingMap = new HashMap<>();

    /**
     * Ingestion refresh rate in seconds
     */
    @PluginParameter(name = REFRESH_RATE, defaultValue = REFRESH_RATE_DEFAULT_VALUE_AS_STRING, optional = true,
            label = "refresh rate",
            description = "Ingestion refresh rate in seconds (minimum delay between two consecutive ingestions)")
    private Integer refreshRate;

    /**
     * Init method
     */
    @PluginInit
    private void initPlugin() throws ModuleException {
        this.model = modelService.getModelByName(modelName);
        if (this.model == null) {
            throw new ModuleException(String.format("Model '%s' does not exist.", modelName));
        }

        List<ModelAttrAssoc> modelAttrAssocs = modelAttrAssocService.getModelAttrAssocs(this.model.getId());
        // Fill map { "properties.titi.tutu", AttributeType.STRING }
        for (ModelAttrAssoc assoc : modelAttrAssocs) {
            modelMappingMap.put(assoc.getAttribute().buildJsonPath(StaticProperties.PROPERTIES),
                                assoc.getAttribute().getType());
        }
        // All bindingMap values should be json path properties from model so each of them starting with "properties."
        // must exist as a value into modelMappingMap
        Set<String> notInModelProperties = bindingMap.values().stream().filter(name -> name.startsWith("properties."))
                .filter(name -> !modelMappingMap.containsKey(name)).collect(Collectors.toSet());
        if (!notInModelProperties.isEmpty()) {
            throw new ModuleException(
                    "Following properties don't exist into model : " + Joiner.on(", ").join(notInModelProperties));
        }
        DataObject forIntrospection = new DataObject();
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        Set<String> notInModelStaticProperties = bindingMap.values().stream()
                .filter(name -> !name.startsWith("properties."))
                .filter(name -> !propertyUtilsBean.isWriteable(forIntrospection, name)).collect(Collectors.toSet());
        if (!notInModelStaticProperties.isEmpty()) {
            throw new ModuleException(
                    "Following static properties don't exist : " + Joiner.on(", ").join(notInModelProperties));
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
                .retrieveAipDataFiles(AIPState.STORED, Collections.singleton(this.model.getName()), date,
                                      pageable.getPageNumber(), pageable.getPageSize());
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
            obj.getFiles().put(dataFileDto.getDataType(), dataFile);
        }

        // Tags
        obj.getTags().addAll(aip.getTags());

        // Binded properties
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        for (Map.Entry<String, String> entry : bindingMap.entrySet()) {
            String aipPropertyPath = entry.getKey();
            String doPropertyPath = entry.getValue();

            // Value from AIP
            Object value = propertyUtilsBean.getNestedProperty(aip, aipPropertyPath);

            // Does property refers to a dynamic ("properties....") or static property ?
            if (!doPropertyPath.startsWith("properties.")) {
                // Static, use propertyUtilsBean
                propertyUtilsBean.setNestedProperty(obj, doPropertyPath, value);
            } else { // Dynamic
                String dynamicPropertyPath = doPropertyPath.substring(doPropertyPath.indexOf('.') + 1);
                // Property name in all cases (fragment or not)
                String propName = dynamicPropertyPath.substring(dynamicPropertyPath.indexOf('.') + 1);
                AbstractAttribute<?> propAtt = AttributeBuilder.forType(modelMappingMap.get(doPropertyPath), propName,
                                                                        value);
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
}
