/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.service;

import java.util.List;

import fr.cnes.regards.modules.collections.domain.Collection;

/**
 * @author lmieulet
 *
 */
public interface ICollectionsRequestService {

    public List<Collection> retrieveCollectionList();

    public List<Collection> retrieveCollectionListByModelId(Long pModelId);

}
