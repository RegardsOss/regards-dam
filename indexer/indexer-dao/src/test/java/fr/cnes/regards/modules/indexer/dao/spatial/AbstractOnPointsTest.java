/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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

import fr.cnes.regards.modules.indexer.domain.IIndexable;

/**
 * @author oroussel
 */
public class AbstractOnPointsTest {
    protected static final String TYPE = "geo";

    protected enum GeometryType {
        Point
    }

    protected static class Item implements IIndexable {

        private String id;

        private String type = TYPE;

        private Geometry wgs84;

        public Item(String id, Geometry wgs84) {
            this.id = id;
            this.wgs84 = wgs84;
        }

        @Override
        public String getDocId() {
            return id;
        }

        @Override
        public String getType() {
            return TYPE;
        }

        public Geometry getWgs84() {
            return wgs84;
        }

        public void setWgs84(Geometry wgs84) {
            this.wgs84 = wgs84;
        }

        @Override
        public String getLabel() {
            return id;
        }
    }

    protected static class Feature {
        private Geometry geometry;

        public Geometry getGeometry() {
            return geometry;
        }

        public void setGeometry(Geometry geometry) {
            this.geometry = geometry;
        }
    }

    protected static class Point extends Geometry {

        public Point() {
            super(GeometryType.Point, null);
        }

        public Point(double... coordinates) {
            super(GeometryType.Point, coordinates);
        }
    }

    protected static class PointItem extends Item {

        public PointItem(String id, double... coordinates) {
            super(id, new Point(coordinates));
        }
    }

    protected static class Geometry<T> {

        private GeometryType type;

        private T coordinates;

        private String crs;

        public Geometry() {

        }

        public Geometry(GeometryType type, T coordinates) {
            this.type = type;
            this.coordinates = coordinates;
        }

        public GeometryType getType() {
            return type;
        }

        public void setType(GeometryType type) {
            this.type = type;
        }

        public T getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(T coordinates) {
            this.coordinates = coordinates;
        }

        public String getCrs() {
            return crs;
        }

        public void setCrs(String crs) {
            this.crs = crs;
        }
    }


}
