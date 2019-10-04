/**
 *
 */
package fr.cnes.regards.modules.feature.service.request;

import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;

/**
 *
 * @author kevin
 *
 */
@Service
@MultitenantTransactional
public class FeatureRequestService implements IFeatureRequestService {

}
