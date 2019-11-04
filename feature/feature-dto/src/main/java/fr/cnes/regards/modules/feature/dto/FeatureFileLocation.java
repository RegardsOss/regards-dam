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

import java.net.MalformedURLException;
import java.nio.file.Path;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 *
 * {@link FeatureFile} storage location information
 *
 * @author Marc SORDI
 *
 */
public class FeatureFileLocation {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureFileLocation.class);

    /**
     * Storage identifier may be null, so the file is directly accessible through FILE or HTTP URL protocol
     */
    private String storage;

    /**
     * URL to access the file
     */
    @NotNull(message = "URL is required")
    private String url;

    /**
     * Build a file location directly accessible through FILE or HTTP URL protocol
     */
    public static FeatureFileLocation build(String url) {
        Assert.notNull(url, "URL is required");
        return buildInternal(url, null);
    }

    /**
     * Build a file location
     * accessible through storage service if storage identifier is recognize
     * else just treated as a reference.
     */
    public static FeatureFileLocation build(String url, String storage) {
        Assert.notNull(url, "URL is required");
        Assert.hasText(storage, "Storage identifier is required");
        return buildInternal(url, storage);
    }

    /**
     * Build a file location directly accessible through FILE protocol
     */
    public static FeatureFileLocation build(Path path) {
        Assert.notNull(path, "File path is required");
        try {
            return buildInternal(path.toUri().toURL().toString(), null);
        } catch (MalformedURLException e) {
            String errorMessage = String.format("Cannot transform %s to valid URL (MalformedURLException).",
                                                path.toString());
            LOGGER.error(errorMessage, e);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private static FeatureFileLocation buildInternal(String url, String storage) {
        FeatureFileLocation location = new FeatureFileLocation();
        location.setUrl(url);
        location.setStorage(storage);
        return location;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (storage == null ? 0 : storage.hashCode());
        result = prime * result + (url == null ? 0 : url.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FeatureFileLocation other = (FeatureFileLocation) obj;
        if (storage == null) {
            if (other.storage != null) {
                return false;
            }
        } else if (!storage.equals(other.storage)) {
            return false;
        }
        if (url == null) {
            if (other.url != null) {
                return false;
            }
        } else if (!url.equals(other.url)) {
            return false;
        }
        return true;
    }
}
