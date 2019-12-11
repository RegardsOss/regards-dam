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
package fr.cnes.regards.modules.notifier.service.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * notifier configuration properties
 *
 * @author Marc SORDI
 *
 */
@Configuration
public class NotificationConfigurationProperties {

    /**
     * Max number of requests to process at a time
     */
    @Value("${regards.notifier.max.bulk.size:1000}")
    private Integer maxBulkSize;

    /**
     * Batch message size
     */
    @Value("${regards.notifier.batch.size:1000}")
    private Integer batchSize;

    /**
     * Batch message reception in millisecond
     */
    @Value("${regards.notifier.batch.receive.timeout:1000}")
    private Long batchReceiveTimeout;

    /**
     * In second.
     */
    @Value("${regards.notifier.delay.before.processing:5}")
    private Integer delayBeforeProcessing;

    /*
     * In minute.
     */
    @Value("${regards.notifier.remote.request.timeout:5}")
    private Long remoteRequestTimeout;

    @Value("${regards.notifier.metrics.enabled:false}")
    private Boolean metricsEnabled;

    public Boolean isMetricsEnabled() {
        return metricsEnabled;
    }

    public void setMetricsEnabled(Boolean metricsEnabled) {
        this.metricsEnabled = metricsEnabled;
    }

    public Integer getMaxBulkSize() {
        return maxBulkSize;
    }

    public void setMaxBulkSize(Integer maxBulkSize) {
        this.maxBulkSize = maxBulkSize;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    public Long getRemoteRequestTimeout() {
        return remoteRequestTimeout;
    }

    public void setRemoteRequestTimeout(Long remoteRequestTimeout) {
        this.remoteRequestTimeout = remoteRequestTimeout;
    }

    public Integer getDelayBeforeProcessing() {
        return delayBeforeProcessing;
    }

    public void setDelayBeforeProcessing(Integer delayBeforeProcessing) {
        this.delayBeforeProcessing = delayBeforeProcessing;
    }

    public Long getBatchReceiveTimeout() {
        return batchReceiveTimeout;
    }

    public void setBatchReceiveTimeout(Long batchReceiveTimeout) {
        this.batchReceiveTimeout = batchReceiveTimeout;
    }
}
