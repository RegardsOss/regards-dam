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

import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.modules.collections.client.ICollectionsClient;
import fr.cnes.regards.modules.collections.domain.Collection;

/**
 * @author lmieulet
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Component
public class CollectionsFallback implements ICollectionsClient {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(CollectionsFallback.class);

    /**
     * Fall back message
     */
    private static final String FALL_BACK_ERROR_MESSAGE = "RS-DAM /collections request error. Fallback.";

    @Override
    public HttpEntity<Resource<Collection>> createCollection(Collection pCollection) {
        LOG.error(FALL_BACK_ERROR_MESSAGE);
        return null;
    }

    @Override
    public HttpEntity<Resource<Collection>> retrieveCollection(Long pCollectionId) {
        LOG.error(FALL_BACK_ERROR_MESSAGE);
        return null;
    }

    @Override
    public HttpEntity<Resource<Collection>> updateCollection(Long pCollectionId, Collection pCollection)
            throws EntityInconsistentIdentifierException {
        LOG.error(FALL_BACK_ERROR_MESSAGE);
        return null;
    }

    @Override
    public HttpEntity<Void> deleteCollection(Long pCollectionId) {
        LOG.error(FALL_BACK_ERROR_MESSAGE);
        return null;
    }

    @Override
    public HttpEntity<List<Resource<Collection>>> retrieveCollectionList(Long pModelId) {
        LOG.error(FALL_BACK_ERROR_MESSAGE);
        return null;
    }

}
