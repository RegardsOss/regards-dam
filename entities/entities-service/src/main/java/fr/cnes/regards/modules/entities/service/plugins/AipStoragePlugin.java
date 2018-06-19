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
package fr.cnes.regards.modules.entities.service.plugins;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import fr.cnes.regards.framework.feign.security.FeignSecurityManager;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.DataType;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.framework.utils.file.ChecksumUtils;
import fr.cnes.regards.modules.entities.domain.AbstractDescEntity;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.Document;
import fr.cnes.regards.modules.entities.domain.EntityAipState;
import fr.cnes.regards.modules.entities.service.IStorageService;
import fr.cnes.regards.modules.indexer.domain.DataFile;
import fr.cnes.regards.modules.project.client.rest.IProjectsClient;
import fr.cnes.regards.modules.storage.client.IAipClient;
import fr.cnes.regards.modules.storage.domain.AIP;
import fr.cnes.regards.modules.storage.domain.AIPBuilder;
import fr.cnes.regards.modules.storage.domain.AIPCollection;
import fr.cnes.regards.modules.storage.domain.RejectedAip;

/**
 * @author Sylvain Vissiere-Guerinet
 * @author Christophe Mertz
 */
@Plugin(description = "This plugin allows to POST AIP entities to storage unit", id = "AipStoragePlugin",
        version = "1.0.0", author = "REGARDS Team", contact = "regards@c-s.fr", licence = "LGPLv3.0", owner = "CSSI",
        url = "https://github.com/RegardsOss")
public class AipStoragePlugin implements IStorageService {

    private final static Logger LOGGER = LoggerFactory.getLogger(AipStoragePlugin.class);

    private final static String MD5 = "MD5";

    private final static String SLASH = "/";

    private final static String PATH_COLLECTIONS = "/collections/";

    private final static String PATH_DATASETS = "/datasets/";

    private final static String PATH_DOCUMENTS = "/documents/";

    private final static String REGARDS_DESCRIPTION = "REGARDS description";

    private final static String PATH_FILE = "/file/";

    private final String SCOPE_PARAM = "?scope=";

    @Autowired
    private IAipClient aipClient;

    @Autowired
    private IProjectsClient projectsClient;

    /**
     * {@link IRuntimeTenantResolver} instance
     */
    @Autowired
    private IRuntimeTenantResolver runtimeTenantResolver;

    @Value("${zuul.prefix}")
    private String gatewayPrefix;

    @Value("${spring.application.name}")
    private String microserviceName;

    @Autowired
    private Gson gson;

    @Override
    public <T extends AbstractEntity> T storeAIP(T entity) {

        try {
            AIPCollection collection = new AIPCollection();

            collection.add(getBuilder(entity).build());
            ResponseEntity<List<RejectedAip>> response = aipClient.store(collection);
            handleClientAIPResponse(response.getStatusCode(), entity, response.getBody());
        } catch (ModuleException e) {
            LOGGER.error("The AIP entity {} can not be stored by microservice storage", entity.getIpId(), e);
            entity.setStateAip(EntityAipState.AIP_STORE_ERROR);
        } catch (HttpClientErrorException e) {
            // Handle non 2xx or 404 status code
            List<RejectedAip> rejectedAips = null;
            if (e.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
                @SuppressWarnings("serial")
                TypeToken<List<RejectedAip>> bodyTypeToken = new TypeToken<List<RejectedAip>>() {
                };
                rejectedAips = gson.fromJson(e.getResponseBodyAsString(), bodyTypeToken.getType());
            }
            handleClientAIPResponse(e.getStatusCode(), entity, rejectedAips);
        }

        return entity;
    }

    @Override
    public <T extends AbstractEntity> T updateAIP(T entity) {

        ResponseEntity<AIP> response;
        try {
            response = aipClient.updateAip(entity.getIpId().toString(), getBuilder(entity).build());
            handleClientAIPResponse(response.getStatusCode(), entity, response.getBody());
        } catch (ModuleException e) {
            LOGGER.error("The AIP entity {} can not be updated by microservice storage", entity.getIpId(), e);
            entity.setStateAip(EntityAipState.AIP_STORE_ERROR);
        }

        return entity;
    }

    @Override
    public void deleteAIP(AbstractEntity pToDelete) {
    }

    /**
     * Build an {@link AIPBuilder} for an entity that can be
     * a {@link EntityType#COLLECTION}, {@link EntityType#DATASET} or {@link EntityType#DOCUMENT}.
     * 
     * @param entity the {@link AbstractEntity} for which to build an {@link AIPBuilder}
     * @return the created {@link AIPBuilder}
     * @throws ModuleException 
     */
    private <T extends AbstractEntity> AIPBuilder getBuilder(T entity) throws ModuleException {
        AIPBuilder builder = new AIPBuilder(entity.getIpId(), entity.getSipId(), EntityType.valueOf(entity.getType()));

        extractEntity(builder, entity);

        if (entity instanceof Dataset) {
            extractDataset(builder, (Dataset) entity);
        } else if (entity instanceof Document) {
            extractDocument(builder, (Document) entity);
        } else if (entity instanceof Collection) {
            extractCollection(builder, (Collection) entity);
        }

        return builder;
    }

    /**
     * Populates the {@link AIPBuilder} for an {@link AbstractEntity}
     * @param builder the current {@link AIPBuilder}
     * @param entity the current {@link AbstractEntity}
     */
    private void extractEntity(AIPBuilder builder, AbstractEntity entity) {
        if (entity.getTags() != null && entity.getTags().size() > 0) {
            builder.addTags(entity.getTags().toArray(new String[entity.getTags().size()]));
        }

        if (entity.getCreationDate() != null) {
            builder.addEvent("AIP creation", entity.getCreationDate());
        }

        if (entity.getLastUpdate() != null) {
            builder.addEvent("AIP modification", entity.getLastUpdate());
        }

        builder.addDescriptiveInformation("label", entity.getLabel());
        if (entity.getProperties() != null && entity.getProperties().size() > 0) {
            entity.getProperties().stream().forEach(attr -> {
                builder.addDescriptiveInformation(attr.getName(), gson.toJson(attr.getValue()));
            });
        }

        // FIXME
        // builder.setGeometry(entity.getGeometry());
    }

    /**
     * Populates the {@link AIPBuilder} for a {@link Document}
     * @param builder the current {@link AIPBuilder}
     * @param document the current {@link Document}
     * @throws ModuleException 
     */
    private void extractDocument(AIPBuilder builder, Document document) throws ModuleException {
        for (DataFile df : document.getDocumentFiles()) {

            try {
                builder.getContentInformationBuilder().setDataObject(DataType.DOCUMENT, df.getName(),
                                                                     df.getDigestAlgorithm(), df.getChecksum(),
                                                                     df.getSize(), df.getUri().toURL());
                builder.getContentInformationBuilder().setSyntax(df.getMimeType().toString(), "", df.getMimeType());
                builder.addContentInformation();
            } catch (MalformedURLException e) {
                throw new ModuleException(e);
            }
        }
    }

    /**
     * Populates the {@link AIPBuilder} for a {@link Collection}
     * @param builder the current {@link AIPBuilder}
     * @param collection the current {@link Collection}
     * @throws ModuleException 
     */
    private void extractCollection(AIPBuilder builder, Collection collection) throws ModuleException {
        extractDescription(builder, collection);
    }

    /**
     * Populates the {@link AIPBuilder} for a {@link Dataset}
     * @param builder the current {@link AIPBuilder}
     * @param document the current {@link Dataset}
     * @throws ModuleException 
     */
    private void extractDataset(AIPBuilder builder, Dataset dataSet) throws ModuleException {
        builder.addContextInformation("score", dataSet.getScore());

        if (!Strings.isNullOrEmpty(dataSet.getLicence())) {
            builder.addDescriptiveInformation("licence", dataSet.getLicence());
        }
        if (dataSet.getQuotations() != null && dataSet.getQuotations().size() > 0) {
            builder.addDescriptiveInformation("quotations", gson.toJson(dataSet.getQuotations()));
        }
        extractDescription(builder, dataSet);
    }

    private <T extends AbstractDescEntity> void extractDescription(AIPBuilder builder, T entity)
            throws ModuleException {
        if (entity.getDescriptionFile() == null) {
            return;
        }

        if (entity.getDescriptionFile().getUrl() != null) {
            builder.addDescriptiveInformation("URL_DESCRIPTION",
                                              gson.toJson(entity.getDescriptionFile().getUrl().toString()));

        } else if (entity.getDescriptionFile().getContent() != null) {
            try (ByteArrayInputStream in = new ByteArrayInputStream(entity.getDescriptionFile().getContent())) {
                String fCheckSum = ChecksumUtils.computeHexChecksum(in, MD5);

                URL url = toPublicDescription(entity.getIpId());

                builder.getContentInformationBuilder().setDataObject(DataType.DOCUMENT, url, MD5, fCheckSum);

                builder.getContentInformationBuilder()
                        .setSyntax(entity.getDescriptionFile().getType().getType().toString(), REGARDS_DESCRIPTION,
                                   entity.getDescriptionFile().getType());
                builder.addContentInformation();

            } catch (NoSuchAlgorithmException | IOException e) {
                throw new ModuleException(e);
            }
        }
    }

    private URL toPublicDescription(UniformResourceName owningAip) throws MalformedURLException {
        // Lets reconstruct the public url of rs-dam
        // First lets get the public hostname from rs-admin-instance
        FeignSecurityManager.asSystem();
        String projectHost = projectsClient.retrieveProject(runtimeTenantResolver.getTenant()).getBody().getContent()
                .getHost();
        FeignSecurityManager.reset();
        // now lets add it the gateway prefix and the microservice name and the endpoint path to it
        StringBuilder sb = new StringBuilder();
        sb.append(projectHost);
        sb.append(SLASH);
        sb.append(gatewayPrefix);
        sb.append(SLASH);
        sb.append(microserviceName);
        sb.append(SLASH);
        
        if (owningAip.getEntityType().equals(EntityType.COLLECTION)) {
            sb.append(PATH_COLLECTIONS);
        } else if (owningAip.getEntityType().equals(EntityType.DATASET)) {
            sb.append(PATH_DATASETS);
        } else if (owningAip.getEntityType().equals(EntityType.DOCUMENT)) {
            sb.append(PATH_DOCUMENTS);
        }

        sb.append(owningAip.toString());
        sb.append(PATH_FILE);
        sb.append(SCOPE_PARAM);
        sb.append(runtimeTenantResolver.getTenant());
        URL downloadUrl = new URL(sb.toString());
        return downloadUrl;
    }

    private void handleClientAIPResponse(HttpStatus status, AbstractEntity entity, List<RejectedAip> rejectedAips) {
        LOGGER.info("status=" + status.toString());
        switch (status) {
            case CREATED:
                LOGGER.info("update entity state set to AIP_STORE_OK:" + entity.getIpId());
                entity.setStateAip(EntityAipState.AIP_STORE_PENDING);
                break;
            case PARTIAL_CONTENT:
            case UNPROCESSABLE_ENTITY:
                // Some AIP are rejected
                if (rejectedAips != null) {
                    rejectedAips.stream().filter(r -> r.getIpId().equals(entity.getIpId().toString())).forEach(r -> {
                        LOGGER.error("{} : update entity state set to AIP_STORE_ERROR for reason : {}",
                                     entity.getIpId(), r.getRejectionCauses().get(0));
                        entity.setStateAip(EntityAipState.AIP_STORE_ERROR);
                    });
                }
                break;
            default:
                break;
        }
    }

    private void handleClientAIPResponse(HttpStatus status, AbstractEntity entity, AIP aip) {
        LOGGER.info("status=" + status.toString());
        switch (status) {
            case CREATED:
                LOGGER.info("{} : entity state set to AIP_STORE_PENDING", entity.getIpId());
                entity.setStateAip(EntityAipState.AIP_STORE_PENDING);
                break;
            case PARTIAL_CONTENT:
            case UNPROCESSABLE_ENTITY:
                // Some AIP are rejected
                if (aip != null) {
                    if (aip.getId().equals(entity.getIpId())) {
                        LOGGER.error("{} : entity state set to AIP_STORE_ERROR", entity.getIpId());
                        entity.setStateAip(EntityAipState.AIP_STORE_ERROR);
                    }
                }
                break;
            default:
                break;
        }
    }

}
