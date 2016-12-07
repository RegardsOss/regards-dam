/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.service;

import org.springframework.stereotype.Service;

import fr.cnes.regards.modules.dataaccess.domain.Greeting;


/**
 * 
 * TODO Description
 * @author TODO
 *
 */
@Service
public class GreetingsService implements IGreetingsService {

    @Override
    public Greeting getGreeting(String pName) {
        return new Greeting(pName);
    }

}