package fr.cnes.regards.modules.feature.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.cnes.regards.modules.feature.domain.request.FeatureCreationRequest;

public interface FeatureCreationRequestRepository extends JpaRepository<FeatureCreationRequest, Long>{

	public void deleteByIdIn(List<Long> ids);
}
