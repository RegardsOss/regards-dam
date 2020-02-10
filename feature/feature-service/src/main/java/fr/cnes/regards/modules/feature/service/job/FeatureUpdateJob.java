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
package fr.cnes.regards.modules.feature.service.job;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.reflect.TypeToken;

import fr.cnes.regards.framework.modules.jobs.domain.AbstractJob;
import fr.cnes.regards.framework.modules.jobs.domain.JobParameter;
import fr.cnes.regards.framework.modules.jobs.domain.exception.JobParameterInvalidException;
import fr.cnes.regards.framework.modules.jobs.domain.exception.JobParameterMissingException;
import fr.cnes.regards.modules.feature.dao.IFeatureUpdateRequestRepository;
import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.domain.request.FeatureUpdateRequest;
import fr.cnes.regards.modules.feature.service.FeatureMetrics;
import fr.cnes.regards.modules.feature.service.FeatureMetrics.FeatureUpdateState;
import fr.cnes.regards.modules.feature.service.IFeatureUpdateService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 * @author Marc SORDI
 *
 */
public class FeatureUpdateJob extends AbstractJob<Void> {

    public static final String IDS_PARAMETER = "ids";

    private List<FeatureUpdateRequest> featureUpdateRequests;

    @Autowired
    private IFeatureUpdateRequestRepository featureUpdateRequestRepo;

    @Autowired
    private IFeatureUpdateService featureUpdateService;

    @Autowired
    private MeterRegistry registry;

    @Autowired
    private FeatureMetrics metrics;

    @Override
    public void setParameters(Map<String, JobParameter> parameters)
            throws JobParameterMissingException, JobParameterInvalidException {
        Type type = new TypeToken<Set<Long>>() {

        }.getType();
        featureUpdateRequests = this.featureUpdateRequestRepo
                .findAllByIdInOrderByRequestDateAsc(getValue(parameters, IDS_PARAMETER, type));
    }

    @Override
    public void run() {
        LOGGER.info("[{}] Feature update job starts", jobInfoId);
        long start = System.currentTimeMillis();
        Timer.Sample sample = Timer.start(registry);
        Set<FeatureEntity> updated = featureUpdateService.processRequests(featureUpdateRequests);
        sample.stop(Timer.builder(this.getClass().getName()).tag("job", "run").register(registry));
        updated.forEach(e -> metrics.count(e.getProviderId(), e.getFeature().getUrn(),
                                           FeatureUpdateState.FEATURE_UPDATED));
        LOGGER.info("[{}]{}{} update request(s) processed in {} ms", jobInfoId, INFO_TAB, featureUpdateRequests.size(),
                    System.currentTimeMillis() - start);
    }
}
