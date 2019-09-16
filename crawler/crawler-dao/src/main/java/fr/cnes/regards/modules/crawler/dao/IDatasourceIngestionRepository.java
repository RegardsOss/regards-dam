package fr.cnes.regards.modules.crawler.dao;

import java.time.OffsetDateTime;
import java.util.Optional;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import fr.cnes.regards.modules.crawler.domain.DatasourceIngestion;
import fr.cnes.regards.modules.crawler.domain.IngestionStatus;

/**
 * @author oroussel
 */
public interface IDatasourceIngestionRepository extends JpaRepository<DatasourceIngestion, String> {

    /**
     * Find a DatasourceIngestion (any of them) whom next planned ingest date is less than given date
     * and with given status (usually 'STARTED')
     * @return a DatasourceIngestion or nothing
     */
    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<DatasourceIngestion> findTopByNextPlannedIngestDateLessThanAndStatusNot(OffsetDateTime limitDate,
            IngestionStatus status);

    /**
     * Find next DatasourceIngestion ready to be ingested
     */
    default Optional<DatasourceIngestion> findNextReady(OffsetDateTime limitDate) {
        return findTopByNextPlannedIngestDateLessThanAndStatusNot(limitDate, IngestionStatus.STARTED);
    }
}
