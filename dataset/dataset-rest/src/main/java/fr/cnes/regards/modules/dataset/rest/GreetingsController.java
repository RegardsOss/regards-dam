/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataset.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.module.annotation.ModuleInfo;

/**
 * REST module controller
 *
 * TODO Description PLACEHOLDER FOR GIT, TO BE REPLACED BY DATASET CONTROLLER
 *
 * @author TODO
 *
 */
@RestController
@ModuleInfo(name = "dataset", version = "1.0-SNAPSHOT", author = "REGARDS", legalOwner = "CS",
        documentation = "http://test")
@RequestMapping("/api")
public class GreetingsController implements IResourceController<Object> {

    @Autowired
    private IResourceService resourceService;

    // @Autowired
    // private GreetingsService myService;
    //
    // @RequestMapping(value = "/greeting", method = RequestMethod.GET)
    // @ResponseBody
    // @ResourceAccess(description = "send 'greeting' as response")
    // public HttpEntity<Resource<Greeting>> greeting(@RequestParam(value = "name", defaultValue = "World") String
    // pName) {
    // Greeting greeting = myService.getGreeting(pName);
    // return new ResponseEntity<>(new Resource<Greeting>(greeting), HttpStatus.OK);
    // }
    //
    // @RequestMapping(value = "/me", method = RequestMethod.GET)
    // @ResponseBody
    // @ResourceAccess(description = "send 'me' as response")
    // public HttpEntity<Resource<Greeting>> me(@RequestParam(value = "name", defaultValue = "me") String pName) {
    // Greeting greeting = myService.getGreeting(pName);
    // return new ResponseEntity<>(new Resource<Greeting>(greeting), HttpStatus.OK);
    // }
    //
    @Override
    public Resource<Object> toResource(Object pElement, Object... pExtras) {
        // TODO add hateoas links
        return resourceService.toResource(pElement);
    }
}
