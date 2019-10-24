package fr.cnes.regards.modules.model.dto.properties;

/**
 * @author oroussel
 */
public class LongProperty extends AbstractProperty<Long> {

    @Override
    public PropertyType getType() {
        return PropertyType.LONG;
    }
}
