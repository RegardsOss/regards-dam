/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.util.List;

/**
 * @author lmieulet
 *
 */
public class DataEntity extends Entity {

    /**
     *
     */
    private List<Data> files_;

    /**
     * @param pFiles
     */
    public DataEntity(String pSid_id, List<Data> pFiles) {
        super(pSid_id);
        files_ = pFiles;
    }

    /**
     * @return the files
     */
    public List<Data> getFiles() {
        return files_;
    }

    /**
     * @param pFiles
     *            the files to set
     */
    public void setFiles(List<Data> pFiles) {
        files_ = pFiles;
    }
}
