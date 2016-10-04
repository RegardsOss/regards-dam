/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

/**
 * @author lmieulet
 *
 */
public enum DataType {
    RAWDATA, QUICKLOOK, DOCUMENT, OTHER;

    @Override
    public String toString() {
        return this.name();
    }
}
