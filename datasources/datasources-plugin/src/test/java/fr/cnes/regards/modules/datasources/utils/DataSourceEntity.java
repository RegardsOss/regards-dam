/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.datasources.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.validator.constraints.NotBlank;

import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.modules.datasources.plugins.PostgreDataSourcePlugin;

/**
 * A domain used to test the {@link PostgreDataSourcePlugin}
 *
 * @author Christophe Mertz
 */
@Entity
@Table(name = "T_TEST_PLUGIN_DATA_SOURCE",
        indexes = { @Index(name = "ndex_test", columnList = "altitude", unique = true) })
@SequenceGenerator(name = "testPlgDataSOurceSequence", initialValue = 1, sequenceName = "SEQ_TEST_PLUGIN")
public class DataSourceEntity implements IIdentifiable<Long> {

    /**
     * DataSourceEntity identifier
     */
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "testPlgDataSOurceSequence")
    private Long id;

    /**
     * DataSourceEntity label
     */
    @NotBlank
    @Column(unique = true)
    private String label;

    private Integer altitude;

    private Double latitude;

    private Double longitude;

    private LocalDate date; // Types.DATE

    private LocalTime timeWithoutTimeZone; // Types.TIME

    private LocalDateTime timeStampWithoutTimeZone; // Types.TIMESTAMP

    private OffsetDateTime timeStampWithTimeZone; // Types.TIMESTAMP or Types.TIMESTAMP_WITH_TIMEZONE > JDBC 4.2

    private Boolean update;

    public DataSourceEntity() {
    }

    public DataSourceEntity(String pLabel, int pAltitude, double pLatitude, double pLongitude, LocalDate pDate,
            LocalTime pTimeWithoutTimeZone, LocalDateTime pTimeStampWithoutTimeZone,
            OffsetDateTime pTimeStampWithTimeZone, Boolean pUpdate) {
        super();
        this.label = pLabel;
        this.altitude = pAltitude;
        this.latitude = pLatitude;
        this.longitude = pLongitude;
        this.date = pDate;
        this.timeWithoutTimeZone = pTimeWithoutTimeZone;
        this.timeStampWithoutTimeZone = pTimeStampWithoutTimeZone;
        this.timeStampWithTimeZone = pTimeStampWithTimeZone;
        this.update = pUpdate;
    }

    @Override
    public Long getId() {
        return id;
    }

}
