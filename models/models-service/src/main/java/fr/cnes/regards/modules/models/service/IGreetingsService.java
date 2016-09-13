/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service;

import fr.cnes.regards.modules.models.domain.Greeting;

/**
 * 
 * TODO Description
 * @author TODO
 *
 */
public interface IGreetingsService {

    /**
     *
     * Get greetings
     *
     * @return {@link Greeting}
     * @since 1.0.0
     */
    Greeting getGreeting(String pName);

}
