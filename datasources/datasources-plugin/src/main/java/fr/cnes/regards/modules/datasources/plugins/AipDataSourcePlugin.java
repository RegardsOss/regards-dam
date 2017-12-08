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

import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeType;

import fr.cnes.regards.framework.feign.security.FeignSecurityManager;
import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginInit;
import fr.cnes.regards.framework.modules.plugins.annotations.PluginParameter;
import fr.cnes.regards.framework.oais.ContentInformation;
import fr.cnes.regards.framework.oais.InformationPackageProperties;
import fr.cnes.regards.framework.oais.OAISDataObject;
import fr.cnes.regards.framework.utils.plugins.PluginUtilsRuntimeException;
import fr.cnes.regards.modules.datasources.plugins.exception.DataSourceException;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourcePlugin;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.StaticProperties;
import fr.cnes.regards.modules.indexer.domain.DataFile;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.service.IModelAttrAssocService;
import fr.cnes.regards.modules.models.service.IModelService;
import fr.cnes.regards.modules.storage.client.IAipClient;
import fr.cnes.regards.modules.storage.domain.AIP;
import fr.cnes.regards.modules.storage.domain.AIPState;

/**
 * @author oroussel
 */
@Plugin(id = "aip-storage-datasource", version = "1.0-SNAPSHOT",
        description = "Allows data extraction from AIP storage", author = "REGARDS Team",
        contact = "regards@c-s.fr", licence = "LGPLv3.0", owner = "CSSI", url = "https://github.com/RegardsOss")
public class AipDataSourcePlugin implements IDataSourcePlugin {
    public final static String MODEL_NAME_PARAM = "model name";

    public final static String BINDING_MAP = "binding map";

    private static final Logger LOGGER = LoggerFactory.getLogger(AipDataSourcePlugin.class);

    @PluginParameter(name = MODEL_NAME_PARAM, label = "model name", description = "Associated data source model name")
    private String modelName;

    @PluginParameter(name = BINDING_MAP, label = "Binding map",
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
     * Ingestion refresh rate in seconds
     */
    @PluginParameter(name = REFRESH_RATE, defaultValue = REFRESH_RATE_DEFAULT_VALUE, optional = true,
            label = "refresh rate",
            description = "Ingestion refresh rate in seconds (minimum delay between two consecutive ingestions)")

    private Integer refreshRate;

    /**
     * Init method
     */
    @PluginInit
    private void initPlugin() {
        this.model = modelService.getModelByName(modelName);
        if (this.model == null) {
            throw new PluginUtilsRuntimeException(String. format("Model '%s' does not exist.", modelName));
        }

        List<ModelAttrAssoc> modelAttrAssocs = modelAttrAssocService.getModelAttrAssocs(this.model.getId());
        // Create map { "toto.titi.tutu", AttributeType.STRING }
        Map<String, AttributeType> modelMappingMap = new HashMap<>();
        for (ModelAttrAssoc assoc : modelAttrAssocs) {
            modelMappingMap.put(assoc.getAttribute().buildJsonPath(StaticProperties.PROPERTIES), assoc.getAttribute().getType());
        }
    }

    @Override
    public int getRefreshRate() {
        return refreshRate;
    }

    @Override
    public Page<DataObject> findAll(String tenant, Pageable pageable, OffsetDateTime date) throws DataSourceException {
        if (this.model == null) {
            throw new DataSourceException("DataSource cannot be searched because associated model doe not exist");
        }
        FeignSecurityManager.asSystem();
        ResponseEntity<PagedResources<Resource<AIP>>> responseEntity = aipClient
                .retrieveAIPs(AIPState.STORED, date, null, pageable.getPageNumber(), pageable.getPageSize());
        FeignSecurityManager.reset();
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            PagedResources<Resource<AIP>> pagedResources = responseEntity.getBody();
            List<DataObject> list = new ArrayList<>();
            for (Resource<AIP> resource : pagedResources.getContent()) {
                try {
                    list.add(createDataObject(resource.getContent()));
                } catch (URISyntaxException e) {
                    throw new DataSourceException("AIP dataObject url cannot be transformed in URI", e);
                }
            }
            return new PageImpl<>(list, pageable, pagedResources.getMetadata().getTotalElements());

        } else {
            throw new DataSourceException(
                    "Error while calling storage client (HTTP STATUS : " + responseEntity.getStatusCode());
        }
    }

    private DataObject createDataObject(AIP aip) throws URISyntaxException {
        DataObject obj = new DataObject();
        obj.setModel(this.model);
        obj.setIpId(aip.getId());
//        obj.setLabel(aip.get); // TODO

        obj.setSipId(aip.getSipId());

        InformationPackageProperties ipp = aip.getProperties();
        for (ContentInformation ci : ipp.getContentInformations()) {
            // Data files
            OAISDataObject oaisDataObject = ci.getDataObject();
            DataFile dataFile = new DataFile();
            dataFile.setChecksum(oaisDataObject.getChecksum());
            dataFile.setDigestAlgorithm(oaisDataObject.getAlgorithm());
            dataFile.setMimeType(MimeType.valueOf(ci.getRepresentationInformation().getSyntax().getMimeType()));
            dataFile.setName(oaisDataObject.getFilename());
            dataFile.setSize(oaisDataObject.getFileSize());
//            dataFile.setOnline(false); // TODO Pourquoi ?
            dataFile.setUri(oaisDataObject.getUrl().toURI());
            // Add dataFile to "files" property (with type, it's a multimap)
            obj.getFiles().put(oaisDataObject.getRegardsDataType(), dataFile);
        }
        obj.getTags().addAll(aip.getTags());
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();


        return obj;
    }
}
