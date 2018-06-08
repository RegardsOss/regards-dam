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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

import com.google.common.base.Strings;
import com.google.gson.Gson;

import fr.cnes.regards.framework.feign.security.FeignSecurityManager;
import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.DataType;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.utils.file.ChecksumUtils;
import fr.cnes.regards.modules.entities.domain.AbstractDescEntity;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.Document;
import fr.cnes.regards.modules.entities.service.IStorageService;
import fr.cnes.regards.modules.project.client.rest.IProjectsClient;
import fr.cnes.regards.modules.storage.client.IAipClient;
import fr.cnes.regards.modules.storage.domain.AIP;
import fr.cnes.regards.modules.storage.domain.AIPBuilder;
import fr.cnes.regards.modules.storage.domain.AIPCollection;
import fr.cnes.regards.modules.storage.domain.DataFileDto;
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

    @Autowired
    private IAipClient aipClient;

    //    @Autowired
    //    private IProjectsClient projectsClient;
    //
    //    /**
    //     * {@link IRuntimeTenantResolver} instance
    //     */
    //    @Autowired
    //    private IRuntimeTenantResolver runtimeTenantResolver;
    //
    //    @Value("${zuul.prefix}")
    //    private String gatewayPrefix;
    //
    //    @Value("${spring.application.name}")
    //    private String microserviceName;

    @Autowired
    private Gson gson;

    @Override
    public <T extends AbstractEntity> T storeAIP(T entity) {

        AIPCollection collection = new AIPCollection();
        collection.add(getBuilder(entity).build());

        ResponseEntity<List<RejectedAip>> response = aipClient.store(collection);
        handleResponse(response.getStatusCode(), response.getBody(), entity);

        return entity;
    }

    @Override
    public <T extends AbstractEntity> T updateAIP(T entity) {

        ResponseEntity<AIP> response = aipClient.updateAip(entity.getIpId().toString(), getBuilder(entity).build());
        handleResponse(response.getStatusCode(), response.getBody(), entity);

        return entity;
    }

    /**
     * Build an {@link AIPBuilder} for an entity that can be
     * a {@link EntityType#COLLECTION}, {@link EntityType#DATASET} or {@link EntityType#DOCUMENT}.
     * 
     * @param entity the {@link AbstractEntity} for which to build an {@link AIPBuilder}
     * @return the created {@link AIPBuilder}
     */
    private <T extends AbstractEntity> AIPBuilder getBuilder(T entity) {
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
     */
    private void extractDocument(AIPBuilder builder, Document document) {
        document.getDocumentFiles().stream().forEach(df -> {
            try {
                builder.getContentInformationBuilder().setDataObject(DataType.DOCUMENT, df.getName(),
                                                                     df.getDigestAlgorithm(), df.getChecksum(),
                                                                     df.getSize(), df.getUri().toURL());
                builder.getContentInformationBuilder().setSyntax(df.getMimeType().toString(), "", df.getMimeType());
                builder.addContentInformation();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        });

    }

    private <T extends AbstractDescEntity> void extractDescription(AIPBuilder builder, T entity) {
        if (entity.getDescriptionFile() == null) {
            return;
        }

        if (entity.getDescriptionFile().getUrl() != null) {
            try {
                URL fUrl = new URL(entity.getDescriptionFile().getUrl());

                try (FileInputStream in = new FileInputStream(fUrl.getFile())) {
                    String fCheckSum = ChecksumUtils.computeHexChecksum(in, "MD5");
                    builder.getContentInformationBuilder().setDataObject(DataType.DOCUMENT, fUrl, "MD5", fCheckSum);
                    builder.getContentInformationBuilder()
                            .setSyntax(entity.getDescriptionFile().getType().getType().toString(), "",
                                       entity.getDescriptionFile().getType());
                    builder.addContentInformation();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else if (entity.getDescriptionFile().getContent() != null) {
            try (ByteArrayInputStream in = new ByteArrayInputStream(entity.getDescriptionFile().getContent())) {
                String fCheckSum = ChecksumUtils.computeHexChecksum(in, "MD5");
                builder.getContentInformationBuilder()
                        .setDataObject(DataType.DOCUMENT, new URL("https://to/be/calculated"), "MD5", fCheckSum);
                builder.getContentInformationBuilder()
                        .setSyntax(entity.getDescriptionFile().getType().getType().toString(), "",
                                   entity.getDescriptionFile().getType());
                builder.addContentInformation();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        } else {
            // error
        }
    }

    /**
     * Populates the {@link AIPBuilder} for a {@link Collection}
     * @param builder the current {@link AIPBuilder}
     * @param collection the current {@link Collection}
     */
    private void extractCollection(AIPBuilder builder, Collection collection) {
        extractDescription(builder, collection);
    }

    /**
     * Populates the {@link AIPBuilder} for a {@link Dataset}
     * @param builder the current {@link AIPBuilder}
     * @param document the current {@link Dataset}
     */
    private void extractDataset(AIPBuilder builder, Dataset dataSet) {
        builder.addContextInformation("score", dataSet.getScore());

        if (Strings.nullToEmpty(dataSet.getLicence()) != null) {
            builder.addDescriptiveInformation("licence", dataSet.getLicence());
        }
        if (dataSet.getQuotations() != null && dataSet.getQuotations().size() > 0) {
            builder.addDescriptiveInformation("quotations", gson.toJson(dataSet.getQuotations()));
        }
        extractDescription(builder, dataSet);
    }

    @Override
    public void deleteAIP(AbstractEntity pToDelete) {
    }

    private void handleResponse(HttpStatus status, List<RejectedAip> rejectedAips, AbstractEntity entity) {

        LOGGER.info("status=" + status.toString());

        System.out.println(status);
        //        switch (status) {
        //            case CREATED:
        //                // All AIP are valid
        //                for (AIPEntity aip : aips) {
        //                    aip.setState(AIPState.VALID);
        //                    aipService.save(aip);
        //                }
        //                break;
        //            case PARTIAL_CONTENT:
        //                // Some AIP are rejected
        //                Map<String, List<String>> rejectionCausesByIpId = new HashMap<>();
        //                if (rejectedAips != null) {
        //                    rejectedAips.forEach(aip -> rejectionCausesByIpId.put(aip.getIpId(), aip.getRejectionCauses()));
        //                }
        //                for (AIPEntity aip : aips) {
        ////                    if (rejectionCausesByIpId.containsKey(aip.getIpId())) {
        ////                        rejectAip(aip.getIpId(), rejectionCausesByIpId.get(aip.getIpId()));
        ////                    } else {
        ////                        aip.setState(AIPState.VALID);
        ////                        aipService.save(aip);
        ////                    }
        //                }
        //                break;
        //            case UNPROCESSABLE_ENTITY:
        //                // All AIP rejected
        //                if (rejectedAips != null) {
        ////                    for (RejectedAip aip : rejectedAips) {
        ////                        rejectAip(aip.getIpId(), aip.getRejectionCauses());
        ////                    }
        //                }
        //                break;
        //            default:
        ////                String message = String.format("AIP submission failure for ingest chain \"%s\"", ingestProcessingChain);
        ////                logger.error(message);
        ////                throw new JobRuntimeException(message);
        //        }
    }

    private void handleResponse(HttpStatus status, AIP aip, AbstractEntity entity) {

        LOGGER.info("status=" + status.toString());

        System.out.println(status);
        //        switch (status) {
        //            case CREATED:
        //                // All AIP are valid
        //                for (AIPEntity aip : aips) {
        //                    aip.setState(AIPState.VALID);
        //                    aipService.save(aip);
        //                }
        //                break;
        //            case PARTIAL_CONTENT:
        //                // Some AIP are rejected
        //                Map<String, List<String>> rejectionCausesByIpId = new HashMap<>();
        //                if (rejectedAips != null) {
        //                    rejectedAips.forEach(aip -> rejectionCausesByIpId.put(aip.getIpId(), aip.getRejectionCauses()));
        //                }
        //                for (AIPEntity aip : aips) {
        ////                    if (rejectionCausesByIpId.containsKey(aip.getIpId())) {
        ////                        rejectAip(aip.getIpId(), rejectionCausesByIpId.get(aip.getIpId()));
        ////                    } else {
        ////                        aip.setState(AIPState.VALID);
        ////                        aipService.save(aip);
        ////                    }
        //                }
        //                break;
        //            case UNPROCESSABLE_ENTITY:
        //                // All AIP rejected
        //                if (rejectedAips != null) {
        ////                    for (RejectedAip aip : rejectedAips) {
        ////                        rejectAip(aip.getIpId(), aip.getRejectionCauses());
        ////                    }
        //                }
        //                break;
        //            default:
        ////                String message = String.format("AIP submission failure for ingest chain \"%s\"", ingestProcessingChain);
        ////                logger.error(message);
        ////                throw new JobRuntimeException(message);
        //        }
    }

    //    /**
    //     * Handles any changes that should occurred between private rs-storage information and how the rest of the world
    //     * should see them.
    //     * For example, change URL from file:// to http://[project_host]/[gateway prefix]/rs-storage/...
    //     */
    //    private void toPublicDataFile(DataFileDto dataFile, AIP owningAip) throws MalformedURLException {
    //        // Lets reconstruct the public url of rs-storage
    //        // First lets get the public hostname from rs-admin-instance
    //        FeignSecurityManager.asSystem();
    //        String projectHost = projectsClient.retrieveProject(runtimeTenantResolver.getTenant()).getBody().getContent()
    //                .getHost();
    //        FeignSecurityManager.reset();
    //        // now lets add it the gateway prefix and the microservice name and the endpoint path to it
    //        StringBuilder sb = new StringBuilder();
    //        sb.append(projectHost);
    //        sb.append("/");
    //        sb.append(gatewayPrefix);
    //        sb.append("/");
    //        sb.append(microserviceName);
    //        sb.append(AIP_PATH);
    //        sb.append(DOWNLOAD_AIP_FILE.replaceAll("\\{ip_id\\}", owningAip.getId().toString())
    //                .replaceAll("\\{checksum\\}", dataFile.getChecksum()));
    //        URL downloadUrl = new URL(sb.toString());
    //        dataFile.setUrl(downloadUrl);
    //    }

}
