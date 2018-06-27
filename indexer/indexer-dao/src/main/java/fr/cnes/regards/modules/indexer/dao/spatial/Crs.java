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
package fr.cnes.regards.modules.indexer.dao.spatial;

/**
 * Some Coordinat references systems
 * @author oroussel
 */
public enum Crs {
    WGS_84("GEOGCS[\"WGS 84\",\n" + "    DATUM[\"WGS_1984\",\n" + "        SPHEROID[\"WGS 84\",6378137,298.257223563,\n"
                   + "            AUTHORITY[\"EPSG\",\"7030\"]],\n" + "        AUTHORITY[\"EPSG\",\"6326\"]],\n"
                   + "    PRIMEM[\"Greenwich\",0,\n" + "        AUTHORITY[\"EPSG\",\"8901\"]],\n"
                   + "    UNIT[\"degree\",0.01745329251994328,\n" + "        AUTHORITY[\"EPSG\",\"9122\"]],\n"
                   + "    AUTHORITY[\"EPSG\",\"4326\"]]"),
    MARS_49900("GEOGCS[\"Mars 2000\",\n" + "    DATUM[\"D_Mars_2000\",\n"
                       + "        SPHEROID[\"Mars_2000_IAU_IAG\",3396190.0,169.89444722361179]],\n"
                       + "    PRIMEM[\"Greenwich\",0],\n" + "    UNIT[\"Decimal_Degree\",0.0174532925199433]]");

    // Associated WKT
    private String wkt;

    Crs(String wkt) {
        this.wkt = wkt;
    }

    public String getWkt() {
        return wkt;
    }
}
