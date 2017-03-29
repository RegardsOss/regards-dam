package fr.cnes.regards.modules.entities.domain.geometry;

/**
 * Coordinate Reference system
 * @author oroussel
 */
public enum Crs {
    EARTH("WGS84"),
    MARS("IAU2000:49900");

    private final String systemName;

    private Crs(String systemName) {
        this.systemName = systemName;
    }

    @Override
    public String toString() {
        return this.systemName;
    }

    public static Crs fromString(String systemName) {
        for (Crs type : values()) {
            if (type.systemName.equals(systemName)) {
                return type;
            }
        }
        throw new IllegalArgumentException(String.format("No enum constant for system name %s", systemName));
    }
}
