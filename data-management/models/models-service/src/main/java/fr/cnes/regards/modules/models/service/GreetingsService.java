package fr.cnes.regards.modules.models.service;

import org.springframework.stereotype.Service;

import fr.cnes.regards.modules.models.domain.Greeting;
import fr.cnes.regards.modules.models.service.actions.GreetingAction;

@Service
public class GreetingsService implements IGreetingsService {

    @Override
    public Greeting getGreeting(String pName) {
    	// return new GreetingAction(pName).execute();
        return null;
    }

}