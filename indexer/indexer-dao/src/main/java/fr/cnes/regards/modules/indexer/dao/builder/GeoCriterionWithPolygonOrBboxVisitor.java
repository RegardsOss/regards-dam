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

import com.google.common.base.Preconditions;

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
 * ones). The only geometric criterion found is a PolygonCriterion one.
 * All polygon criterions must be modified with projected coordinates.
 * @author oroussel
 */
public class GeoCriterionWithPolygonOrBboxVisitor implements ICriterionVisitor<ICriterion> {

    /**
     * CRS concerned by data to be tested through criterion
     */
    private Crs crs;

    /**
     * @param crs coordinate reference system used by geometries
     */
    public GeoCriterionWithPolygonOrBboxVisitor(Crs crs) {
        Preconditions.checkNotNull(crs);
        if (crs == Crs.WGS_84) {
            throw new IllegalArgumentException("GeoCriterionWithPolygonOrBboxVisitor cannot be used with WGS84 crs");
        }
        this.crs = crs;
    }

    @Override
    public ICriterion visitEmptyCriterion(EmptyCriterion criterion) {
        return criterion.copy();
    }

    @Override
    public ICriterion visitAndCriterion(AbstractMultiCriterion criterion) {
        return criterion.copy();
    }

    @Override
    public ICriterion visitOrCriterion(AbstractMultiCriterion criterion) {
        return criterion.copy();
    }

    @Override
    public ICriterion visitNotCriterion(NotCriterion criterion) {
        return criterion.copy();
    }

    @Override
    public ICriterion visitStringMatchCriterion(StringMatchCriterion criterion) {
        return criterion.copy();
    }

    @Override
    public ICriterion visitStringMultiMatchCriterion(StringMultiMatchCriterion criterion) {
        return criterion.copy();
    }

    @Override
    public ICriterion visitStringMatchAnyCriterion(StringMatchAnyCriterion criterion) {
        return criterion.copy();
    }

    @Override
    public ICriterion visitIntMatchCriterion(IntMatchCriterion criterion) {
        return criterion.copy();
    }

    @Override
    public ICriterion visitLongMatchCriterion(LongMatchCriterion criterion) {
        return criterion.copy();
    }

    @Override
    public ICriterion visitDateMatchCriterion(DateMatchCriterion criterion) {
        return criterion.copy();
    }

    @Override
    public <U extends Comparable<? super U>> ICriterion visitRangeCriterion(RangeCriterion<U> criterion) {
        return criterion.copy();
    }

    @Override
    public ICriterion visitDateRangeCriterion(DateRangeCriterion criterion) {
        return criterion.copy();
    }

    @Override
    public ICriterion visitBooleanMatchCriterion(BooleanMatchCriterion criterion) {
        return criterion.copy();
    }

    @Override
    public ICriterion visitPolygonCriterion(PolygonCriterion criterion) {
        return ICriterion.intersectsPolygon(GeoHelper.transform(criterion.getCoordinates(), crs, Crs.WGS_84));
    }

    @Override
    public ICriterion visitBoundaryBoxCriterion(BoundaryBoxCriterion criterion) {
        double[][] fromBbox = new double[][] { { criterion.getMinX(), criterion.getMinY() },
                { criterion.getMaxX(), criterion.getMaxY() } };
        double[][] toBbox = GeoHelper.transform(fromBbox, crs, Crs.WGS_84);
        // DON'T TOUCH THE F$%CKING LONGITUDES !!! (180 -> -180 which is very annoying for a cap Bbox and
        // longitudes are not impacted by projection transformations)
        return ICriterion.intersectsBbox(fromBbox[0][0], toBbox[0][1], fromBbox[1][0], toBbox[1][1]);
    }

    @Override
    public ICriterion visitCircleCriterion(CircleCriterion criterion) {
        throw new IllegalArgumentException(
                "GeoCriterionWithPolygonOrBboxVisitor shouldn't visit a criterion tree with CircleCriterion");
    }

    @Override
    public ICriterion visitFieldExistsCriterion(FieldExistsCriterion criterion) {
        return criterion.copy();
    }
}
