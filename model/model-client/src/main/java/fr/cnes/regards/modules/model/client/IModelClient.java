/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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

import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import fr.cnes.regards.framework.feign.annotation.RestClient;
import fr.cnes.regards.framework.urn.EntityType;
import fr.cnes.regards.modules.model.domain.Model;

/**
 * @author Sylvain Vissiere-Guerinet
 */
@RestClient(name = "rs-dam", contextId = "rs-dam.model.client")
@RequestMapping(IModelClient.TYPE_MAPPING)
public interface IModelClient {

    /**
     * Client base path
     */
    String TYPE_MAPPING = "/models";

    /**
     * Retrieve the models of a given type (optional)
     * @return the models
     */
    @RequestMapping(method = RequestMethod.GET)
    ResponseEntity<List<EntityModel<Model>>> getModels(
            @RequestParam(value = "type", required = false) EntityType pType);
}
