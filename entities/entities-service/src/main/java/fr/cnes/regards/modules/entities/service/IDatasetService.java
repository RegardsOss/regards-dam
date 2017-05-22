/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.DescriptionFile;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;

/**
 * Qualified interface for Dataset entity service
 *
 * @author oroussel
 */
public interface IDatasetService extends IEntityService<Dataset> {

    /**
     * Extract the AttributeModel of {@link DataObject} that can be contained into datasets. <br/>
     * If pUrns is null or empty AND pModelName is null then the scope of datasets is not restrained.<br/>
     * If pUrns is not null and not empty AND pModelName is not null then we only consider pModelName.<br/>
     * If pModelName is not null then the scope of datasets is restrained to all datasets complient with the given
     * model.<br/>
     * If pUrns is not null and not empty AND pModelName is null then the scope of datasets is restrained to all the
     * datasets represented by the given urns
     * 
     * @param pPageable
     */
    Page<AttributeModel> getDataAttributeModels(Set<UniformResourceName> pUrns, String pModelName, Pageable pPageable)
            throws ModuleException;

    DescriptionFile retrieveDescription(Long datasetId) throws EntityNotFoundException;

    void removeDescription(Long datasetId) throws EntityNotFoundException;
}