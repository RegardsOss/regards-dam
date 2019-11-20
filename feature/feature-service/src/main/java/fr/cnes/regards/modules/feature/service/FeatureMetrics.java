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
package fr.cnes.regards.modules.feature.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.cnes.regards.modules.feature.dto.urn.FeatureUniformResourceName;
import fr.cnes.regards.modules.feature.service.conf.FeatureConfigurationProperties;

/**
 * Dedicated class for feature metrics
 *
 * @author Marc SORDI
 *
 */
@Component
public class FeatureMetrics {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureMetrics.class);

    protected static final Marker METRICS_MARKER = MarkerFactory.getMarker("METRICS");

    private static final String METRICS_FORMAT = "Feature ID {} - State {}";

    //private static final String METRICS_FORMAT = "Feature ID [{}] - URN [{}] - State [{}]";

    @Autowired
    private FeatureConfigurationProperties properties;

    public static enum FeatureCreationState {
        // Related request state
        CREATION_REQUEST_PENDING,
        CREATION_REQUEST_DENIED,
        CREATION_REQUEST_ERROR,
        CREATION_REQUEST_SCHEDULED,
        // Intermediate state
        FEATURE_INITIALIZED,
        // Final stated
        FEATURE_CREATED;
    }

    public static enum FeatureUpdateState {
        // Related request state
        UPDATE_REQUEST_PENDING,
        UPDATE_REQUEST_DENIED,
        UPDATE_REQUEST_ERROR,
        UPDATE_REQUEST_SCHEDULED,
        // Intermediate state
        FEATURE_MERGED,
        // Final stated
        FEATURE_UPDATED;
    }

    // FIXME add urn to log?
    public void state(String providerId, FeatureUniformResourceName urn, FeatureCreationState state) {
        if (properties.isMetricsEnabled()) {
            LOGGER.debug(METRICS_MARKER, METRICS_FORMAT, providerId, state);
        }
    }

    // FIXME add urn to log?
    public void state(String providerId, FeatureUniformResourceName urn, FeatureUpdateState state) {
        if (properties.isMetricsEnabled()) {
            LOGGER.debug(METRICS_MARKER, METRICS_FORMAT, providerId, state);
        }
    }
}
