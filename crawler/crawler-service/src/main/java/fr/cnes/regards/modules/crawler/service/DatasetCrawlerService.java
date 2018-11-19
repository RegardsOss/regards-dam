package fr.cnes.regards.modules.crawler.service;

import java.time.OffsetDateTime;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.amqp.ISubscriber;
import fr.cnes.regards.framework.amqp.domain.IHandler;
import fr.cnes.regards.framework.amqp.domain.TenantWrapper;
import fr.cnes.regards.framework.jpa.utils.RegardsTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.dam.dao.entities.IDatasetRepository;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.event.AccessRightEvent;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.dam.domain.entities.event.DatasetEvent;
import fr.cnes.regards.modules.dam.domain.models.ModelAttrAssoc;
import fr.cnes.regards.modules.dam.service.entities.IDatasetService;
import fr.cnes.regards.modules.dam.service.models.event.ComputedAttributeModelEvent;

/**
 * Crawler service for Dataset. <b>This service need @EnableSchedule at Configuration</b>
 * @author oroussel
 */
@Service
public class DatasetCrawlerService extends AbstractCrawlerService<DatasetEvent>
        implements IDatasetCrawlerService, IHandler<AccessRightEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasetCrawlerService.class);

    @Autowired
    private ISubscriber subscriber;

    @Autowired
    private IDatasetService datasetService;

    @Autowired
    private IDatasetRepository datasetRepository;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    /**
     * Self proxy
     */
    @Autowired
    @Lazy
    private IDatasetCrawlerService self;

    @Override
    @EventListener
    public void onApplicationReadyEvent(ApplicationReadyEvent event) {
        subscriber.subscribeTo(AccessRightEvent.class, this);
    }

    @Override
    @EventListener
    @RegardsTransactional
    public void onComputedAttributeModelEvent(ComputedAttributeModelEvent event) {
        ModelAttrAssoc modelAttrAssoc = event.getSource();
        // Only recompute if a plugin conf is set (a priori if a plugin confis removed it is to be changed soon)
        if (modelAttrAssoc.getComputationConf() != null) {
            Set<Dataset> datasets = datasetService.findAllByModel(modelAttrAssoc.getModel().getId());
            for (Dataset dataset : datasets) {
                try {
                    datasetRepository.save(dataset);
                    entityIndexerService.updateEntityIntoEs(tenantResolver.getTenant(), dataset.getIpId(),
                                                            OffsetDateTime.now(), true);
                } catch (ModuleException e) {
                    LOGGER.error("Cannot update dataset", e);
                }
            }
        }
    }

    @Override
    @Async
    public void crawl() {
        super.crawl(self::doPoll);
    }

    @Override
    public void handle(TenantWrapper<AccessRightEvent> wrapper) {
        if (wrapper.getContent() != null) {
            AccessRightEvent event = wrapper.getContent();
            try {
                entityIndexerService.updateEntityIntoEs(wrapper.getTenant(), event.getDatasetIpId(),
                                                        OffsetDateTime.now(), false);
            } catch (ModuleException e) {
                LOGGER.error("Cannot handle access right event", e);
                // FIXME notify
            }
        }
    }

}
