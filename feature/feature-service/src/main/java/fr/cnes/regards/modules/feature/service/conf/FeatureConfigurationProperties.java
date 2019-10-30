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
package fr.cnes.regards.modules.feature.service.conf;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Feature configuration properties
 *
 * @author Marc SORDI
 *
 */
@Configuration
public class FeatureConfigurationProperties {

    /**
     * Max number of requests to process at a time
     */
    @Value("${regards.feature.max.bulk.size:2000}")
    private Integer maxBulkSize;

    /**
     * Batch message reception in millisecond
     */
    @Value("${regards.feature.batch.receive.timeout:1000}")
    private Long batchReceiveTimeout;

    /**
     * In second.
     */
    @Value("${regards.feature.delay.before.processing:5}")
    private Integer delayBeforeProcessing;

    /*
     * In minute.
     */
    @Value("${regards.feature.remote.request.timeout:5}")
    private Long remoteRequestTimeout;

    public Integer getMaxBulkSize() {
        return maxBulkSize;
    }

    public void setMaxBulkSize(Integer maxBulkSize) {
        this.maxBulkSize = maxBulkSize;
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
