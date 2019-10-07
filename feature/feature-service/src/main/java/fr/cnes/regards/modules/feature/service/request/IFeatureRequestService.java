/**
 *
 */
package fr.cnes.regards.modules.feature.service.request;

import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.domain.request.FeatureCreationRequest;

/**
 * @author kevin
 *
 */
public interface IFeatureRequestService {

	/**
	 * Set the status STORAGE_OK to the {@link FeatureEntity} references by
	 * {@link FeatureCreationRequest} with the group id send in parameter
	 *
	 * @param groupId
	 */
	void handleSuccess(String groupId);

	/**
	 * Set the status STORAGE_ERROR to the {@link FeatureEntity} references by
	 * {@link FeatureCreationRequest} with the group id send in parameter
	 *
	 * @param groupId
	 */
	void handleError(String groupId);

}