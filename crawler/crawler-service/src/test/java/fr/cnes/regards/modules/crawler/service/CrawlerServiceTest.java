package fr.cnes.regards.modules.crawler.service;

import java.time.LocalDateTime;
import java.util.Collections;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourcePlugin;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.service.adapters.gson.FlattenedAttributeAdapterFactory;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { CrawlerConfiguration.class })
public class CrawlerServiceTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(CrawlerServiceTest.class);

    @Autowired
    private FlattenedAttributeAdapterFactory gsonAttributeFactory;

    // private ICrawlerService service;

    @Autowired
    private IIndexerService indexerService;

    @Before
    public void tearUp() {

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSuckUp() {
        String tenant = "oracle";
        IDataSourcePlugin dsPlugin = new IDataSourcePlugin() {

            @Override
            public boolean isOutOfDate() {
                return false;
            }

            @Override
            public int getRefreshRate() {
                return 0;
            }

            @Override
            public Page<AbstractEntity> findAll(Pageable pPageable) {
                return new PageImpl<>(Collections.emptyList());
            }

            @Override
            public Page<AbstractEntity> findAll(Pageable pPageable, LocalDateTime pDate) {
                return null;
            }
        };
        // Creating index if it doesn't already exist
        indexerService.createIndex(tenant);
        // Retrieve first 1000 objects
        Page<AbstractEntity> page = dsPlugin.findAll(new PageRequest(0, 1000));
        // Save to ES
        LOGGER.info(String.format("save %d/%d entities", page.getNumberOfElements(), page.getTotalElements()));
        LOGGER.info("save 1000 entities");
        while (page.hasNext()) {
            page = dsPlugin.findAll(page.nextPageable());
            indexerService.saveBulkEntities(tenant, page.getContent());
            LOGGER.info(String.format("save %d/%d entities", page.getNumberOfElements(), page.getTotalElements()));
        }
        Assert.assertTrue(true);
    }
}
