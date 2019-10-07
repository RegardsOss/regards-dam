/**
 *
 */
package fr.cnes.regards.modules.feature.dto.event.out;

/**
 * @author kevin
 *
 */
public enum FeatureState {

	/**
	 * A request has been send to storage with the feature
	 */
	STORAGE_REQUESTED,
	/**
	 * The feature has been stored
	 */
	STORAGE_OK,
	/**
	 * An error has occured during the storage of the feature
	 */
	STORAGE_ERROR
}
