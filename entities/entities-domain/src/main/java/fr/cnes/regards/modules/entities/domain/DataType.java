/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

/**
 * {@link AbstractEntity} types
 *
 * @author lmieulet
 *
 */
public enum DataType {

    /**
     * Available data types
     */
    RAWDATA, QUICKLOOK, DOCUMENT, THUMBNAIL, OTHER;

    @Override
    public String toString() {
        return this.name();
    }
}
