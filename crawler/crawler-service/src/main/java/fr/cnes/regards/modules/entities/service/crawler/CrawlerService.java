package fr.cnes.regards.modules.entities.service.crawler;

import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.multitenant.autoconfigure.tenant.ITenantResolver;

@Service
public class CrawlerService implements ICrawlerService {

    private ITenantResolver tenantResolver;

    public CrawlerService() {

    }

}
