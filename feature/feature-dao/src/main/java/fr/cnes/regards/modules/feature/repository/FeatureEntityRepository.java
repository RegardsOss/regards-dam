package fr.cnes.regards.modules.feature.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.cnes.regards.modules.feature.domain.FeatureEntity;

public interface FeatureEntityRepository extends JpaRepository<FeatureEntity, Long> {

}
