/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.dao;

import java.util.List;
import java.util.Set;

import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.IEntityPagingAndSortingRepository;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

/**
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
public interface IAbstractEntityRepository<T extends AbstractEntity> extends IEntityPagingAndSortingRepository<T> {

    /**
     * @param pIpIds
     * @return
     */
    List<AbstractEntity> findByIpIdIn(Set<UniformResourceName> pIpIds);

    /**
     * @param pTagToSearch
     * @return
     */
    List<AbstractEntity> findByTagsValue(String pTagToSearch);

}
