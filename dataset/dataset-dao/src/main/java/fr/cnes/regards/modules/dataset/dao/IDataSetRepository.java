/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataset.dao;

import fr.cnes.regards.modules.dataset.domain.DataSet;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
// FIXME: check if should extends AbstractEntityRepository directly or CollectionRepository
public interface IDataSetRepository extends IAbstractEntityRepository<DataSet> {

}
