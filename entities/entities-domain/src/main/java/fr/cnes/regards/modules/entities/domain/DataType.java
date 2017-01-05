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

    RAWDATA, QUICKLOOK, DOCUMENT, OTHER;

    @Override
    public String toString() {
        return this.name();
    }
}
