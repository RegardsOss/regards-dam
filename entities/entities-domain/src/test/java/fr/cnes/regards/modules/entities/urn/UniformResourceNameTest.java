/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.urn;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
public class UniformResourceNameTest {

    @Test
    public void testFromStringSIP() {
        final UniformResourceName sipUrn = new UniformResourceName(OAISIdentifier.SIP, "Collection", "CDPP",
                UUID.randomUUID(), 1);
        final Pattern pattern = Pattern.compile(UniformResourceName.URN_PATTERN);
        final Matcher matcher = pattern.matcher(sipUrn.toString());
        Assert.assertTrue(pattern.matcher(sipUrn.toString()).matches());
    }

    @Test
    public void testFromStringFullAIP() {
        final UniformResourceName aipUrn = new UniformResourceName(OAISIdentifier.AIP, "Collection", "CDPP",
                UUID.randomUUID(), 1, 2L, "3");
        final Pattern pattern = Pattern.compile(UniformResourceName.URN_PATTERN);
        Assert.assertTrue(pattern.matcher(aipUrn.toString()).matches());
    }

    @Test
    public void testFromStringAIPWithoutRevision() {
        final UniformResourceName aipUrn = new UniformResourceName(OAISIdentifier.AIP, "Collection", "CDPP",
                UUID.randomUUID(), 1, 2L);
        final Pattern pattern = Pattern.compile(UniformResourceName.URN_PATTERN);
        Assert.assertTrue(pattern.matcher(aipUrn.toString()).matches());
    }

    @Test
    public void testFromStringAIPWithoutOrder() {
        final UniformResourceName aipUrn = new UniformResourceName(OAISIdentifier.AIP, "Collection", "CDPP",
                UUID.randomUUID(), 1, "revision");
        final Pattern pattern = Pattern.compile(UniformResourceName.URN_PATTERN);
        Assert.assertTrue(pattern.matcher(aipUrn.toString()).matches());
    }

    @Test
    public void testFromStringAIPWithoutOrderOrRevision() {
        final UniformResourceName aipUrn = new UniformResourceName(OAISIdentifier.AIP, "Collection", "CDPP",
                UUID.randomUUID(), 1);
        final Pattern pattern = Pattern.compile(UniformResourceName.URN_PATTERN);
        Assert.assertTrue(pattern.matcher(aipUrn.toString()).matches());
    }

}
