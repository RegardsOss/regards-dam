package fr.cnes.regards.modules.opensearch.service;

import fr.cnes.regards.modules.models.client.IModelAttrAssocClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

/**
 * @author lmieulet
 */
@Configuration
@EnableFeignClients(clients = IModelAttrAssocClient.class)
public class OpenSearchConfiguration {
}
