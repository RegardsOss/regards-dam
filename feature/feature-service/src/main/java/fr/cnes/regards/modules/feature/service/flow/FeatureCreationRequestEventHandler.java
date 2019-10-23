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
package fr.cnes.regards.modules.feature.service.flow;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import fr.cnes.regards.framework.amqp.ISubscriber;
import fr.cnes.regards.framework.utils.request.handler.AbstractRequestFlowHandler;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureCreationRequestEvent;
import fr.cnes.regards.modules.feature.service.IFeatureCreationService;
import fr.cnes.regards.modules.feature.service.conf.FeatureConfigurationProperties;

/**
 * This handler absorbs the incoming creation request flow
 *
 * @author Marc SORDI
 *
 */
@Component
public class FeatureCreationRequestEventHandler extends AbstractRequestFlowHandler<FeatureCreationRequestEvent>
		implements ApplicationListener<ApplicationReadyEvent> {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(FeatureCreationRequestEventHandler.class);

	@Autowired
	private FeatureConfigurationProperties confProperties;

	@Autowired
	private ISubscriber subscriber;

	@Autowired
	private IFeatureCreationService featureService;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		subscriber.subscribeTo(FeatureCreationRequestEvent.class, this);
	}

	/**
	 * Bulk save queued items every second.
	 */
	@Override
	@Scheduled(fixedDelayString = "${regards.feature.request.flow.bulk.delay:1000}")
	protected void handleQueue() {
		super.handleQueue();
	}

	@Override
	protected Integer getBulkSize() {
		return confProperties.getMaxBulkSize();
	}

	@Override
	protected void processBulk(List<FeatureCreationRequestEvent> items) {
		featureService.registerRequests(items);
	}
}
