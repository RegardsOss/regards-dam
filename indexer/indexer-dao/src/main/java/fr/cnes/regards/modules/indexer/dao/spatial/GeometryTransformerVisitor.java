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

import java.util.stream.Collectors;

import fr.cnes.regards.framework.geojson.geometry.AbstractGeometry;
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
import fr.cnes.regards.modules.indexer.domain.spatial.Crs;

/**
 * IGeometryVisitor permitting to transform a Geometry from one Crs to another
 * @author oroussel
 */
public class GeometryTransformerVisitor implements IGeometryVisitor<IGeometry> {

    private final Crs fromCrs;

    private final Crs toCrs;

    public GeometryTransformerVisitor(Crs fromCrs, Crs toCrs) {
        this.fromCrs = fromCrs;
        this.toCrs = toCrs;
    }

    @Override
    public GeometryCollection visitGeometryCollection(GeometryCollection geometry) {
        GeometryCollection geomColl = new GeometryCollection();
        geomColl.setGeometries(geomColl.getGeometries().stream().map(g -> (AbstractGeometry<?>) g.accept(this))
                .collect(Collectors.toList()));
        return geomColl;
    }

    @Override
    public LineString visitLineString(LineString geometry) {
        return LineString.fromArray(GeoHelper.transform(geometry.toArray(), fromCrs, toCrs));
    }

    @Override
    public MultiLineString visitMultiLineString(MultiLineString geometry) {
        return MultiLineString.fromArray(GeoHelper.transform(geometry.toArray(), fromCrs, toCrs));
    }

    @Override
    public MultiPoint visitMultiPoint(MultiPoint geometry) {
        return MultiPoint.fromArray(GeoHelper.transform(geometry.toArray(), fromCrs, toCrs));
    }

    @Override
    public MultiPolygon visitMultiPolygon(MultiPolygon geometry) {
        double[][][][] fromArray = geometry.toArray();
        double[][][][] toArray = new double[fromArray.length][][][];
        for (int i = 0; i < toArray.length; i++) {
            toArray[i] = GeoHelper.transform(fromArray[i], fromCrs, toCrs);
        }
        return MultiPolygon.fromArray(toArray);
    }

    @Override
    public Point visitPoint(Point geometry) {
        return Point.fromArray(GeoHelper.transform(geometry.toArray(), fromCrs, toCrs));
    }

    @Override
    public Polygon visitPolygon(Polygon geometry) {
        return Polygon.fromArray(GeoHelper.transform(geometry.toArray(), fromCrs, toCrs));
    }

    @Override
    public Unlocated visitUnlocated(Unlocated geometry) {
        return IGeometry.unlocated();
    }
}
