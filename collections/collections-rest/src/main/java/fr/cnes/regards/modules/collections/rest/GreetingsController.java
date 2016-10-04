/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.modules.core.annotation.ModuleInfo;
import fr.cnes.regards.security.utils.endpoint.annotation.ResourceAccess;
import fr.cnes.regards.modules.collections.domain.Greeting;
import fr.cnes.regards.modules.collections.service.GreetingsService;

/**
 * REST module controller
 * 
 * TODO Description
  * 
* @author TODO
 *
 */
@RestController
@ModuleInfo(name="collections", version="1.0-SNAPSHOT", author="REGARDS", legalOwner="CS", documentation="http://test")
@RequestMapping("/api")
public class GreetingsController {

    @Autowired
    GreetingsService myService_;

    @RequestMapping(value = "/greeting", method = RequestMethod.GET)
    @ResourceAccess(description = "send 'greeting' as response")
    public HttpEntity<Resource<Greeting>> greeting(@RequestParam(value = "name", defaultValue = "World") String pName) {
        Greeting greeting = myService_.getGreeting(pName);
            return new ResponseEntity<>(new Resource<Greeting>(greeting), HttpStatus.OK);
    }

    @RequestMapping(value = "/me", method = RequestMethod.GET)
    @ResourceAccess(description = "send 'me' as response")
    public HttpEntity<Resource<Greeting>> me(@RequestParam(value = "name", defaultValue = "me") String pName) {
        Greeting greeting = myService_.getGreeting(pName);
            return new ResponseEntity<>(new Resource<Greeting>(greeting), HttpStatus.OK);
   }
}
