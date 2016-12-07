/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.module.annotation.ModuleInfo;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.dataaccess.domain.Greeting;
import fr.cnes.regards.modules.dataaccess.service.GreetingsService;

/**
 * REST module controller
 *
 * TODO Description
 *
 * @author TODO
 *
 */
@RestController
@ModuleInfo(name = "dataaccess", version = "1.0-SNAPSHOT", author = "REGARDS", legalOwner = "CS",
        documentation = "http://test")
@RequestMapping("/api")
public class GreetingsController implements IResourceController<Greeting> {

    @Autowired
    private IResourceService resourceService;

    @Autowired
    private GreetingsService myService;

    @RequestMapping(value = "/greeting", method = RequestMethod.GET)
    @ResponseBody
    @ResourceAccess(description = "send 'greeting' as response")
    public HttpEntity<Resource<Greeting>> greeting(@RequestParam(value = "name", defaultValue = "World") String pName) {
        Greeting greeting = myService.getGreeting(pName);
        return new ResponseEntity<>(new Resource<Greeting>(greeting), HttpStatus.OK);
    }

    @RequestMapping(value = "/me", method = RequestMethod.GET)
    @ResponseBody
    @ResourceAccess(description = "send 'me' as response")
    public HttpEntity<Resource<Greeting>> me(@RequestParam(value = "name", defaultValue = "me") String pName) {
        Greeting greeting = myService.getGreeting(pName);
        return new ResponseEntity<>(new Resource<Greeting>(greeting), HttpStatus.OK);
    }

    @Override
    public Resource<Greeting> toResource(Greeting pElement, Object... pExtras) {
        // TODO add hateoas links
        return resourceService.toResource(pElement);
    }
}
