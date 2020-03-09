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
package fr.cnes.regards.modules.opensearch.rest;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.framework.security.role.DefaultRole;
import fr.cnes.regards.modules.opensearch.service.OpenSearchService;
import fr.cnes.regards.modules.search.schema.OpenSearchDescription;

/**
 * @author sbinda
 *
 */
@RestController
@RequestMapping(OpensearchController.TYPE_MAPPING)
public class OpensearchController {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OpensearchController.class);

    /**
     * Type mapping
     */
    public static final String TYPE_MAPPING = "/opensearch";

    @Autowired
    private OpenSearchService service;

    @ResourceAccess(description = "Import a fragment", role = DefaultRole.PUBLIC)
    @RequestMapping(method = RequestMethod.GET, value = "/descriptor")
    public ResponseEntity<OpenSearchDescription> retrieveDescriptor(@RequestParam("url") String url) {
        try {
            return ResponseEntity.ok(service.readDescriptor(new URL(url)));
        } catch (MalformedURLException | ModuleException e) {
            LOGGER.error(e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

}
