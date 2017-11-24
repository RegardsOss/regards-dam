/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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

import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.module.annotation.ModuleInfo;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.crawler.domain.DatasourceIngestion;
import fr.cnes.regards.modules.crawler.domain.dto.DatasourceIngestionDTO;
import fr.cnes.regards.modules.crawler.domain.dto.DatasourceIngestionDTOBuilder;
import fr.cnes.regards.modules.crawler.service.ICrawlerAndIngesterService;

/**
 * Crawler rest controller
 * @author Sébastien Binda
 */
@RestController
//CHECKSTYLE:OFF
@ModuleInfo(name = "crawler", version = "2.0-SNAPSHOT", author = "REGARDS", legalOwner = "CS SI",
        documentation = "http://test")
//CHECKSTYLE:ON
@RequestMapping(CrawlerController.TYPE_MAPPING)
public class CrawlerController implements IResourceController<DatasourceIngestionDTO> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrawlerController.class);

    public static final String TYPE_MAPPING = "/crawler";

    /**
     * Crawler service
     */
    @Autowired
    private ICrawlerAndIngesterService crawlerService;

    @Autowired
    private IPluginService pluginService;

    /**
     * HATEOAS service
     */
    @Autowired
    private IResourceService resourceService;

    /**
     * Retrieve all {@link DatasourceIngestion}.
     * @return a list of {@link DatasourceIngestion}
     */
    @ResourceAccess(description = "List all crawler datasources.")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<Resource<DatasourceIngestionDTO>>> getAllDatasourceIngestion() {
        List<DatasourceIngestion> list = crawlerService.getDatasourceIngestions();
        List<DatasourceIngestionDTO> dtos = Lists.newArrayList();
        for (DatasourceIngestion ds : list) {
            try {
                PluginConfiguration conf = pluginService.getPluginConfiguration(ds.getId());
                dtos.add(DatasourceIngestionDTOBuilder.build(ds, conf.getLabel()));
            } catch (ModuleException e) {
                LOGGER.warn("Plugin configuration associated to datasourceIngestion {} does not exists anymore",
                            ds.getId(), e);
                dtos.add(DatasourceIngestionDTOBuilder.build(ds, null));
            }
        }
        return ResponseEntity.ok(toResources(dtos));
    }

    @Override
    public Resource<DatasourceIngestionDTO> toResource(DatasourceIngestionDTO pElement, Object... pExtras) {
        return resourceService.toResource(pElement);
    }

}
