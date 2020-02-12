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
package fr.cnes.regards.modules.dam.service.dataaccess;

import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.feign.security.FeignSecurityManager;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.notification.NotificationLevel;
import fr.cnes.regards.framework.notification.client.INotificationClient;
import fr.cnes.regards.framework.security.role.DefaultRole;
import fr.cnes.regards.framework.urn.UniformResourceName;
import fr.cnes.regards.framework.utils.plugins.exception.NotAvailablePluginConfigurationException;
import fr.cnes.regards.framwork.logbackappender.LogConstants;
import fr.cnes.regards.modules.dam.dao.dataaccess.IAccessRightRepository;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessgroup.User;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.AccessLevel;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.AccessRight;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.DataAccessLevel;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.event.AccessRightEvent;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.event.AccessRightEventType;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.plugins.IDataObjectAccessFilterPlugin;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.dam.domain.entities.metadata.DatasetMetadata;
import fr.cnes.regards.modules.dam.service.entities.IDatasetService;

/**
 * Access right service implementation
 *
 * @author Sylvain Vissiere-Guerinet
 */
@Service
@MultitenantTransactional
public class AccessRightService implements IAccessRightService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessRightService.class);

    @Autowired
    private IAccessRightRepository repository;

    @Autowired
    private IAccessGroupService accessGroupService;

    @Autowired
    private IDatasetService datasetService;

    @Autowired
    private IPublisher eventPublisher;

    @Autowired
    private IPluginService pluginService;

    @Autowired
    private INotificationClient notificationClient;

    @Override
    public Page<AccessRight> retrieveAccessRights(String accessGroupName, UniformResourceName datasetIpId,
            Pageable pageable) throws ModuleException {
        if (accessGroupName != null) {
            return retrieveAccessRightsByAccessGroup(datasetIpId, accessGroupName, pageable);
        }
        return retrieveAccessRightsByDataset(datasetIpId, pageable);
    }

    private Page<AccessRight> retrieveAccessRightsByDataset(UniformResourceName datasetIpId, Pageable pageable)
            throws ModuleException {
        if (datasetIpId != null) {
            Dataset ds = datasetService.load(datasetIpId);
            if (ds == null) {
                throw new EntityNotFoundException(datasetIpId.toString(), Dataset.class);
            }

            return repository.findAllByDataset(ds, pageable);
        }
        return repository.findAll(pageable);
    }

    @Override
    public Optional<AccessRight> retrieveAccessRight(String accessGroupName, UniformResourceName datasetIpId)
            throws ModuleException {
        Preconditions.checkNotNull(accessGroupName);
        Preconditions.checkNotNull(datasetIpId);
        AccessGroup ag = accessGroupService.retrieveAccessGroup(accessGroupName);
        Dataset dataset = datasetService.load(datasetIpId);

        return repository.findAccessRightByAccessGroupAndDataset(ag, dataset);
    }

    @Override
    public boolean hasAccessRights(AccessGroup accessGroup) {
        Assert.notNull(accessGroup, "Access group is required");
        Page<AccessRight> accessRights = repository.findAllByAccessGroup(accessGroup, PageRequest.of(0, 1));
        return accessRights.getTotalElements() > 0;
    }

    @Override
    public DatasetMetadata retrieveDatasetMetadata(UniformResourceName datasetIpId) throws ModuleException {

        if (datasetIpId == null) {
            throw new IllegalArgumentException("datasetIpId must not be null");
        }
        DatasetMetadata metadata = new DatasetMetadata();

        retrieveAccessRightsByDataset(datasetIpId, PageRequest.of(0, Integer.MAX_VALUE)).getContent().stream()
                .forEach(accessRight -> {
                    Long metadataPluginId = null;
                    Long pluginId = null;
                    if (accessRight.getDataAccessPlugin() != null) {
                        metadataPluginId = accessRight.getDataAccessPlugin().getId();
                    }

                    boolean datasetAccess = accessRight.getAccessLevel() != AccessLevel.NO_ACCESS;
                    boolean dataAccess = datasetAccess
                            && (accessRight.getDataAccessLevel() != DataAccessLevel.NO_ACCESS);
                    metadata.addDataObjectGroup(accessRight.getAccessGroup().getName(), datasetAccess, dataAccess,
                                                metadataPluginId, pluginId);
                });
        return metadata;

    }

    private Page<AccessRight> retrieveAccessRightsByAccessGroup(UniformResourceName pDatasetIpId,
            String pAccessGroupName, Pageable pPageable) throws ModuleException {
        AccessGroup ag = accessGroupService.retrieveAccessGroup(pAccessGroupName);
        if (ag == null) {
            throw new EntityNotFoundException(pAccessGroupName, AccessGroup.class);
        }
        if (pDatasetIpId != null) {
            Dataset ds = datasetService.load(pDatasetIpId);
            if (ds == null) {
                throw new EntityNotFoundException(pDatasetIpId.toString(), Dataset.class);
            }

            return repository.findAllByAccessGroupAndDataset(ag, ds, pPageable);
        } else {
            return repository.findAllByAccessGroup(ag, pPageable);
        }
    }

    @Override
    public AccessRight createAccessRight(AccessRight accessRight) throws ModuleException {
        Dataset dataset = datasetService.load(accessRight.getDataset().getId());
        if (dataset == null) {
            throw new EntityNotFoundException(accessRight.getDataset().getId(), Dataset.class);
        }

        AccessGroup accessGroup = accessRight.getAccessGroup();
        if (!accessGroupService.existGroup(accessGroup.getId())) {
            throw new EntityNotFoundException(accessGroup.getId(), AccessGroup.class);
        }
        // Adding group to Dataset
        dataset.getGroups().add(accessGroup.getName());
        // Save dataset through DatasetService (no cascade on AbstractAccessRight.dataset)
        datasetService.update(dataset);
        // re-set updated dataset on accessRight to make its state correct on accessRight object
        accessRight.setDataset(dataset);

        // If a new plugin conf is provided, create it
        if (accessRight.getDataAccessPlugin() != null) {
            createPluginConfiguration(accessRight.getDataAccessPlugin());
        }

        AccessRight created = repository.save(accessRight);
        logForSecurity(created);
        eventPublisher.publish(new AccessRightEvent(dataset.getIpId(), AccessRightEventType.CREATE));
        return created;
    }

    /**
     * Handle security log
     */
    private void logForSecurity(AccessRight accessRight) {
        // Access rights have diferent method of being configured, each one should has its own log format to be understandable.
        switch (accessRight.getAccessLevel()) {
            case FULL_ACCESS:
                LOGGER.info("{}Dataset {} access right has been modified."
                        + " Users from group {} has access to this dataset metadata and its data metadata."
                        + " Access to physical data is: {}", LogConstants.SECURITY_MARKER,
                            accessRight.getConstrained().getLabel(), accessRight.getAccessGroup().getName(),
                            accessRight.getAccessLevel(), accessRight.getDataAccessLevel());
                break;
            case RESTRICTED_ACCESS:
                LOGGER.info("{}Dataset {} access right has been modified."
                        + " Users from group {} has access to this dataset."
                        + " This means they can only see its metadata and no information about its data.",
                            LogConstants.SECURITY_MARKER, accessRight.getConstrained().getLabel(),
                            accessRight.getAccessGroup().getName(), accessRight.getAccessLevel());
                break;
            case CUSTOM_ACCESS:
                LOGGER.info("{}Dataset {} access right has been modified."
                        + " Users from group {} has access to this dataset metadata"
                        + " and its data access is decided by the plugin {}.", LogConstants.SECURITY_MARKER,
                            accessRight.getConstrained().getLabel(), accessRight.getAccessGroup().getName(),
                            accessRight.getAccessLevel(), accessRight.getDataAccessPlugin().getLabel());
                break;
            case NO_ACCESS:
                LOGGER.info("{}Dataset {} access right has been modified."
                        + " Users from group {} has no access to this dataset metadata and its data.",
                            LogConstants.SECURITY_MARKER, accessRight.getConstrained().getLabel(),
                            accessRight.getAccessGroup().getName());
                break;
            default:
                LOGGER.error("{}Dataset {} access right has been modified with an undocumented access level {}.",
                             LogConstants.SECURITY_MARKER, accessRight.getConstrained().getLabel(),
                             accessRight.getAccessLevel());
                break;
        }
    }

    @Override
    public AccessRight retrieveAccessRight(Long id) throws EntityNotFoundException {
        Optional<AccessRight> resultOpt = repository.findById(id);
        if (!resultOpt.isPresent()) {
            throw new EntityNotFoundException(id, AccessRight.class);
        }
        return resultOpt.get();
    }

    /**
     * Update access right is only used to change access levels.
     */
    @Override
    public AccessRight updateAccessRight(Long id, AccessRight accessRight) throws ModuleException {
        Optional<AccessRight> accessRightFromDbOpt = repository.findById(id);
        if (!accessRightFromDbOpt.isPresent()) {
            throw new EntityNotFoundException(id, AccessRight.class);
        }
        if (!accessRight.getId().equals(id)) {
            throw new EntityInconsistentIdentifierException(id, accessRight.getId(), AccessRight.class);
        }
        AccessRight accessRightFromDb = accessRightFromDbOpt.get();
        Optional<PluginConfiguration> toRemove = updatePluginConfiguration(Optional.ofNullable(accessRight
                .getDataAccessPlugin()), Optional.ofNullable(accessRightFromDb.getDataAccessPlugin()));

        repository.save(accessRight);
        // Load access right with dependencies
        accessRight = repository.findById(accessRight.getId()).get();
        logForSecurity(accessRight);
        // Remove unused plugin conf id any
        if (toRemove.isPresent()) {
            pluginService.deletePluginConfiguration(toRemove.get().getBusinessId());
        }

        eventPublisher.publish(new AccessRightEvent(accessRight.getDataset().getIpId(), AccessRightEventType.UPDATE));
        return accessRight;
    }

    @Override
    public void deleteAccessRight(Long id) throws ModuleException {
        Optional<AccessRight> accessRightOpt = repository.findById(id);
        if (!accessRightOpt.isPresent()) {
            throw new EntityNotFoundException(id, AccessRight.class);
        }
        AccessRight accessRight = accessRightOpt.get();
        // Remove current group from dataset if accessRight is a GroupAccessRight
        Dataset dataset = datasetService.load(accessRight.getDataset().getId());
        if (dataset != null) {
            dataset.getGroups().remove(accessRight.getAccessGroup().getName());
            datasetService.update(dataset);
        }

        PluginConfiguration confToDelete = accessRight.getDataAccessPlugin();
        repository.deleteById(id);
        LOGGER.info("{}Dataset {} has no more configured access for users from group {}", LogConstants.SECURITY_MARKER,
                    accessRight.getConstrained().getLabel(), accessRight.getAccessGroup().getName());
        if ((confToDelete != null) && (confToDelete.getId() != null)) {
            pluginService.deletePluginConfiguration(confToDelete.getBusinessId());
        }

        if (dataset != null) {
            eventPublisher.publish(new AccessRightEvent(dataset.getIpId(), AccessRightEventType.DELETE));
        }
    }

    @Override
    public boolean isUserAutorisedToAccessDataset(UniformResourceName datasetIpId, String userEMail)
            throws ModuleException {
        User user = new User(userEMail);

        Dataset ds = datasetService.load(datasetIpId);
        if (ds == null) {
            throw new EntityNotFoundException(datasetIpId.toString(), Dataset.class);
        }
        Set<AccessGroup> accessGroups = accessGroupService.retrieveAllUserAccessGroupsOrPublicAccessGroups(userEMail);
        boolean isAutorised = false;
        for (AccessGroup accessGroup : accessGroups) {
            // Check if the user is concerned by the accessGroup
            if (accessGroup.isPublic() || accessGroup.getUsers().contains(user)) {
                Optional<AccessRight> accessRightOptional = repository
                        .findAccessRightByAccessGroupAndDataset(accessGroup, ds);
                // Check if the accessRight allows to access to that dataset
                if (accessRightOptional.isPresent()
                        && !AccessLevel.NO_ACCESS.equals(accessRightOptional.get().getAccessLevel())) {
                    isAutorised = true;
                    // Stop loop iteration
                    break;
                }
            }
        }
        return isAutorised;
    }

    /**
     * Allow to send an update event for all {@link AccessRight}s with a dynamic plugin filter
     */

    @Override
    public void updateDynamicAccessRights() {
        Set<UniformResourceName> datasetsToUpdate = Sets.newHashSet();
        repository.findByDataAccessPluginNotNull().forEach(ar -> {
            try {
                if (!datasetsToUpdate.contains(ar.getDataset().getIpId())) {
                    IDataObjectAccessFilterPlugin plugin = pluginService
                            .getPlugin(ar.getDataAccessPlugin().getBusinessId());
                    if (plugin.isDynamic()) {
                        LOGGER.info("Updating dynamic accessRights for dataset {} - {}", ar.getDataset().getLabel(),
                                    ar.getDataset().getIpId());
                        datasetsToUpdate.add(ar.getDataset().getIpId());
                    }
                }
            } catch (ModuleException | NotAvailablePluginConfigurationException e) {
                LOGGER.error(String.format(
                                           "updateDynamicAccessRights - Error getting plugin %d for accessRight %d of "
                                                   + "dataset %s and group %s. Does plugin exist anymore ?",
                                           ar.getDataAccessPlugin().getId(), ar.getId(), ar.getDataset().getLabel(),
                                           ar.getAccessGroup().getName()),
                             e);
            }
        });

        if (!datasetsToUpdate.isEmpty()) {
            String message = String.format("Update of accessRights scheduled for %d datasets", datasetsToUpdate.size());
            try {
                FeignSecurityManager.asSystem();
                notificationClient.notify(message, "Dynamic access right update", NotificationLevel.INFO,
                                          DefaultRole.PROJECT_ADMIN, DefaultRole.ADMIN);
                FeignSecurityManager.reset();
            } catch (HttpClientErrorException | HttpServerErrorException e) {
                LOGGER.error(String.format("Error sending notification : %s", message), e);
            }

            datasetsToUpdate
                    .forEach(ds -> eventPublisher.publish(new AccessRightEvent(ds, AccessRightEventType.UPDATE)));
        }
    }

    private PluginConfiguration createPluginConfiguration(PluginConfiguration pluginConfiguration)
            throws ModuleException {
        // Check no identifier. For each new chain, we force plugin configuration creation. A configuration cannot be
        // reused.
        if (pluginConfiguration.getId() != null) {
            throw new EntityInvalidException(
                    String.format("Plugin configuration %s must not already have an identifier.",
                                  pluginConfiguration.getLabel()));
        }
        return pluginService.savePluginConfiguration(pluginConfiguration);
    }

    /**
     * Create or update a plugin configuration cleaning old one if necessary
     *
     * @param pluginConfiguration new plugin configuration or update
     * @param existing            existing plugin configuration
     * @return configuration to remove because it is no longer used
     * @throws ModuleException if error occurs!
     */
    private Optional<PluginConfiguration> updatePluginConfiguration(Optional<PluginConfiguration> pluginConfiguration,
            Optional<PluginConfiguration> existing) throws ModuleException {

        Optional<PluginConfiguration> confToRemove = Optional.empty();

        if (pluginConfiguration.isPresent()) {
            PluginConfiguration conf = pluginConfiguration.get();
            if (conf.getId() == null) {
                // Delete previous configuration if exists
                confToRemove = existing;
                // Save new configuration
                pluginService.savePluginConfiguration(conf);
            } else {
                // Update configuration
                pluginService.updatePluginConfiguration(conf);
            }
        } else {
            // Delete previous configuration if exists
            confToRemove = existing;
        }

        return confToRemove;
    }

}
