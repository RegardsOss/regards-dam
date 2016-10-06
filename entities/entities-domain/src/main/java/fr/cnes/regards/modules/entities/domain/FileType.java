/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

/**
 * @author lmieulet
 * @TODO : Why this class is a clone of DataType ?
 */
public enum FileType {
    RAWDATA, QUICKLOOK, DOCUMENT, OTHER;

    @Override
    public String toString() {
        return this.name();
    }
}
