/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.util.List;

import fr.cnes.regards.modules.models.domain.Model;

/**
 * @author lmieulet
 *
 */
public class DataEntity extends AbstractEntity {

    /**
     *
     */
    private List<Data> files;

    /**
     * @param pFiles
     */
    public DataEntity(Long pId, String pSidId, Model pModel, List<Data> pFiles) {
        super(pModel, pId);
        sipId = pSidId;
        files = pFiles;
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
