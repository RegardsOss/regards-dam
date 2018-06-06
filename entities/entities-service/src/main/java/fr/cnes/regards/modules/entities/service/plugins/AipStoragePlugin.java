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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.google.common.base.Strings;
import com.google.gson.Gson;

import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.service.IStorageService;
import fr.cnes.regards.modules.storage.client.IAipClient;
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

    @Autowired
    private IAipClient aipClient;

    @Autowired
    private Gson gson;

    @Override
    public <T extends AbstractEntity> T storeAIP(T pToPersist) {
        AIPBuilder builder = new AIPBuilder(pToPersist.getIpId(), pToPersist.getSipId(), EntityType.DATASET);

        extractEntity(builder, pToPersist);

        if (pToPersist instanceof Dataset) {
            extractDataset(builder, (Dataset) pToPersist);
        }

        AIPCollection collection = new AIPCollection();
        collection.add(builder.build());

        ResponseEntity<List<RejectedAip>> response = aipClient.store(collection);
        handleResponse(response.getStatusCode(), response.getBody(), pToPersist);

        return pToPersist;
    }

    private void extractEntity(AIPBuilder builder, AbstractEntity entity) {
        builder.addTags(entity.getTags().toArray(new String[entity.getTags().size()]));
        builder.addEvent("AIP creation", entity.getCreationDate());
        entity.getProperties().stream().forEach(attr->{
            builder.addDescriptiveInformation(attr.getName(), gson.toJson(attr.getValue()));            
        });
    }

    private void extractDataset(AIPBuilder builder, Dataset dataSet) {
        builder.addContextInformation("score", dataSet.getScore());

        if (Strings.nullToEmpty(dataSet.getLicence()) != null) {
            builder.addDescriptiveInformation("licence", dataSet.getLicence());
        }
        if (dataSet.getQuotations() != null && dataSet.getQuotations().size() > 0) {
            builder.addDescriptiveInformation("quotations", gson.toJson(dataSet.getQuotations()));
        }

    }

    @Override
    public void deleteAIP(AbstractEntity pToDelete) {
    }

    @Override
    public <T extends AbstractEntity> T updateAIP(T pToUpdate) {
        // TODO Auto-generated method stub
        return pToUpdate;
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

}
