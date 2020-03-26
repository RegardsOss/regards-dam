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
package fr.cnes.regards.modules.indexer.service;

import org.opengis.referencing.operation.TransformException;

import fr.cnes.regards.framework.geojson.coordinates.Position;
import fr.cnes.regards.framework.geojson.coordinates.Positions;
import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.geojson.geometry.Point;
import fr.cnes.regards.framework.geojson.geometry.Polygon;
import fr.cnes.regards.modules.indexer.dao.spatial.GeoHelper;
import fr.cnes.regards.modules.indexer.domain.spatial.Crs;

/**
 * @author oroussel
 */
public class GeoUtil {
    public static <T extends IGeometry> T toWgs84(T geom) throws TransformException {
        if (geom instanceof Point) {
            return (T)toWgs84((Point)geom);
        } else if (geom instanceof Polygon) {
            return (T)toWgs84((Polygon)geom);
        }
        return null;
    }

    private static Point toWgs84(Point pointOnMars) throws TransformException {
        double[] lonLat = GeoHelper.transform(new double[] { pointOnMars.getCoordinates().getLongitude(),
                pointOnMars.getCoordinates().getLatitude() }, pointOnMars.getCrs().isPresent() ?
                                                      Crs.valueOf(pointOnMars.getCrs().get()) :
                                                      Crs.WGS_84, Crs.WGS_84);
        return IGeometry.point(lonLat[0], lonLat[1]);
    }

    private static Polygon toWgs84(Polygon polyOnMars) throws TransformException {
        Positions positionsOnMars = polyOnMars.getCoordinates().getExteriorRing();
        Positions positions = new Positions();
        for (Position pos : positionsOnMars) {
            double[] point = GeoHelper
                    .transform(new double[] { pos.getLongitude(), pos.getLatitude() }, Crs.MARS_49900, Crs.WGS_84);
            positions.add(new Position(point[0], point[1]));
        }
        return IGeometry.polygon(IGeometry.toPolygonCoordinates(positions));
    }

    /**
     * Retrieve polygon edge and return it as a simple double double array
     */
    public static double[][] toArray(Polygon polygon) {
        return polygon.getCoordinates().get(0).stream().map(p -> new double[] { p.getLongitude(), p.getLatitude() }).toArray(double[][]::new);
    }
}
