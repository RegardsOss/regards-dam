package fr.cnes.regards.modules.entities.domain.geometry;

/**
 * All GeoJson geometry types
 * @author oroussel
 */
public enum GeometryType {
    POINT("Point"), LINE_STRING("LineString"), MULTI_LINE_STRING("MultiLineString"), MULTI_POINT(
            "MultiPoint"), MULTI_POLYGON("MultiPolygon"), POLYGON("Polygon");

    private final String geoJson;

    private GeometryType(String geoJson) {
        this.geoJson = geoJson;
    }

    public static GeometryType fromString(String geoJson) {
        for (GeometryType type : values()) {
            if (type.geoJson.equals(geoJson)) {
                return type;
            }
        }
        throw new IllegalArgumentException(String.format("No enum constant for geoJson type %s", geoJson));
    }

    @Override
    public String toString() {
        return this.geoJson;
    }
}
