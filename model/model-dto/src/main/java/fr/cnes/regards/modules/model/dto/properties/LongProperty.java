package fr.cnes.regards.modules.model.dto.properties;

/**
 * @author oroussel
 */
public class LongProperty extends AbstractProperty<Long> {

    @Override
    public boolean represents(PropertyType pAttributeType) {
        return PropertyType.LONG.equals(pAttributeType);
    }

}
