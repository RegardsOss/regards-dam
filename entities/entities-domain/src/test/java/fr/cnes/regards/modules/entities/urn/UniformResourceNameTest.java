/*
 * LICENSE_PLACEHOLDER
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
