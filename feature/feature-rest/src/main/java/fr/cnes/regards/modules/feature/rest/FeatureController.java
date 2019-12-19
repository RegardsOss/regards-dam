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
package fr.cnes.regards.modules.feature.rest;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.framework.geojson.GeoJsonMediaType;
import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.feature.domain.request.FeatureCreationRequest;
import fr.cnes.regards.modules.feature.domain.request.FeatureUpdateRequest;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureCreationCollection;
import fr.cnes.regards.modules.feature.dto.FeatureUpdateCollection;
import fr.cnes.regards.modules.feature.dto.RequestInfo;
import fr.cnes.regards.modules.feature.dto.urn.FeatureUniformResourceName;
import fr.cnes.regards.modules.feature.service.IFeatureCreationService;
import fr.cnes.regards.modules.feature.service.IFeatureUpdateService;

/**
 * Controller REST handling {@link Feature} collections.
 *
 * @author Kevin Marchois
 */
@RestController
@RequestMapping(FeatureController.PATH_FEATURES)
public class FeatureController implements IResourceController<RequestInfo<?>> {

    public final static String PATH_FEATURES = "/features";

    @Autowired
    private IFeatureCreationService featureCreationService;

    @Autowired
    private IFeatureUpdateService featureUpdateService;

    @Autowired
    private IResourceService resourceService;

    /**
     * Create a list of {@link FeatureCreationRequest} from a list of {@link Feature} stored in a {@link FeatureCreationCollection}
     * and return a {@link RequestInfo} full of request ids and occured errors
     * @param collection {@link FeatureUpdateCollection} it contain all {@link Feature} to handle
     * @return {@link RequestInfo}
     */
    @RequestMapping(method = RequestMethod.POST, consumes = GeoJsonMediaType.APPLICATION_GEOJSON_VALUE)
    @ResourceAccess(description = "Public a feature and return the request id")
    public ResponseEntity<EntityModel<RequestInfo<?>>> createFeatures(
            @Valid @RequestBody FeatureCreationCollection collection) {

        RequestInfo<String> info = this.featureCreationService.registerRequests(collection);
        return new ResponseEntity<>(toResource(info), computeStatus(info));
    }

    /**
     * Create a list of {@link FeatureUpdateRequest} from a list of {@link Feature} stored in a {@link FeatureUpdateCollection}
     * and return a {@link RequestInfo} full of urns and occured errors
     * @param collection {@link FeatureUpdateCollection} it contain all {@link Feature} to handle
     * @return {@link RequestInfo}
     */
    @RequestMapping(method = RequestMethod.PATCH, consumes = GeoJsonMediaType.APPLICATION_GEOJSON_VALUE)
    @ResourceAccess(description = "Public a feature and return the request id")
    public ResponseEntity<EntityModel<RequestInfo<?>>> updateFeatures(
            @Valid @RequestBody FeatureUpdateCollection collection) {

        RequestInfo<FeatureUniformResourceName> info = this.featureUpdateService.registerRequests(collection);
        return new ResponseEntity<>(toResource(info), computeStatus(info));
    }

    /**
     * Compute {@link HttpStatus} according to information return by the service
     */
    private HttpStatus computeStatus(RequestInfo<?> info) {
        Boolean hasGranted = !info.getGranted().isEmpty();
        Boolean hasDenied = !info.getDenied().isEmpty();

        HttpStatus status;
        if (hasGranted && hasDenied) {
            status = HttpStatus.PARTIAL_CONTENT; // 206
        } else if (hasDenied) {
            status = HttpStatus.UNPROCESSABLE_ENTITY; // 422
        } else {
            status = HttpStatus.CREATED; // 201
        }
        return status;
    }

    @Override
    public EntityModel<RequestInfo<?>> toResource(RequestInfo<?> element, Object... extras) {
        return resourceService.toResource(element);
    }
}
