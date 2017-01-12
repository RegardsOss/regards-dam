/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.util.List;

import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * Abstraction for entities managing data files
 * 
 * @author lmieulet
 * @author Marc Sordi
 *
 */
public abstract class AbstractDataEntity extends AbstractEntity {

    /**
     * Physical data file references
     */
    private List<Data> files;

    protected AbstractDataEntity() {
        this(null, null, null);
    }

    protected AbstractDataEntity(Model pModel, UniformResourceName pIpId, String pLabel) {
        super(pModel, pIpId, pLabel);
    }

    public List<Data> getFiles() {
        return files;
    }

    public void setFiles(List<Data> pFiles) {
        files = pFiles;
    }
}
