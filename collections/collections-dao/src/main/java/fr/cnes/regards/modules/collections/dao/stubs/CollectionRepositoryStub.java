/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.dao.stubs;

import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import fr.cnes.regards.modules.collections.dao.ICollectionRepository;
import fr.cnes.regards.modules.collections.domain.Collection;

/**
 * @author lmieulet
 *
 */
@Repository
public class CollectionRepositoryStub extends RepositoryStub<Collection> implements ICollectionRepository {

    @Override
    public Iterable<Collection> findAllByModelId(Long pModelId) {
        return entities_.stream().filter(r -> r.getModel().getId().equals(pModelId)).collect(Collectors.toSet());
    }
}
