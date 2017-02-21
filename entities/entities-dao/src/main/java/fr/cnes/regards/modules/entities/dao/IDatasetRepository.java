/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fr.cnes.regards.modules.entities.domain.Dataset;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Repository
public interface IDatasetRepository extends IAbstractEntityRepository<Dataset> {

    @Query("from Dataset ds left join fetch ds.pluginConfigurationIds where ds.id=:id")
    Dataset findOneWithPluginConfigurations(@Param("id") Long pDatasetId);

    /**
     * @param pDatasetId
     * @return
     */
    @Query("from Dataset ds left join fetch ds.descriptionFile where ds.id=:id")
    Dataset findOneDescriptionFile(@Param("id") Long pDatasetId);

    List<Dataset> findByGroups(String group);
}
