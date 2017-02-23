package fr.cnes.regards.modules.crawler.service;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.amqp.IPoller;
import fr.cnes.regards.framework.amqp.domain.TenantWrapper;
import fr.cnes.regards.framework.multitenant.ITenantResolver;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.event.CreateEntityEvent;
import fr.cnes.regards.modules.entities.service.ICollectionService;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

@Service
public class CrawlerService implements ICrawlerService {

    @Autowired
    private ITenantResolver tenantResolver;

    @Autowired
    private IPoller poller;

    @Autowired
    private ICollectionService entityService;

    @Autowired
    private IAbstractEntityRepository<AbstractEntity> entityRepository;

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
        //        try {
        //            Thread.sleep(10000);
        //        } catch (InterruptedException e) {
        //            e.printStackTrace();
        //        }
        while (true) {
            if (this.stopAsked) {
                break;
            }
            // For all tenants
            for (String tenant : tenantResolver.getAllTenants()) {
                try {
                    //                    poller.bind(tenant);
                    // Try polling message from current tenant
                    TenantWrapper<CreateEntityEvent> wrapper = poller.poll(CreateEntityEvent.class);
                    if (wrapper != null) {
                        UniformResourceName ipId = wrapper.getContent().getIpId();
                        System.out.println(ipId);
                        AbstractEntity entity = entityRepository.findByIpId(ipId);
                        entity.getGroups().toArray();
                        entity.getTags().toArray();
                        System.out.println(entity + ", " + Arrays.toString(entity.getTags().toArray()));
                    }
                } catch (IllegalStateException ise) {
                    System.out.println("ISE thrown");
                } finally {
                    //                    poller.unbind();
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
