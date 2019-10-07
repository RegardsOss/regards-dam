package fr.cnes.regards.modules.feature.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.dto.event.out.FeatureState;

public interface FeatureEntityRepository extends JpaRepository<FeatureEntity, Long> {

	@Query("update FeatureEntity feature set feature.state = ?1 where feature.id in ?2")
	public void updateStateByRequestIdIn(FeatureState state, Set<Long> featureId);
}
