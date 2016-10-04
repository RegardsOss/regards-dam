/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.validation.constraints.NotNull;

/**
 * @author lmieulet
 *
 */
public class DataEntity extends Entities {

    @NotNull
    private final Long id_;

    /**
     *
     */
    private List<Data> files_;

    /**
     * @param pFiles
     */
    public DataEntity(List<Data> pFiles) {
        super();
        id_ = (long) ThreadLocalRandom.current().nextInt(1, 1000000);
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
