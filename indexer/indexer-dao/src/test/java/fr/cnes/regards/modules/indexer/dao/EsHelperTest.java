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
package fr.cnes.regards.modules.indexer.dao;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author oroussel
 */
public class EsHelperTest {

    @Test
    public void testToMeters() {
        Assert.assertEquals(25.4, EsHelper.toMeters("1e3 in"), 0.01);
        Assert.assertEquals(1000.0, EsHelper.toMeters("1e3"), 0.01);
        Assert.assertEquals(1.0E-4, EsHelper.toMeters(".1e-3"), 0.00001);
        Assert.assertEquals(255.0, EsHelper.toMeters("255 m"), 0.1);
        Assert.assertEquals(25450.0, EsHelper.toMeters("25.45 km"), 0.1);


    }
}
