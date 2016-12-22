package fr.cnes.regards.modules.entities.service.crawler;

import org.springframework.stereotype.Service;

import fr.cnes.regards.modules.collections.service.ICollectionsRequestService;

@Service
public class CrawlerService implements ICrawlerService {

    public CrawlerService(ICollectionsRequestService pCollectionsRequestService) {

    }
}
