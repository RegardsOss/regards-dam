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
package fr.cnes.regards.modules.dam.service.entities.plugins;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.modules.dam.dao.entities.IAbstractEntityRequestRepository;
import fr.cnes.regards.modules.dam.domain.entities.AbstractEntity;
import fr.cnes.regards.modules.dam.domain.entities.AbstractEntityRequest;
import fr.cnes.regards.modules.dam.service.entities.AbstractEntityService;
import fr.cnes.regards.modules.dam.service.entities.IStorageService;
import fr.cnes.regards.modules.indexer.domain.DataFile;
import fr.cnes.regards.modules.storage.client.IStorageClient;
import fr.cnes.regards.modules.storage.domain.dto.request.FileDeletionRequestDTO;
import fr.cnes.regards.modules.storage.domain.dto.request.FileStorageRequestDTO;

/**
 * @author Sylvain Vissiere-Guerinet
 * @author Christophe Mertz
 */
@Plugin(description = "This plugin allows to POST AIP entities to storage unit", id = "StoragePlugin",
        version = "1.0.0", author = "REGARDS Team", contact = "regards@c-s.fr", license = "GPLv3", owner = "CSSI",
        url = "https://github.com/RegardsOss")
public class StoragePlugin implements IStorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoragePlugin.class);

    private final String URI_TEMPLATE = "%s?scope=%s";

    public static final String ID_PATH_PARAM = "urn";

    public static final String DOWNLOAD_PATH = "/{" + ID_PATH_PARAM + "}/download";

    @Value("${plugin.storage.name:#{null}}")
    private String storage;

    @Value("${plugin.storage.directory.name:#{null}}")
    private String storageSubDirectory;

    @Value("${spring.application.name}")
    private String applicationName;

    @Autowired
    private IStorageClient storageClient;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    @Autowired
    private IAbstractEntityRequestRepository entityRequestRepo;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Override
    public <T extends AbstractEntity<?>> T store(T toPersist) {
        if (storage != null) {
            Collection<FileStorageRequestDTO> files = toPersist.getFiles().values().stream()
                    .map(entry -> initStorageRequest(entry, toPersist.getIpId())).collect(Collectors.toSet());
            if (!files.isEmpty()) {
                Set<AbstractEntityRequest> infos = this.storageClient.store(files).stream()
                        .map(request -> new AbstractEntityRequest(request.getGroupId(), toPersist.getIpId()))
                        .collect(Collectors.toSet());
                this.entityRequestRepo.saveAll(infos);
            }
        }
        return toPersist;
    }

    @Override
    public <T extends AbstractEntity<?>> T update(T toUpdate, T oldEntity) {
        if (storage != null) {

            // manage added files in toUpdate and not in oldEntity
            Collection<FileStorageRequestDTO> filesToAdd = toUpdate.getFiles().values().stream()
                    .filter(file -> !oldEntity.getFiles().values().stream()
                            .anyMatch(f -> f.getChecksum().equals(file.getChecksum())))
                    .map(entry -> initStorageRequest(entry, toUpdate.getIpId())).collect(Collectors.toSet());
            if (!filesToAdd.isEmpty()) {
                Set<AbstractEntityRequest> infos = this.storageClient.store(filesToAdd).stream()
                        .map(request -> new AbstractEntityRequest(request.getGroupId(), toUpdate.getIpId()))
                        .collect(Collectors.toSet());
                this.entityRequestRepo.saveAll(infos);
            }
            // manage deleted file in toUpdate and present in oldEntity
            Collection<FileDeletionRequestDTO> filesToDelete = oldEntity.getFiles().values().stream()
                    .filter(file -> !toUpdate.getFiles().values().stream()
                            .anyMatch(f -> f.getChecksum().equals(file.getChecksum())))
                    .map(entry -> initDeletionRequest(entry, toUpdate.getIpId().toString()))
                    .collect(Collectors.toSet());
            if (!filesToDelete.isEmpty()) {
                this.storageClient.delete(filesToDelete);
            }
        }

        return toUpdate;
    }

    @Override
    public void delete(AbstractEntity<?> toDelete) {
        if (storage != null) {

            Collection<FileDeletionRequestDTO> files = toDelete.getFiles().values().stream()
                    .map(entry -> initDeletionRequest(entry, toDelete.getIpId().toString()))
                    .collect(Collectors.toList());
            this.storageClient.delete(files);
        }
    }

    private FileStorageRequestDTO initStorageRequest(DataFile file, UniformResourceName urn) {

        try {
            return FileStorageRequestDTO.build(file.getFilename(), file.getChecksum(), file.getDigestAlgorithm(),
                                               file.getMimeType().toString(), urn.toString(),
                                               String.format(URI_TEMPLATE, generateDownloadUrl(urn).toString(),
                                                             this.tenantResolver.getTenant().toString()),
                                               this.storage, Optional.of(this.storageSubDirectory));
        } catch (ModuleException e) {
            LOGGER.error("Error while generating URI", e);
        }
        return null;
    }

    private FileDeletionRequestDTO initDeletionRequest(DataFile file, String urn) {
        return FileDeletionRequestDTO.build(file.getFilename(), storage, urn, false);
    }

    /**
     * Generate a public download URL for the file associated to the given Checksum
     * @param urn
     * @return a public URL to retrieve the AIP manifest
     * @throws ModuleException if the Eureka server is not reachable
     */
    public URL generateDownloadUrl(UniformResourceName urn) throws ModuleException {
        Optional<ServiceInstance> instance = discoveryClient.getInstances(this.applicationName).stream().findFirst();
        if (!instance.isPresent()) {
            throw new ModuleException("Unable to retrieve an accessible instance for dam microservice");
        }

        String host = instance.get().getHost();
        String path = Paths.get(AbstractEntityService.DATA_TYPE_CONTROLLER_ROOT_PATH, DOWNLOAD_PATH).toString();
        String p = path.toString().replace("{" + ID_PATH_PARAM + "}", urn.toString());
        p = (p.charAt(0) == '/') ? p.replaceFirst("/", "") : p;
        String urlStr = String.format("%s/%s?scope=%s", host, p, tenantResolver.getTenant());
        try {
            return new URL(urlStr);
        } catch (MalformedURLException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ModuleException(
                    String.format("Error generating AIP download url. Invalid calculated url %s. Cause : %s", urlStr,
                                  e.getMessage()),
                    e);
        }
    }
}
