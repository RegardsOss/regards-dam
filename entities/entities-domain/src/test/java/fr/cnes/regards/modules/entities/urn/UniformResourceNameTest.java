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
package fr.cnes.regards.modules.entities.urn;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.models.domain.EntityType;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
public class UniformResourceNameTest {

    @Test
    @Requirement("REGARDS_DSL_SYS_ARC_410")
    @Purpose("The SIP identifier is an URN")
    public void testFromStringSIP() {
        final UniformResourceName sipUrn = new UniformResourceName(OAISIdentifier.SIP, EntityType.COLLECTION, "CDPP",
                UUID.randomUUID(), 1);
        final Pattern pattern = Pattern.compile(UniformResourceName.URN_PATTERN);
        final Matcher matcher = pattern.matcher(sipUrn.toString());
        Assert.assertTrue(pattern.matcher(sipUrn.toString()).matches());
    }

    @Test
    @Requirement("REGARDS_DSL_SYS_ARC_410")
    @Purpose("The AIP identifier is an URN")
    public void testFromStringFullAIP() {
        final UniformResourceName aipUrn = new UniformResourceName(OAISIdentifier.AIP, EntityType.COLLECTION, "CDPP",
                UUID.randomUUID(), 1, 2L, "3");
        final Pattern pattern = Pattern.compile(UniformResourceName.URN_PATTERN);
        Assert.assertTrue(pattern.matcher(aipUrn.toString()).matches());
    }

    @Test
    @Requirement("REGARDS_DSL_SYS_ARC_410")
    @Purpose("The AIP identifier is an URN")
    public void testFromStringAIPWithoutRevision() {
        final UniformResourceName aipUrn = new UniformResourceName(OAISIdentifier.AIP, EntityType.COLLECTION, "CDPP",
                UUID.randomUUID(), 1, 2L);
        final Pattern pattern = Pattern.compile(UniformResourceName.URN_PATTERN);
        Assert.assertTrue(pattern.matcher(aipUrn.toString()).matches());
    }

    @Test
    @Requirement("REGARDS_DSL_SYS_ARC_410")
    @Purpose("The AIP identifier is an URN")
    public void testFromStringAIPWithoutOrder() {
        final UniformResourceName aipUrn = new UniformResourceName(OAISIdentifier.AIP, EntityType.COLLECTION, "CDPP",
                UUID.randomUUID(), 1, "revision");
        final Pattern pattern = Pattern.compile(UniformResourceName.URN_PATTERN);
        Assert.assertTrue(pattern.matcher(aipUrn.toString()).matches());
    }

    @Test
    @Requirement("REGARDS_DSL_SYS_ARC_410")
    @Purpose("The AIP identifier is an URN")
    public void testFromStringAIPWithoutOrderOrRevision() {
        final UniformResourceName aipUrn = new UniformResourceName(OAISIdentifier.AIP, EntityType.COLLECTION, "CDPP",
                UUID.randomUUID(), 1);
        final Pattern pattern = Pattern.compile(UniformResourceName.URN_PATTERN);
        Assert.assertTrue(pattern.matcher(aipUrn.toString()).matches());
    }

}
