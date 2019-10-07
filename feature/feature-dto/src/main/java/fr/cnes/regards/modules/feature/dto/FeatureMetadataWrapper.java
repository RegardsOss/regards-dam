/**
 *
 */
package fr.cnes.regards.modules.feature.dto;

import java.util.List;

/**
 * @author kevin
 *
 */
public class FeatureMetadataWrapper {

	private Feature feature;

	private List<FeatureMetadataDto> metada;

	public Feature getFeature() {
		return feature;
	}

	public void setFeature(Feature feature) {
		this.feature = feature;
	}

	public List<FeatureMetadataDto> getMetada() {
		return metada;
	}

	public void setMetada(List<FeatureMetadataDto> metada) {
		this.metada = metada;
	}

	public static FeatureMetadataWrapper builder(Feature feature, List<FeatureMetadataDto> metada) {
		FeatureMetadataWrapper fmw = new FeatureMetadataWrapper();
		fmw.setFeature(feature);
		fmw.setMetada(metada);

		return fmw;
	}
}
