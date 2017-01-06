/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.util.List;

import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * @author lmieulet
 *
 */
public abstract class DataEntity extends AbstractEntity {

    /**
     *
     */
    private List<Data> files;

    public DataEntity(EntityType pEntityType) {
        super(null, pEntityType);
    }

    public DataEntity(EntityType pEntityType, Model pModel) {
        super(pModel, pEntityType);
    }

    /**
     * @return the files
     */
    public List<Data> getFiles() {
        return files;
    }

    /**
     * @param pFiles
     *            the files to set
     */
    public void setFiles(List<Data> pFiles) {
        files = pFiles;
    }
}
