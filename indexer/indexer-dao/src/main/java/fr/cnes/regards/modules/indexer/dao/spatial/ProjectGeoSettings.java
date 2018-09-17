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
package fr.cnes.regards.modules.indexer.dao.spatial;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import fr.cnes.regards.framework.feign.security.FeignSecurityManager;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.utils.RsRuntimeException;
import fr.cnes.regards.modules.indexer.domain.spatial.Crs;
import fr.cnes.regards.modules.project.client.rest.IProjectsClient;
import fr.cnes.regards.modules.project.domain.Project;

/**
 * Current project settings concerning geospatial behavior
 * @author oroussel
 */
@Component
public class ProjectGeoSettings {

    @Autowired
    private IProjectsClient projectsClient;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    private LoadingCache<String, Pair<Boolean, Crs>> settingsCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES).build(new CacheLoader<String, Pair<Boolean, Crs>>() {

                @Override
                public Pair<Boolean, Crs> load(String key) throws Exception {
                    try {
                        FeignSecurityManager.asSystem();

                        ResponseEntity<Resource<Project>> response = projectsClient
                                .retrieveProject(tenantResolver.getTenant());
                        if (response.getStatusCode() == HttpStatus.OK) {
                            Project currentProject = response.getBody().getContent();
                            return Pair.of(currentProject.getPoleToBeManaged(), Crs.valueOf(currentProject.getCrs()));
                        } else { // Must throw something
                            throw new RsRuntimeException(new Exception(
                                    String.format("Error while asking project client: Error %d",
                                                  response.getStatusCode())));
                        }
                    } finally {
                        FeignSecurityManager.reset();
                    }
                }
            });

    private Boolean shouldManagePolesOnGeometries = null;

    private Crs crs = null;

    public Boolean getShouldManagePolesOnGeometries() {
        return settingsCache.getUnchecked(tenantResolver.getTenant()).getLeft();
    }

    public Crs getCrs() {
        return settingsCache.getUnchecked(tenantResolver.getTenant()).getRight();
    }
}
