/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service.geoparser;

/**
 * Enumerates the parameters handled by the {@link GeoParser} defined in OpenSearch-Geo extension.
 *
 * @see http://www.opensearch.org/Specifications/OpenSearch/Extensions/Geo/1.0/Draft_2
 * @author Xavier-Alexandre Brochard
 */
public enum GeoParameters {

    G("g"),
    LAT("lat"),
    LON("lon"),
    R("r");

    /**
     * Small caps correspondance in the OpenSearch request
     */
    private final String name;

    /**
     * @param pName
     */
    private GeoParameters(String pName) {
        name = pName;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

}
