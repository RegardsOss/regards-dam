/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataset.client;

import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import fr.cnes.regards.client.core.annotation.RestClient;
import fr.cnes.regards.modules.dataset.domain.Greeting;

/**
 *
 * TODO Description
 *
 * @author TODO
 *
 */
@RestClient(name = "MyMicroServiceName") // TODO: change name
@RequestMapping(value = "/api", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public interface IGreetingsClient {

    /**
     * Rest resource /api/greeting/{name} Method GET
     *
     * @param name
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/greeting")
    @ResponseBody
    public HttpEntity<Resource<Greeting>> greeting(String pName);

    /**
     * Rest resource /api/me/{name} Method GET
     *
     * @param name
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/me")
    @ResponseBody
    public HttpEntity<Resource<Greeting>> me(String pName);

}
