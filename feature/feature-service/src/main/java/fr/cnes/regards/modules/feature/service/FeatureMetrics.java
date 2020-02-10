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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.cnes.regards.modules.feature.dto.urn.FeatureUniformResourceName;
import fr.cnes.regards.modules.feature.service.conf.FeatureConfigurationProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

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

    private static final String METRICS_PREFIX = "regards.";

    //private static final String METRICS_FORMAT = "Feature ID [{}] - URN [{}] - State [{}]";

    private static final String METRIC_TYPE_TAG = "metric_type";

    private static final String METRIC_TYPE = "feature";

    private static final String METRIC_SUBTYPE_TAG = "metric_subtype";

    @Autowired
    private MeterRegistry registry;

    @Autowired
    private FeatureConfigurationProperties properties;

    private final Map<String, Counter> counters = new HashMap<>();

    public static enum FeatureCreationState {

        CREATION_REQUEST_GRANTED("granted.creation.requests"),
        CREATION_REQUEST_DENIED("denied.creation.requests"),
        CREATION_REQUEST_ERROR("error.creation.requests"),
        CREATION_REQUEST_SUCCESS("successful.creation.requests"),
        CREATION_REQUEST_SCHEDULED("scheduled.creation.requests"),
        FEATURE_INITIALIZED("initialized.features"),
        FEATURE_CREATED("created.features");

        private final String name;

        private FeatureCreationState(String name) {
            this.name = METRICS_PREFIX + name;
        }

        /**
         * @return the metric name
         */
        public String getName() {
            return name;
        }
    }

    public static enum FeatureUpdateState {

        UPDATE_REQUEST_GRANTED("granted.update.requests"),
        UPDATE_REQUEST_DENIED("denied.update.requests"),
        UPDATE_REQUEST_ERROR("error.update.requests"),
        UPDATE_REQUEST_SUCCESS("successful.update.requests"),
        UPDATE_REQUEST_SCHEDULED("scheduled.update.requests"),
        FEATURE_MERGED("merged.features"),
        FEATURE_UPDATED("updated.features");

        private final String name;

        private FeatureUpdateState(String name) {
            this.name = METRICS_PREFIX + name;
        }

        /**
         * @return the metric name
         */
        public String getName() {
            return name;
        }
    }

    @PostConstruct
    public void initialize() {
        if (properties.isMetricsEnabled()) {

            List<Tag> tags = Arrays.asList(Tag.of(METRIC_TYPE_TAG, METRIC_TYPE),
                                           Tag.of(METRIC_SUBTYPE_TAG, "creation"));
            for (FeatureCreationState state : FeatureCreationState.values()) {
                initMetric(state.getName(), tags);
            }
            tags = Arrays.asList(Tag.of(METRIC_TYPE_TAG, METRIC_TYPE), Tag.of(METRIC_SUBTYPE_TAG, "update"));
            for (FeatureUpdateState state : FeatureUpdateState.values()) {
                initMetric(state.getName(), tags);
            }
        }
    }

    private void initMetric(String name, Iterable<Tag> tags) {
        counters.put(name, Counter.builder(name).tags(tags).register(registry));
    }

    public void count(String providerId, FeatureUniformResourceName urn, FeatureCreationState state) {
        if (properties.isMetricsEnabled()) {
            LOGGER.debug(METRICS_MARKER, METRICS_FORMAT, providerId, state);
            counters.get(state.getName()).increment();
        }
    }

    public void count(String providerId, FeatureUniformResourceName urn, FeatureUpdateState state) {
        if (properties.isMetricsEnabled()) {
            LOGGER.debug(METRICS_MARKER, METRICS_FORMAT, providerId, state);
        }
    }
}
