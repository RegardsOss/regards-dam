/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.notifier.service;

import java.time.OffsetDateTime;

import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.model.dto.properties.IProperty;
import fr.cnes.regards.modules.model.dto.properties.ObjectProperty;

/**
 * @author Marc SORDI
 *
 */
public final class GeodeProperties {

    public static String getGeodeModel() {
        return "model_geode.xml";
    }

    public static void addGeodeProperties(Feature feature) {
        // System
        ObjectProperty system = IProperty.buildObject("system", IProperty.buildInteger("filesize", 8648),
                                                      IProperty.buildDate("creation_date", OffsetDateTime.now()),
                                                      IProperty.buildDate("modification_date", OffsetDateTime.now()),
                                                      IProperty.buildStringArray("urls", "file://home/geode/test.tar"),
                                                      IProperty.buildString("filename", "test.tar"),
                                                      IProperty.buildString("checksum",
                                                                            "4e188bd8a6288164c25c3728ce394927"),
                                                      IProperty.buildString("extension", "tar"));
        // File infos
        ObjectProperty fileInfos = IProperty.buildObject("file_infos", IProperty.buildString("type", "L0A_LR_Packet"),
                                                         IProperty.buildString("nature", "TM"),
                                                         IProperty.buildString("date_type", "BEGINEND"),
                                                         IProperty.buildString("level", "L0A"),
                                                         IProperty.buildDate("production_date", OffsetDateTime.now()),
                                                         IProperty.buildDate("utc_start_date", OffsetDateTime.now()),
                                                         IProperty.buildDate("utc_end_date", OffsetDateTime.now()),
                                                         IProperty.buildDate("tai_start_date", OffsetDateTime.now()),
                                                         IProperty.buildDate("tai_end_date", OffsetDateTime.now()),
                                                         IProperty.buildBoolean("valid", true));
        // Ground segment
        ObjectProperty groundSegment = IProperty
                .buildObject("ground_segment", IProperty.buildBoolean("sended", true),
                             IProperty.buildDate("sending_date", OffsetDateTime.now()),
                             IProperty.buildStringArray("recipients", "JPL", "REGARDS"),
                             IProperty.buildBoolean("archived", true),
                             IProperty.buildDate("archiving_date", OffsetDateTime.now()),
                             IProperty.buildBoolean("public", false), IProperty.buildBoolean("distributed", false),
                             IProperty.buildBoolean("restored", false), IProperty.buildString("state", "NOT ARCHIVED"));

        // SWOT
        ObjectProperty swot = IProperty
                .buildObject("swot", IProperty.buildString("CRID", "crid"),
                             IProperty.buildInteger("product_counter", 1),
                             IProperty.buildBoolean("is_last_version", true), IProperty.buildString("station", "KUX"),
                             IProperty.buildDate("day_date", OffsetDateTime.now()), IProperty.buildInteger("cycle", 23),
                             IProperty.buildInteger("pass", 125), IProperty.buildInteger("tile", 25),
                             IProperty.buildString("tile_side", "Full"), IProperty.buildString("granule_type", "Cycle"),
                             IProperty.buildStringArray("continent_id", "eu"),
                             IProperty.buildString("bassin_id", "bass1"));
        // CORPUS
        ObjectProperty corpus = IProperty.buildObject("corpus", IProperty.buildInteger("corpus_id", 10),
                                                      IProperty.buildString("corpus_lot", "lot2"));

        feature.setProperties(IProperty.set(system, fileInfos, groundSegment, swot, corpus));
    }

    public static void addGeodeUpdateProperties(Feature feature) {
        // Ground segment
        ObjectProperty groundSegment = IProperty
                .buildObject("ground_segment", IProperty.buildBoolean("distributed", true),
                             IProperty.buildDate("distribution_date", OffsetDateTime.now()));

        feature.setProperties(IProperty.set(groundSegment));
    }
}
