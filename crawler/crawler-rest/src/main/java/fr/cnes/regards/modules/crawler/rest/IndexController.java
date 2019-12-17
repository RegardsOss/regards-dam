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
package fr.cnes.regards.modules.crawler.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.crawler.domain.DatasourceIngestion;
import fr.cnes.regards.modules.crawler.service.IDatasourceIngesterService;
import fr.cnes.regards.modules.crawler.service.IEntityIndexerService;

@RestController
@RequestMapping(IndexController.TYPE_MAPPING)
public class IndexController {

    public static final String TYPE_MAPPING = "/index";

    @Autowired
    protected IEntityIndexerService entityIndexerService;

    @Autowired
    private IDatasourceIngesterService dataSourceIngesterService;

    /**
     * Current tenant resolver
     */
    @Autowired
    protected IRuntimeTenantResolver runtimeTenantResolver;

    /**
     * Delete a DatasourceIngestion.
     * @return void
     * @throws ModuleException
     */
    @ResourceAccess(description = "Delete and recreate curent index.")
    @RequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity<Void> recreateIndex() throws ModuleException {

        String tenant = runtimeTenantResolver.getTenant();

        entityIndexerService.deleteIndexNRecreateEntities(tenant);

        // Clear all datasources ingestion
        List<DatasourceIngestion> datasources = dataSourceIngesterService.getDatasourceIngestions();
        if ((datasources != null) && !datasources.isEmpty()) {
            datasources.forEach(ds -> dataSourceIngesterService.deleteDatasourceIngestion(ds.getId()));
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
