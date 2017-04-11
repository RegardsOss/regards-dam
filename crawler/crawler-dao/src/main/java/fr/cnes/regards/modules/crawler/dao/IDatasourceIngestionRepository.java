package fr.cnes.regards.modules.crawler.dao;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.cnes.regards.modules.crawler.domain.DatasourceIngestion;
import fr.cnes.regards.modules.crawler.domain.IngestionStatus;

/**
 * @author oroussel
 */
public interface IDatasourceIngestionRepository extends JpaRepository<DatasourceIngestion, Long> {

    //    @Query("from DatasourceIngestion where (nextPlannedIngestDate < :limitDate) and status <> 'STARTED' limit 1")
    //    Optional<DatasourceIngestion> findOneReadyToIngest(@Param("limitDate") LocalDateTime limitDate);

    Optional<DatasourceIngestion> findTopByNextPlannedIngestDateLessThanAndStatusNot(LocalDateTime limitDate,
            IngestionStatus status);
}
