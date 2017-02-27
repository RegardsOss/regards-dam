package fr.cnes.regards.modules.crawler.service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;

import fr.cnes.regards.framework.amqp.IPoller;
import fr.cnes.regards.framework.amqp.domain.TenantWrapper;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.multitenant.ITenantResolver;
import fr.cnes.regards.modules.crawler.dao.IEsRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.event.EntityEvent;
import fr.cnes.regards.modules.entities.service.IEntityService;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

@Service
public class CrawlerService implements ICrawlerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrawlerService.class);

    @Autowired
    private ITenantResolver tenantResolver;

    @Autowired
    private IRuntimeTenantResolver runtimeTenantResolver;

    @Autowired
    private IPoller poller;

    @Autowired
    @Qualifier(value = "entityService")
    private IEntityService entityService;

    @Autowired
    private IEsRepository esRepos;

    private ExecutorService executor;

    private boolean stopAsked = false;

    public CrawlerService() {
        executor = Executors.newSingleThreadExecutor();
    }

    @PostConstruct
    private void launchCrawl() {
        executor.execute(this::crawl);
    }

    @PreDestroy
    private void endCrawl() {
        System.out.println("end called");
        executor.shutdown();
        this.stopAsked = true;
    }

    public void crawl() {
        while (true) {
            if (this.stopAsked) {
                break;
            }
            // For all tenants
            for (String tenant : tenantResolver.getAllTenants()) {
                try {
                    runtimeTenantResolver.forceTenant(tenant);
                    this.doPoll();
                } catch (Throwable t) {
                    LOGGER.error("Cannot manage entity event message", t);
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Transactional(rollbackOn = Exception.class)
    private void doPoll() {
        // Try to poll an EntityEvent
        TenantWrapper<EntityEvent> wrapper = poller.poll(EntityEvent.class);
        if (wrapper != null) {
            UniformResourceName[] ipIds = wrapper.getContent().getIpIds();
            if ((ipIds != null) && (ipIds.length != 0)) {
                // Only one entity
                if (ipIds.length == 1) {
                    UniformResourceName ipId = ipIds[0];
                    LOGGER.info("received msg for " + ipId.toString());
                    AbstractEntity entity = entityService.loadWithRelations(ipId);
                    // If entity does no more exist in database, it must be deleted from ES
                    if (entity == null) {
                        esRepos.delete(wrapper.getTenant(), ipId.getEntityType().toString(), ipId.toString());
                    } else { // entity has been created or updated, it must be saved into ES
                        // First, check if index exists
                        if (!esRepos.indexExists(wrapper.getTenant())) {
                            esRepos.createIndex(wrapper.getTenant());
                        }
                        // Then save entity
                        esRepos.save(wrapper.getTenant(), entity);
                    }
                    LOGGER.info(ipId.toString() + " managed into Elasticsearch");
                } else if (ipIds.length > 1) { // serveral entities at once
                    LOGGER.info("received msg for " + Arrays.toString(ipIds));
                    Set<UniformResourceName> toDeleteIpIds = Sets.newHashSet(ipIds);
                    List<AbstractEntity> entities = entityService.loadAllWithRelations(ipIds);
                    entities.forEach(e -> toDeleteIpIds.remove(e.getIpId()));
                    // Entities to save
                    if (!entities.isEmpty()) {
                        if (!esRepos.indexExists(wrapper.getTenant())) {
                            esRepos.createIndex(wrapper.getTenant());
                        }
                        esRepos.saveBulk(wrapper.getTenant(), entities);
                    }
                    // Entities to remove
                    if (!toDeleteIpIds.isEmpty()) {
                        toDeleteIpIds.forEach(ipId -> esRepos.delete(wrapper.getTenant(),
                                                                     ipId.getEntityType().toString(), ipId.toString()));
                    }
                    LOGGER.info(Arrays.toString(ipIds) + " managed into Elasticsearch");
                }
            }
        }
    }
}
