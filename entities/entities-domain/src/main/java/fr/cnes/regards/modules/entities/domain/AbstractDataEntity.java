/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.util.List;

import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * @author lmieulet
 *
 */
public abstract class AbstractDataEntity extends AbstractEntity {

    /**
     * Physical data file references
     */
    private List<Data> files;

    protected AbstractDataEntity(EntityType pEntityType) {
        this(null, null, pEntityType);
    }

    protected AbstractDataEntity(Model pModel, UniformResourceName pIpId, EntityType pEntityType) {
        super(pModel, pIpId, pEntityType);
    }

    public List<Data> getFiles() {
        return files;
    }

    public void setFiles(List<Data> pFiles) {
        files = pFiles;
    }
}
