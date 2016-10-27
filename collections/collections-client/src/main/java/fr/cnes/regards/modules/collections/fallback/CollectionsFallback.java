/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.fallback;

import java.util.List;

import javax.naming.OperationNotSupportedException;

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

    @Override
    public HttpEntity<Resource<Collection>> createCollection(Collection pCollection) {
        LOG.error(fallBackErrorMessage_);
        return null;
    }

    @Override
    public HttpEntity<Resource<Collection>> retrieveCollection(String pCollectionId) {
        LOG.error(fallBackErrorMessage_);
        return null;
    }

    @Override
    public HttpEntity<Resource<Collection>> updateCollection(String pCollectionId, Collection pCollection)
            throws OperationNotSupportedException {
        LOG.error(fallBackErrorMessage_);
        return null;
    }

    @Override
    public HttpEntity<Void> deleteCollection(String pCollectionId) {
        LOG.error(fallBackErrorMessage_);
        return null;
    }

}
