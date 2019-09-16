package fr.cnes.regards.modules.crawler.domain;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import fr.cnes.regards.framework.jpa.converters.OffsetDateTimeAttributeConverter;

/**
 * Datasource ingestion entity. Indicates the status of the last and current datsource ingestion.
 * @author oroussel
 */
@Entity
@Table(name = "t_datasource_ingestion")
public class DatasourceIngestion {

    /**
     * Id is datasource id (see table pluginConf businnessId)
     */
    @Id
    @Column(name = "ds_id", length = 36)
    private String id;

    @Column(name = "label", length = 255)
    private String label;

    /**
     * Date of last ingestion (null if none yet)
     */
    @Column(name = "last_ingest_date")
    @Convert(converter = OffsetDateTimeAttributeConverter.class)
    private OffsetDateTime lastIngestDate;

    /**
     * Date of next planned ingest date (= last ingest date + refresh rate of datasource plugin)
     */
    @Column(name = "next_planned_ingest_date")
    @Convert(converter = OffsetDateTimeAttributeConverter.class)
    private OffsetDateTime nextPlannedIngestDate;

    /**
     * Duration from Status.STARTED to Status.FINISHED with second representation format (for example PT8H6M12.345S --
     * "8 hours 6 mn 12.345 s"
     */
    @Column(name = "duration", length = 20)
    private String duration = null;

    /**
     * Status of previous or current ingestion
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private IngestionStatus status = IngestionStatus.NEW;

    /**
     * Date of status change (default to object creation with NEW status if nothing provided)
     */
    @Column(name = "status_date")
    @Convert(converter = OffsetDateTimeAttributeConverter.class)
    private OffsetDateTime statusDate = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);

    /**
     * Last ingestion saved objects count
     */
    @Column(name = "saved_objects_count")
    private Integer savedObjectsCount;

    /**
     * Last ingestion number of objects that couldn't be saved
     */
    @Column(name = "error_objects_count")
    private Integer inErrorObjectsCount;

    /**
     * Last NOT_FINISHED ingestion error page number
     */
    @Column(name = "error_page_nb")
    private Integer errorPageNumber;

    /**
     * When status is ERROR, the exception stack trace
     */
    @Column
    @Type(type = "text")
    private String stackTrace;

    @SuppressWarnings("unused")
    private DatasourceIngestion() {
    }

    public DatasourceIngestion(String id) {
        super();
        this.id = id;
    }

    public DatasourceIngestion(String id, OffsetDateTime nextPlannedIngestDate, String label) {
        this(id);
        this.nextPlannedIngestDate = nextPlannedIngestDate;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public OffsetDateTime getLastIngestDate() {
        return lastIngestDate;
    }

    public void setLastIngestDate(OffsetDateTime lastIngestDate) {
        this.lastIngestDate = lastIngestDate;
    }

    public IngestionStatus getStatus() {
        return status;
    }

    public void setStatus(IngestionStatus status) {
        OffsetDateTime now = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
        if ((status == IngestionStatus.FINISHED) && (statusDate != null)) {
            duration = Duration.between(statusDate, now).toString();
        } else {
            duration = null;
        }
        this.status = status;
        statusDate = now;
    }

    public OffsetDateTime getStatusDate() {
        return statusDate;
    }

    public void setStatusDate(OffsetDateTime statusDate) {
        this.statusDate = statusDate;
    }

    public OffsetDateTime getNextPlannedIngestDate() {
        return nextPlannedIngestDate;
    }

    public void setNextPlannedIngestDate(OffsetDateTime nextPlannedIngestDate) {
        this.nextPlannedIngestDate = nextPlannedIngestDate;
    }

    public Integer getSavedObjectsCount() {
        return savedObjectsCount;
    }

    public void setSavedObjectsCount(Integer savedObjectsCount) {
        this.savedObjectsCount = savedObjectsCount;
    }

    public Integer getInErrorObjectsCount() {
        return inErrorObjectsCount;
    }

    public void setInErrorObjectsCount(Integer inErrorObjectsCount) {
        this.inErrorObjectsCount = inErrorObjectsCount;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getErrorPageNumber() {
        return errorPageNumber;
    }

    public void setErrorPageNumber(Integer errorPageNumber) {
        this.errorPageNumber = errorPageNumber;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DatasourceIngestion other = (DatasourceIngestion) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DatasourceIngestion [id=" + id + ", lastIngestDate=" + lastIngestDate + ", status=" + status
                + ", statusDate=" + statusDate + "]";
    }

}
