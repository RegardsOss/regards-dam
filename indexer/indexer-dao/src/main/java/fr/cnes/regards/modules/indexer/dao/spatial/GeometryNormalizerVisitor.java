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

import fr.cnes.regards.framework.geojson.coordinates.PolygonPositions;
import fr.cnes.regards.framework.geojson.geometry.GeometryCollection;
import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.geojson.geometry.IGeometryVisitor;
import fr.cnes.regards.framework.geojson.geometry.LineString;
import fr.cnes.regards.framework.geojson.geometry.MultiLineString;
import fr.cnes.regards.framework.geojson.geometry.MultiPoint;
import fr.cnes.regards.framework.geojson.geometry.MultiPolygon;
import fr.cnes.regards.framework.geojson.geometry.Point;
import fr.cnes.regards.framework.geojson.geometry.Polygon;
import fr.cnes.regards.framework.geojson.geometry.Unlocated;

/**
 * IGeometryVisitor permitting to normalize a Geometry
 * @author oroussel
 */
public class GeometryNormalizerVisitor implements IGeometryVisitor<IGeometry> {

    @Override
    public GeometryCollection visitGeometryCollection(GeometryCollection geometry) {
        geometry.getGeometries().forEach(g -> g.accept(this));
        return geometry;
    }

    /**
     * Return type is IGeometry because in some case a LineString is normalized into MultiLineString
     */
    @Override
    public IGeometry visitLineString(LineString geometry) {
        return GeoHelper.normalizeLineString(geometry);
    }

    @Override
    public MultiLineString visitMultiLineString(MultiLineString geometry) {
        return GeoHelper.normalizeMultiLineString(geometry);
    }

    @Override
    public MultiPoint visitMultiPoint(MultiPoint geometry) {
        return geometry;
    }

    @Override
    public IGeometry visitMultiPolygon(MultiPolygon geometry) {
        MultiPolygon multiPolygon = GeoHelper.normalizeMultiPolygon(geometry);
        if (multiPolygon.getCoordinates().size() == 1) {
            PolygonPositions positions = multiPolygon.getCoordinates().get(0);
            return Polygon.fromArray(positions.toArray());
        }
        return multiPolygon;
    }

    @Override
    public Point visitPoint(Point geometry) {
        return geometry;
    }

    @Override
    public IGeometry visitPolygon(Polygon geometry) {
        MultiPolygon multiPolygon = GeoHelper.normalizePolygon(geometry);
        if (multiPolygon.getCoordinates().size() == 1) {
            PolygonPositions positions = multiPolygon.getCoordinates().get(0);
            return Polygon.fromArray(positions.toArray());
        }
        return multiPolygon;
    }

    @Override
    public Unlocated visitUnlocated(Unlocated geometry) {
        return geometry;
    }
}
