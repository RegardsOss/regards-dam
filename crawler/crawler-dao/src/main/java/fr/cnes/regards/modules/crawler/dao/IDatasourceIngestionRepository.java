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

    /**
     * Find a DatasourceIngestion (any of them) whom next planned ingest date is less than given date
     * and with given status (usually 'STARTED')
     * @return a DatasourceIngestion or nothing
     */
    Optional<DatasourceIngestion> findTopByNextPlannedIngestDateLessThanAndStatusNot(LocalDateTime limitDate,
            IngestionStatus status);
}
