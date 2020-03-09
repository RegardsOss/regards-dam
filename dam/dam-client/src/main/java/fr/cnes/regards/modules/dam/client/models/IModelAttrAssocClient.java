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
package fr.cnes.regards.modules.dam.client.models;

import java.util.Collection;
import java.util.List;

import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import fr.cnes.regards.framework.feign.annotation.RestClient;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.modules.dam.domain.models.ModelAttrAssoc;

/**
 * @author Sylvain Vissiere-Guerinet
 */
@RestClient(name = "rs-dam", contextId = "rs-dam.model-att-assoc.client")
@RequestMapping(IModelAttrAssocClient.BASE_MAPPING)
public interface IModelAttrAssocClient {

    /**
     * Client base path
     */
    String BASE_MAPPING = "/models";

    /**
     * Client type path
     */
    String TYPE_MAPPING = "/{modelName}/attributes";

    /**
     * Client association path
     */
    String ASSOCS_MAPPING = "/assocs";

    @RequestMapping(path = TYPE_MAPPING, method = RequestMethod.GET)
    ResponseEntity<List<Resource<ModelAttrAssoc>>> getModelAttrAssocs(@PathVariable("modelName") String modelName);

    @RequestMapping(path = "{datasetUrn}" + ASSOCS_MAPPING, method = RequestMethod.GET)
    ResponseEntity<Collection<ModelAttrAssoc>> getModelAttrAssocsForDataInDataset(
            @RequestParam(name = "datasetUrn") UniformResourceName datasetUrn);

    /**
     * Retrieve model attribute associations for a given entity type (optional)
     * @return the model attribute associations
     */
    @RequestMapping(path = ASSOCS_MAPPING, method = RequestMethod.GET)
    ResponseEntity<Collection<ModelAttrAssoc>> getModelAttrAssocsFor(
            @RequestParam(name = "type", required = false) EntityType type);

}
