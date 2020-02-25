/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.indexer.dao.builder;

import java.util.ArrayList;
import java.util.List;

import org.hipparchus.util.FastMath;
import org.opengis.referencing.operation.TransformException;
import org.springframework.data.util.Pair;

import com.google.common.base.Preconditions;

import fr.cnes.regards.framework.utils.RsRuntimeException;
import fr.cnes.regards.modules.indexer.dao.EsHelper;
import fr.cnes.regards.modules.indexer.dao.spatial.GeoHelper;
import fr.cnes.regards.modules.indexer.domain.criterion.AbstractMultiCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.BooleanMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.BoundaryBoxCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.CircleCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.DateMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.DateRangeCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.EmptyCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.FieldExistsCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterionVisitor;
import fr.cnes.regards.modules.indexer.domain.criterion.IntMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.LongMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.NotCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.PolygonCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.RangeCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.StringMatchAnyCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.StringMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.StringMultiMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.spatial.Crs;

/**
 * Criterion visitor implementation to manage a criterion containing geometric criterions on other CRS (as Mars or Astro
 * ones). The only geometric criterion found is a circleCriterion one.<br/>
 * All circle criterions must lead to two criterion trees :<br/>
 * - first one will take all points into an inner circle projection,<br/>
 * - second one will take only points between inner one and outer ones (and resulted points will be tested with
 * Geotools to know if they are taken or not).<br/>
 * Be careful: in case inner and outer circles are the same (circle center on equator or pole for example), both
 * resulted criterions tree are the same (first one).<br/>
 * @author oroussel
 */
public class GeoCriterionWithCircleVisitor implements ICriterionVisitor<Pair<ICriterion, ICriterion>> {

    /**
     * CRS concerned by data to be tested through criterion
     */
    private final Crs crs;

    /**
     * @param crs coordinate reference system used by geometries
     */
    public GeoCriterionWithCircleVisitor(Crs crs) {
        Preconditions.checkNotNull(crs);
        if (crs == Crs.WGS_84) {
            throw new IllegalArgumentException("GeoCriterionWithCircleVisitor cannot be used with WGS84 crs");
        }
        this.crs = crs;
    }

    @Override
    public Pair<ICriterion, ICriterion> visitEmptyCriterion(EmptyCriterion criterion) {
        return Pair.of(ICriterion.all(), ICriterion.all());
    }

    @Override
    public Pair<ICriterion, ICriterion> visitAndCriterion(AbstractMultiCriterion criterion) {
        List<ICriterion> childrenForFirst = new ArrayList<>(criterion.getCriterions().size());
        List<ICriterion> childrenForSecond = new ArrayList<>(criterion.getCriterions().size());
        for (ICriterion crit : criterion.getCriterions()) {
            Pair<ICriterion, ICriterion> pair = crit.accept(this);
            childrenForFirst.add(pair.getFirst());
            childrenForSecond.add(pair.getSecond());
        }
        return Pair.of(ICriterion.and(childrenForFirst), ICriterion.and(childrenForSecond));
    }

    @Override
    public Pair<ICriterion, ICriterion> visitOrCriterion(AbstractMultiCriterion criterion) {
        List<ICriterion> childrenForFirst = new ArrayList<>(criterion.getCriterions().size());
        List<ICriterion> childrenForSecond = new ArrayList<>(criterion.getCriterions().size());
        for (ICriterion crit : criterion.getCriterions()) {
            Pair<ICriterion, ICriterion> pair = crit.accept(this);
            childrenForFirst.add(pair.getFirst());
            childrenForSecond.add(pair.getSecond());
        }
        return Pair.of(ICriterion.or(childrenForFirst), ICriterion.or(childrenForSecond));
    }

    @Override
    public Pair<ICriterion, ICriterion> visitNotCriterion(NotCriterion criterion) {
        Pair<ICriterion, ICriterion> pair = criterion.accept(this);
        return Pair.of(ICriterion.not(pair.getFirst()), ICriterion.not(pair.getSecond()));
    }

    @Override
    public Pair<ICriterion, ICriterion> visitStringMatchCriterion(StringMatchCriterion criterion) {
        return Pair.of(criterion.copy(), criterion.copy());
    }

    @Override
    public Pair<ICriterion, ICriterion> visitStringMultiMatchCriterion(StringMultiMatchCriterion criterion) {
        return Pair.of(criterion.copy(), criterion.copy());
    }

    @Override
    public Pair<ICriterion, ICriterion> visitStringMatchAnyCriterion(StringMatchAnyCriterion criterion) {
        return Pair.of(criterion.copy(), criterion.copy());
    }

    @Override
    public Pair<ICriterion, ICriterion> visitIntMatchCriterion(IntMatchCriterion criterion) {
        return Pair.of(criterion.copy(), criterion.copy());
    }

    @Override
    public Pair<ICriterion, ICriterion> visitLongMatchCriterion(LongMatchCriterion criterion) {
        return Pair.of(criterion.copy(), criterion.copy());
    }

    @Override
    public Pair<ICriterion, ICriterion> visitDateMatchCriterion(DateMatchCriterion criterion) {
        return Pair.of(criterion.copy(), criterion.copy());
    }

    @Override
    public <U extends Comparable<? super U>> Pair<ICriterion, ICriterion> visitRangeCriterion(
            RangeCriterion<U> criterion) {
        return Pair.of(criterion.copy(), criterion.copy());
    }

    @Override
    public Pair<ICriterion, ICriterion> visitDateRangeCriterion(DateRangeCriterion criterion) {
        return Pair.of(criterion.copy(), criterion.copy());
    }

    @Override
    public Pair<ICriterion, ICriterion> visitBooleanMatchCriterion(BooleanMatchCriterion criterion) {
        return Pair.of(criterion.copy(), criterion.copy());
    }

    @Override
    public Pair<ICriterion, ICriterion> visitPolygonCriterion(PolygonCriterion criterion) {
        throw new IllegalArgumentException(
                "GeoCriterionWithCircleVisitor shouldn't visit a criterion tree with PolygonCriterion");
    }

    @Override
    public Pair<ICriterion, ICriterion> visitBoundaryBoxCriterion(BoundaryBoxCriterion criterion) {
        throw new IllegalArgumentException(
                "GeoCriterionWithCircleVisitor shouldn't visit a criterion tree with BoundaryBoxCriterion");
    }

    @Override
    public Pair<ICriterion, ICriterion> visitCircleCriterion(CircleCriterion criterion) {
        try {
            // User has created a circle criterion into associated crs.
            // Get the center
            double[] centerOnCrs = criterion.getCoordinates();
            // Get the northernmost point at given radius
            double[] northernmostPointOnCrs = GeoHelper
                    .getPointAtDirection(centerOnCrs, 0.0, EsHelper.toMeters(criterion.getRadius()), crs);
            // Get the southernmostPoint at given radius
            double[] southernmostPointOnCrs = GeoHelper
                    .getPointAtDirection(centerOnCrs, 180.0, EsHelper.toMeters(criterion.getRadius()), crs);
            // Get center on WGS84 projection
            double[] centerOnWgs84 = GeoHelper.transform(centerOnCrs, crs, Crs.WGS_84);
            // Get northermost point on WGS84 projection
            double[] northernmostPointOnWgs84 = GeoHelper.transform(northernmostPointOnCrs, crs, Crs.WGS_84);
            // Get southermost point on WGS84 projection
            double[] southernmostPointOnWgs84 = GeoHelper.transform(southernmostPointOnCrs, crs, Crs.WGS_84);
            // Compute distance from center and northernmost point on WGS84
            double distanceNorthermostCenter = GeoHelper.getDistanceOnEarth(centerOnWgs84, northernmostPointOnWgs84);
            // Compute distance from center and southernmost point on WGS84
            double distanceSouthermostCenter = GeoHelper.getDistanceOnEarth(centerOnWgs84, southernmostPointOnWgs84);
            // Return pair with circle criterion on min distance...
            double minDistanceAsDouble = FastMath.min(distanceNorthermostCenter, distanceSouthermostCenter);
            String minDistance = Double.toString(minDistanceAsDouble);
            ICriterion inMinCircleCrit = ICriterion.intersectsCircle(centerOnWgs84, minDistance);
            // ...and (not circle on min distance and circle criterion on max distance) => between both circles
            double maxDistanceAsDouble = FastMath.max(distanceNorthermostCenter, distanceSouthermostCenter);
            // TO BE INVESTIGATE
            // Because of error precision between Elasticsearch and Postgis, increase a little maxDistance
            //            maxDistanceAsDouble *= 1.1;
            String maxDistance = Double.toString(maxDistanceAsDouble);
            // Be careful : in some cases (center on equator or on a pole), max and min distances are equal.
            // In this case, we return same CircleCriterion to make both criterion trees equals and so avoiding
            // executing two requests
            // Because of maxDistance increase due to error precision, this case should never occur
            ICriterion betweenBothCirclesCriterion = minDistance.equals(maxDistance) ? inMinCircleCrit
                    : ICriterion.and(ICriterion.not(inMinCircleCrit),
                                     ICriterion.intersectsCircle(centerOnWgs84, maxDistance));
            return Pair.of(inMinCircleCrit, betweenBothCirclesCriterion);
        } catch (TransformException e) {
            throw new RsRuntimeException(e);
        }
    }

    @Override
    public Pair<ICriterion, ICriterion> visitFieldExistsCriterion(FieldExistsCriterion criterion) {
        return Pair.of(criterion.copy(), criterion.copy());
    }
}
