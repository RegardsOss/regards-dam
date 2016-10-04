/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.regards.modules.collections.domain.Greeting;

public class GreetingsClientFallback implements IGreetingsClient {

    private static final Logger LOG = LoggerFactory.getLogger(GreetingsClientFallback.class);

    @Override
    public Greeting greeting(String pName) {
        LOG.error("Error greeting");
        return new Greeting(pName);
    }
    
    public Greeting me(String pName) {
        LOG.error("Error me");
        return new Greeting(pName);
    }

}