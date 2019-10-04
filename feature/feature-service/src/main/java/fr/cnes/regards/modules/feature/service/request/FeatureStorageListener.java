/**
 *
 */
package fr.cnes.regards.modules.feature.service.request;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.cnes.regards.modules.storagelight.client.IStorageRequestListener;
import fr.cnes.regards.modules.storagelight.client.RequestInfo;
import fr.cnes.regards.modules.storagelight.domain.dto.request.RequestResultInfoDTO;

/**
 *
 * This class offers callbacks from storage events
 *
 * @author kevin
 *
 */
@Component
public class FeatureStorageListener implements IStorageRequestListener {

	@Autowired
	private IFeatureRequestService featureRequestService;

	@Override
	public void onRequestGranted(RequestInfo request) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRequestDenied(RequestInfo request) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCopySuccess(RequestInfo request, Collection<RequestResultInfoDTO> success) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCopyError(RequestInfo request, Collection<RequestResultInfoDTO> success,
			Collection<RequestResultInfoDTO> errors) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAvailable(RequestInfo request, Collection<RequestResultInfoDTO> success) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAvailabilityError(RequestInfo request, Collection<RequestResultInfoDTO> success,
			Collection<RequestResultInfoDTO> errors) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDeletionSuccess(RequestInfo request, Collection<RequestResultInfoDTO> success) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDeletionError(RequestInfo request, Collection<RequestResultInfoDTO> success,
			Collection<RequestResultInfoDTO> errors) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onReferenceSuccess(RequestInfo request, Collection<RequestResultInfoDTO> success) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onReferenceError(RequestInfo request, Collection<RequestResultInfoDTO> success,
			Collection<RequestResultInfoDTO> errors) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStoreSuccess(RequestInfo request, Collection<RequestResultInfoDTO> success) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStoreError(RequestInfo request, Collection<RequestResultInfoDTO> success,
			Collection<RequestResultInfoDTO> errors) {
		// TODO Auto-generated method stub

	}

}
