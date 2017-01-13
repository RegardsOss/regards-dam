package fr.cnes.regards.modules.crawler.service;

import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.multitenant.ITenantResolver;

@Service
public class CrawlerService implements ICrawlerService {

    private ITenantResolver tenantResolver;

    public CrawlerService() {

    }

}
