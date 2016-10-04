/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.service;

import fr.cnes.regards.modules.collections.domain.Greeting;

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
