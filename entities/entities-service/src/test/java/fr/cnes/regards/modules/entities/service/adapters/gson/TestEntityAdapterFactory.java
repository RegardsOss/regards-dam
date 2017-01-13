/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.adapters.gson;

/**
 * @author Marc Sordi
 *
 */
public class TestEntityAdapterFactory extends EntityAdapterFactory {

    protected TestEntityAdapterFactory() {
        super();
        registerSubtype(Car.class, "CAR");
    }
}
