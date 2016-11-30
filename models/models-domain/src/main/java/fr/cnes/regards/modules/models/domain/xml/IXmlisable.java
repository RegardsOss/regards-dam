/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.xml;

/**
 *
 * Identify pojo canditates for XML import and export
 *
 * @param <X>
 *            XML element
 * @author Marc Sordi
 *
 */
public interface IXmlisable<X> {

    X toXml();

    void fromXml(X pXmlElement);
}
