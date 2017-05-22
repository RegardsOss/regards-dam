/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.dao;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

/**
 * Specific requests on Dataset
 *
 * @author Sylvain Vissiere-Guerinet
 * @author oroussel
 */
@Repository
public interface IDatasetRepository extends IAbstractEntityRepository<Dataset> {


    List<Dataset> findByGroups(String group);

    /**
     * Find entity giving its id eagerly loading its common relations (ie relations defined into AbstractEntity)
     *
     * @param pId id of entity
     * @return entity
     */
    @Override
    @EntityGraph(attributePaths = { "tags", "groups", "quotations", "model", "plgConfDataSource.parameters",
            "plgConfDataSource.parameters.dynamicsValues" })
    Dataset findById(Long pId);

    /**
     * Find all datasets of which ipId belongs to given set (eagerly loading all relations)
     *
     * @param pIpIds set of ipId
     * @return found entities
     */
    @Override
    @EntityGraph(attributePaths = { "tags", "groups", "quotations", "model", "plgConfDataSource.parameters",
            "plgConfDataSource.parameters.dynamicsValues", "descriptionFile" })
    List<Dataset> findByIpIdIn(Set<UniformResourceName> pIpIds);

    /**
     * Find entity of given IpId eagerly loading all common relations (except pluginConfigurationIds)
     *
     * @param pIpId ipId of which entity
     * @return found entity
     */
    @Override
    @EntityGraph(attributePaths = { "tags", "groups", "quotations", "model", "plgConfDataSource.parameters",
            "plgConfDataSource.parameters.dynamicsValues", "descriptionFile" })
    Dataset findByIpId(UniformResourceName pIpId);

    /**
     * Find all entities complient with the given modelName
     *
     * @param pModelName name of the model we want to be complient with
     * @return datasets complient with the given model
     */
    @Override
    @EntityGraph(attributePaths = { "tags", "groups", "quotations", "model", "plgConfDataSource.parameters",
            "plgConfDataSource.parameters.dynamicsValues" })
    Set<Dataset> findAllByModelName(String pModelName);

    @Query("from Dataset ds left join fetch ds.descriptionFile where ds.id=:id")
    Dataset findOneDescriptionFile(@Param("id") Long datasetId);

}
