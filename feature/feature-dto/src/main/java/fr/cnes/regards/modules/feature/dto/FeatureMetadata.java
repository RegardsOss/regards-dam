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
package fr.cnes.regards.modules.feature.dto;

import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.util.Assert;

/**
 *
 * The sessionOwner and session allows to make groups of features.
 *
 * @author Marc Sordi
 *
 */
public class FeatureMetadata {

    public static final String MISSING_SESSION_OWNER = "Identifier of the session owner that submitted the feature is required";

    public static final String MISSING_SESSION = "Session is required";

    public static final String MISSING_STORAGE_METADATA = "Storage metadata is required";

    @NotBlank(message = MISSING_SESSION_OWNER)
    @Size(max = 128)
    private String sessionOwner;

    @NotBlank(message = MISSING_SESSION)
    @Size(max = 128)
    private String session;

    @Valid
    @NotNull(message = MISSING_STORAGE_METADATA)
    private List<StorageMetadata> storages;

    public String getSessionOwner() {
        return sessionOwner;
    }

    public void setSessionOwner(String sessionOwner) {
        this.sessionOwner = sessionOwner;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public List<StorageMetadata> getStorages() {
        return storages;
    }

    public void setStorages(List<StorageMetadata> storages) {
        this.storages = storages;
    }

    public boolean hasStorage() {
        return !storages.isEmpty();
    }

    /**
    
     * Build ingest metadata
     * @param sessionOwner Owner of the session
     * @param session session
     * @param storages storage metadata
     */
    public static FeatureMetadata build(String sessionOwner, String session, StorageMetadata... storages) {
        return FeatureMetadata.build(sessionOwner, session, Arrays.asList(storages));
    }

    /**
     * Build ingest metadata
     * @param sessionOwner Owner of the session
     * @param session session
     * @param storages storage metadata
     */
    public static FeatureMetadata build(String sessionOwner, String session, List<StorageMetadata> storages) {
        Assert.hasLength(sessionOwner, MISSING_SESSION_OWNER);
        Assert.hasLength(session, MISSING_SESSION);
        Assert.notNull(storages, MISSING_STORAGE_METADATA);
        FeatureMetadata m = new FeatureMetadata();
        m.setSessionOwner(sessionOwner);
        m.setSession(session);
        m.setStorages(storages);
        return m;
    }
}
