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
import fr.cnes.regards.modules.feature.dao.IFeatureReferenceRequestRepository;
import fr.cnes.regards.modules.feature.domain.request.FeatureReferenceRequest;
import fr.cnes.regards.modules.feature.service.IFeatureReferenceService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

/**
 *
 * Store a new feature
 *
 * @author Marc SORDI
 *
 */
public class FeatureReferenceCreationJob extends AbstractJob<Void> {

    public static final String IDS_PARAMETER = "ids";

    private List<FeatureReferenceRequest> featureReferenceRequests;

    @Autowired
    private IFeatureReferenceRequestRepository featureReferenceRequestRepo;

    @Autowired
    private IFeatureReferenceService featureService;

    @Autowired
    private MeterRegistry registry;

    @Override
    public void setParameters(Map<String, JobParameter> parameters)
            throws JobParameterMissingException, JobParameterInvalidException {
        Type type = new TypeToken<Set<Long>>() {

        }.getType();
        featureReferenceRequests = this.featureReferenceRequestRepo
                .findAllById(getValue(parameters, IDS_PARAMETER, type));
    }

    @Override
    public void run() {
        LOGGER.info("[{}] Feature reference creation job starts", jobInfoId);
        long start = System.currentTimeMillis();
        Timer.Sample sample = Timer.start(registry);

        featureService.processRequests(featureReferenceRequests);

        sample.stop(Timer.builder(this.getClass().getName()).tag("job", "run").register(registry));
        LOGGER.info("[{}]{} reference creation request(s) processed in {} ms", jobInfoId, INFO_TAB,
                    System.currentTimeMillis() - start);
    }

}
