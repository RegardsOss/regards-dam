package fr.cnes.regards.modules.models.rest;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.microservices.core.auth.MethodAutorizationService;
import fr.cnes.regards.microservices.core.auth.ResourceAccess;
import fr.cnes.regards.microservices.core.auth.RoleAuthority;
import fr.cnes.regards.microservices.core.information.ModuleInfo;
import fr.cnes.regards.modules.models.domain.Greeting;
import fr.cnes.regards.modules.models.service.GreetingsService;


/**
 * 
 * myService Rest controller for the microservice
 *
 */
@RestController
@ModuleInfo(name="models", version="1.0-SNAPSHOT", author="REGARDS", legalOwner="CS", documentation="http://test")
@RequestMapping("/api")
public class GreetingsController {

    @Autowired
    MethodAutorizationService authService_;

    @Autowired
    GreetingsService myService_;

    /**
     * Method to initiate REST resources authorizations.
     */
    @PostConstruct
    public void initAuthorisations() {
        authService_.setAutorities("/api/me@GET", new RoleAuthority("ADMIN"));
        authService_.setAutorities("/api/greeting@GET", new RoleAuthority("USER"));
    }

    @RequestMapping(value = "/greeting", method = RequestMethod.GET)
    @ResourceAccess
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String pName) {
        return myService_.getGreeting(pName);
    }

    @RequestMapping(value = "/me", method = RequestMethod.GET)
    @ResourceAccess
    public Greeting me(@RequestParam(value = "name", defaultValue = "me") String pName) {
        return myService_.getGreeting(pName);
    }

}
