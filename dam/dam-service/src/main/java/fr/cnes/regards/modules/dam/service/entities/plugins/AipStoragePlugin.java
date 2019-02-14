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
package fr.cnes.regards.modules.dam.service.entities.plugins;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import feign.FeignException;
import fr.cnes.regards.framework.feign.security.FeignSecurityManager;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.builder.ContentInformationBuilder;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.dam.domain.entities.AbstractEntity;
import fr.cnes.regards.modules.dam.domain.entities.EntityAipState;
import fr.cnes.regards.modules.dam.service.entities.IStorageService;
import fr.cnes.regards.modules.indexer.domain.DataFile;
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

    /**
     * Prefix for static properties to avoid collision with dynamic ones
     */
    private static final String STATIC_PPTY_PREFIX = "@";

    private static final String STATIC_PPTY_LABEL = STATIC_PPTY_PREFIX + "label";

    @Autowired
    private IAipClient aipClient;

    @Value("${zuul.prefix}")
    private String gatewayPrefix;

    @Value("${spring.application.name}")
    private String microserviceName;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    @Autowired
    private Gson gson;

    @Override
    public <T extends AbstractEntity<?>> T storeAIP(T toPersist) {
        try {
            final AIPCollection collection = new AIPCollection();
            FeignSecurityManager.asSystem();

            collection.add(getBuilder(toPersist).build());

            final ResponseEntity<List<RejectedAip>> response = aipClient.store(collection);
            handleClientAIPResponse(response.getStatusCode(), toPersist, response.getBody());
        } catch (final ModuleException | FeignException | HttpServerErrorException e) {
            LOGGER.error("The AIP entity {} can not be store by microservice storage", toPersist.getIpId(), e);
            toPersist.setStateAip(EntityAipState.AIP_STORE_ERROR);
        } catch (final HttpClientErrorException e) {
            // Handle non 2xx or 404 status code
            List<RejectedAip> rejectedAips = null;
            if (e.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
                @SuppressWarnings("serial")
                final TypeToken<List<RejectedAip>> bodyTypeToken = new TypeToken<List<RejectedAip>>() {
                };
                rejectedAips = gson.fromJson(e.getResponseBodyAsString(), bodyTypeToken.getType());
            }
            handleClientAIPResponse(e.getStatusCode(), toPersist, rejectedAips);
        } finally {
            FeignSecurityManager.reset();
        }

        return toPersist;
    }

    @SuppressWarnings("serial")
    @Override
    public <T extends AbstractEntity<?>> T updateAIP(T toUpdate) {
        ResponseEntity<Void> response;
        try {
            FeignSecurityManager.asSystem();

            final AIP aip = getBuilder(toUpdate).build();

            response = aipClient.updateAip(toUpdate.getIpId().toString(), aip);
            handleClientAIPResponse(response.getStatusCode(), toUpdate, aip);
        } catch (HttpClientErrorException e) {
            HttpStatus responseStatus = e.getStatusCode();
            if (responseStatus == HttpStatus.UNPROCESSABLE_ENTITY) {
                TypeToken<List<RejectedAip>> bodyTypeToken = new TypeToken<List<RejectedAip>>() {
                };
                List<RejectedAip> rejectedAips = gson.fromJson(e.getResponseBodyAsString(), bodyTypeToken.getType());
                handleClientAIPResponse(responseStatus, toUpdate, rejectedAips);
            } else {
                LOGGER.error("The AIP entity {} can not be update by microservice storage", toUpdate.getIpId(), e);
                toUpdate.setStateAip(EntityAipState.AIP_STORE_ERROR);
            }
        } catch (ModuleException | FeignException | HttpServerErrorException e) {
            LOGGER.error("The AIP entity {} can not be update by microservice storage", toUpdate.getIpId(), e);
            toUpdate.setStateAip(EntityAipState.AIP_STORE_ERROR);
        } finally {
            FeignSecurityManager.reset();
        }

        return toUpdate;
    }

    @Override
    public void deleteAIP(AbstractEntity<?> toDelete) {
        FeignSecurityManager.asSystem();

        final ResponseEntity<String> response = aipClient.deleteAip(toDelete.getIpId().toString());
        handleClientAIPDeleteResponse(response.getStatusCode(), toDelete, response.getBody());
        FeignSecurityManager.reset();
    }

    /**
     * Build an {@link AIPBuilder} for an entity that can be a
     * {@link EntityType#COLLECTION}, {@link EntityType#DATASET} or
     * {@link EntityType#DOCUMENT}.
     *
     * @param entity the {@link AbstractEntity} for which to build an
     *               {@link AIPBuilder}
     * @return the created {@link AIPBuilder}
     * @throws ModuleException
     */
    private <T extends AbstractEntity<?>> AIPBuilder getBuilder(T entity) throws ModuleException {
        final AIPBuilder builder = new AIPBuilder(entity.getIpId(), Optional.empty(), entity.getProviderId(),
                entity.getFeature().getEntityType(), getSession(entity));

        if ((entity.getTags() != null) && (entity.getTags().size() > 0)) {
            builder.addTags(entity.getTags().toArray(new String[entity.getTags().size()]));
        }

        if (entity.getCreationDate() != null) {
            builder.addEvent("AIP creation", entity.getCreationDate());
        }

        if (entity.getLastUpdate() != null) {
            builder.addEvent("AIP modification", entity.getLastUpdate());
        }

        // Add static label
        builder.addDescriptiveInformation(STATIC_PPTY_LABEL, entity.getLabel());

        // Add dynamic properties
        if ((entity.getProperties() != null) && (entity.getProperties().size() > 0)) {
            entity.getProperties().stream().forEach(property -> {
                builder.addDescriptiveInformation(property.getName(), property.getValue());
            });
        }

        // Propagate geometry
        builder.setGeometry(entity.getNormalizedGeometry());

        // Propagate files
        if ((entity.getFiles() != null) && (entity.getFiles().size() > 0)) {
            try {
                for (final DataFile dataFile : entity.getFiles().values()) {
                    final ContentInformationBuilder ciBuilder = builder.getContentInformationBuilder();
                    // Add scope to URL. As the files are public access, we don't need token to access them.
                    String fileUrl = dataFile.getUri() + "?scope=" + tenantResolver.getTenant();
                    // Manage reference
                    if (dataFile.isReference()) {
                        ciBuilder.setDataObjectReference(dataFile.getDataType(), dataFile.getFilename(),
                                                         new URL(fileUrl));
                    } else {
                        ciBuilder.setDataObject(dataFile.getDataType(), dataFile.getFilename(),
                                                dataFile.getDigestAlgorithm(), dataFile.getChecksum(),
                                                dataFile.getFilesize(), new URL(fileUrl));
                    }
                    ciBuilder.setSyntax(dataFile.getMimeType());
                    builder.addContentInformation();
                }
            } catch (final MalformedURLException e) {
                LOGGER.error("Error building data object for entity {}", entity.getIpId());
                throw new ModuleException(e);
            }
        }

        return builder;
    }

    private <T extends AbstractEntity<?>> String getSession(T entity) {
        return "DAM " + entity.getType();
    }

    private void handleClientAIPResponse(HttpStatus status, AbstractEntity<?> entity, List<RejectedAip> rejectedAips) {
        LOGGER.info("status=" + status.toString());
        switch (status) {
            case OK:
            case CREATED:
                LOGGER.info("{} : update entity state to AIP_STORE_OK:", entity.getIpId());
                entity.setStateAip(EntityAipState.AIP_STORE_PENDING);
                break;
            case PARTIAL_CONTENT:
            case UNPROCESSABLE_ENTITY:
                // Some AIP are rejected
                if (rejectedAips != null) {
                    rejectedAips.stream().filter(r -> r.getAipId().equals(entity.getIpId().toString())).forEach(r -> {
                        LOGGER.error("{} : update entity state to AIP_STORE_ERROR for reason : {}", entity.getIpId(),
                                     r.getRejectionCauses().get(0));
                        entity.setStateAip(EntityAipState.AIP_STORE_ERROR);
                    });
                }
                break;
            default:
                break;
        }
    }

    private void handleClientAIPResponse(HttpStatus status, AbstractEntity<?> entity, AIP aip) {
        LOGGER.info("status=" + status.toString());
        switch (status) {
            case OK:
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

    private void handleClientAIPDeleteResponse(HttpStatus status, AbstractEntity<?> entity, String ipId) {
        LOGGER.info("status=" + status.toString());
        switch (status) {
            case NO_CONTENT:
                LOGGER.info("{} : AIP deletion is done", entity.getIpId());
                break;
            case CONFLICT:
                LOGGER.error("{} : AIP deletion failed", entity.getIpId());
                break;
            default:
                break;
        }
    }

}
