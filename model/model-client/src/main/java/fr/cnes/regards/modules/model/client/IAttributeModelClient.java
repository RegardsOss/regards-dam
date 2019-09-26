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
package fr.cnes.regards.modules.model.client;

import java.util.List;

import org.springframework.hateoas.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import fr.cnes.regards.framework.feign.annotation.RestClient;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.model.dto.properties.PropertyType;

/**
 * Feign client handling {@link AttributeModel}s
 * @author Xavier-Alexandre Brochard
 */
@RestClient(name = "rs-dam", contextId = "rs-dam.attribute-model.client")
@RequestMapping(value = IAttributeModelClient.PATH, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public interface IAttributeModelClient {

    /**
     * Mapping
     */
    String PATH = "/models/attributes";

    /**
     * Request parameter : attribute type
     */
    String PARAM_TYPE = "type";

    /**
     * Request parameter : fragment name
     */
    String PARAM_FRAGMENT_NAME = "fragmentName";

    /**
     * Get the list of {@link AttributeModel}
     * @param type the type to filter on
     * @param fragmentName the fragment to filter on
     * @return the list wrapped in an HTTP response
     */
    @RequestMapping(method = RequestMethod.GET)
    ResponseEntity<List<Resource<AttributeModel>>> getAttributes(
            @RequestParam(value = PARAM_TYPE, required = false) PropertyType type,
            @RequestParam(value = PARAM_FRAGMENT_NAME, required = false) String fragmentName);

}
