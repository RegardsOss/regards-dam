/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.fallback;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;

import fr.cnes.regards.modules.collections.client.CollectionsClient;
import fr.cnes.regards.modules.collections.domain.Collection;

/**
 * @author lmieulet
 *
 */
@Component
public class CollectionsFallback implements CollectionsClient {

    private static final Logger LOG = LoggerFactory.getLogger(CollectionsFallback.class);

    private static final String fallBackErrorMessage_ = "RS-DAM /collections request error. Fallback.";

    @Override
    public HttpEntity<List<Resource<Collection>>> retrieveCollectionList() {
        LOG.error(fallBackErrorMessage_);
        return null;
    }

    @Override
    public HttpEntity<List<Resource<Collection>>> retrieveCollectionListByModelId(Long pModelId) {
        LOG.error(fallBackErrorMessage_);
        return null;
    }

}
