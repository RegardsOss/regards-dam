/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.gson;

import fr.cnes.regards.modules.entities.gson.EntityAdapterFactory;

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
