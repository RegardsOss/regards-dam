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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

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

    @Autowired
    private MeterRegistry registry;

    @Autowired
    private FeatureConfigurationProperties properties;

    private final Map<String, AtomicLong> gauges = new HashMap<>();

    private final Map<String, Counter> counters = new HashMap<>();

    public static enum MetricType {
        GAUGE,
        COUNTER;
    }

    public static enum FeatureCreationState {
        CREATION_REQUEST_GRANTED("granted.creation.requests", MetricType.GAUGE),
        CREATION_REQUEST_DENIED("denied.creation.requests", MetricType.GAUGE),
        CREATION_REQUEST_ERROR("error.creation.requests", MetricType.GAUGE),
        CREATION_REQUEST_SUCCESS("successful.creation.requests", MetricType.GAUGE),
        CREATION_REQUEST_SCHEDULED("scheduled.creation.requests", MetricType.GAUGE),
        FEATURE_INITIALIZED("initialized.features", MetricType.COUNTER),
        FEATURE_CREATED("created.features", MetricType.COUNTER);

        private final String name;

        private final MetricType type;

        private FeatureCreationState(String name, MetricType type) {
            this.name = METRICS_PREFIX + name;
            this.type = type;
        }

        /**
         * @return the metric name
         */
        public String getName() {
            return name;
        }

        /**
         * @return the metric type
         */
        public MetricType getType() {
            return type;
        }
    }

    public static enum FeatureUpdateState {
        UPDATE_REQUEST_GRANTED("granted.update.requests", MetricType.GAUGE),
        UPDATE_REQUEST_DENIED("denied.update.requests", MetricType.GAUGE),
        UPDATE_REQUEST_ERROR("error.update.requests", MetricType.GAUGE),
        UPDATE_REQUEST_SUCCESS("successful.update.requests", MetricType.GAUGE),
        UPDATE_REQUEST_SCHEDULED("scheduled.update.requests", MetricType.GAUGE),
        FEATURE_MERGED("merged.features", MetricType.GAUGE),
        FEATURE_UPDATED("updated.features", MetricType.COUNTER);

        private final String name;

        private final MetricType type;

        private FeatureUpdateState(String name, MetricType type) {
            this.name = METRICS_PREFIX + name;
            this.type = type;
        }

        /**
         * @return the metric name
         */
        public String getName() {
            return name;
        }

        /**
         * @return the metric type
         */
        public MetricType getType() {
            return type;
        }
    }

    @PostConstruct
    public void initialize() {
        for (FeatureCreationState state : FeatureCreationState.values()) {
            initMetric(state.getName(), state.getType());
        }
        for (FeatureUpdateState state : FeatureUpdateState.values()) {
            initMetric(state.getName(), state.getType());
        }
    }

    private void initMetric(String name, MetricType type) {
        switch (type) {
            case COUNTER:
                counters.put(name, registry.counter(name));
                break;
            case GAUGE:
                gauges.put(name, registry.gauge(name, new AtomicLong(0)));
                break;
            default:
                throw new UnsupportedOperationException(String.format("Unsupported metric type %s", type));
        }

    }

    // FIXME add urn to log?
    public void state(String providerId, FeatureUniformResourceName urn, FeatureCreationState state) {
        if (properties.isMetricsEnabled()) {
            LOGGER.debug(METRICS_MARKER, METRICS_FORMAT, providerId, state);

            switch (state) {
                case CREATION_REQUEST_DENIED:
                case CREATION_REQUEST_GRANTED:
                    gauges.get(state.getName()).incrementAndGet();
                    break;

                case CREATION_REQUEST_ERROR:
                    // TODO
                    break;

                case CREATION_REQUEST_SCHEDULED:
                    gauges.get(state.getName()).incrementAndGet();
                    gauges.get(FeatureCreationState.CREATION_REQUEST_GRANTED.getName()).decrementAndGet();
                    break;

                case FEATURE_INITIALIZED:
                    counters.get(state.getName()).increment();
                    gauges.get(FeatureCreationState.CREATION_REQUEST_SCHEDULED.getName()).decrementAndGet();
                    break;

                case FEATURE_CREATED:
                    counters.get(state.getName()).increment();
                    break;

                case CREATION_REQUEST_SUCCESS:
                    // TODO
                    break;
            }
        }
    }

    // FIXME add urn to log?
    public void state(String providerId, FeatureUniformResourceName urn, FeatureUpdateState state) {
        if (properties.isMetricsEnabled()) {
            LOGGER.debug(METRICS_MARKER, METRICS_FORMAT, providerId, state);
        }
    }
}
