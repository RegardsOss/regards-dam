/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.client;

import java.util.List;

import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.ResponseBody;

import feign.RequestLine;
import fr.cnes.regards.client.core.ClientFactory;
import fr.cnes.regards.modules.collections.domain.Greeting;

/**
 * 
 * TODO Description
 * @author TODO
 *
 */
public interface IGreetingsClient {
    
    static IGreetingsClient getClient(String pUrl, String pJwtToken) {
        return ClientFactory.build(IGreetingsClient.class, new GreetingsClientFallback(), pUrl, pJwtToken);
    }

    /**
     * Rest resource /api/greeting/{name} Method GET
     *
     * @param name
     * @return
     */
    @RequestLine("GET /api/greeting")
    Greeting greeting(String pName);

    /**
     * Rest resource /api/me/{name} Method GET
     *
     * @param name
     * @return
     */
    @RequestLine("GET /api/me")
    Greeting me(String pName);

}
