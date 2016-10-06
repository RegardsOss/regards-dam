/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.client;

import org.springframework.cloud.netflix.feign.FeignClient;

import feign.Headers;
import fr.cnes.regards.modules.collections.fallback.CollectionsFallback;
import fr.cnes.regards.modules.collections.signature.CollectionsSignature;

/**
 * @author lmieulet
 *
 */
@FeignClient(value = "rs-dam", fallback = CollectionsFallback.class)
@Headers({ "Accept: application/json", "Content-Type: application/json" })
public interface CollectionsClient extends CollectionsSignature {

}
