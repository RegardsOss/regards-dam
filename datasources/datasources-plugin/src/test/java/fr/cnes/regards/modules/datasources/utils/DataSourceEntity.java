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
import java.net.URL;
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
    
    private String dateStr;

    private URL url;

    @Convert(converter = OffsetDateTimeAttributeConverter.class)
    private OffsetDateTime timeStampWithTimeZone; // Types.TIMESTAMP or Types.TIMESTAMP_WITH_TIMEZONE > JDBC 4.2

    private Boolean update;

    public DataSourceEntity() {
    }

    public DataSourceEntity(String label, int altitude, double latitude, double longitude, LocalDate date,
            LocalTime timeWithoutTimeZone, LocalDateTime timeStampWithoutTimeZone,
            OffsetDateTime timeStampWithTimeZone, String dateStr, Boolean update, URL url) {
        super();
        this.label = label;
        this.altitude = altitude;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
        this.timeWithoutTimeZone = timeWithoutTimeZone;
        this.timeStampWithoutTimeZone = timeStampWithoutTimeZone;
        this.timeStampWithTimeZone = timeStampWithTimeZone;
        this.dateStr = dateStr;
        this.update = update;
        this.url = url;
    }

    @Override
    public Long getId() {
        return id;
    }

}
