/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.datasources.utils;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;

import org.hibernate.validator.constraints.NotBlank;

import fr.cnes.regards.framework.jpa.IIdentifiable;
import fr.cnes.regards.framework.jpa.converters.OffsetDateTimeAttributeConverter;
import fr.cnes.regards.modules.datasources.plugins.PostgreDataSourcePlugin;

/**
 * A domain used to test the {@link PostgreDataSourcePlugin}
 *
 * @author Christophe Mertz
 */
@Entity
@Table(name = "t_test_plugin_data_source",
        indexes = { @Index(name = "index_test", columnList = "altitude", unique = true) })
@SequenceGenerator(name = "testPlgDataSourceSequence", initialValue = 1, sequenceName = "seq_test_plugin")
public class DataSourceEntity implements IIdentifiable<Long> {

    /**
     * DataSourceEntity identifier
     */
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "testPlgDataSourceSequence")
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

    @Convert(converter = OffsetDateTimeAttributeConverter.class)
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
