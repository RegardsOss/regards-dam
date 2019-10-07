package fr.cnes.regards.modules.feature.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.cnes.regards.modules.feature.domain.request.FeatureCreationRequest;

public interface FeatureCreationRequestRepository extends JpaRepository<FeatureCreationRequest, Long> {

	public void deleteByIdIn(List<Long> ids);

	public Set<FeatureCreationRequest> findByGroupId(String groupId);

}
