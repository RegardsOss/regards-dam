/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.dao;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.jpa.multitenant.test.AbstractDaoTransactionalTest;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.urn.OAISIdentifier;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.dao.IModelRepository;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

/**
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
@TestPropertySource("classpath:application-test.properties")
@Ignore
public class CollectionTest extends AbstractDaoTransactionalTest {

    @Autowired
    private ICollectionRepository collectionRepository;

    @Autowired
    private IModelRepository modelRepository;

    @Test
    public void test() {
        Model model1 = new Model();
        model1.setName("Model1");
        model1.setType(EntityType.COLLECTION);
        model1 = modelRepository.save(model1);
        UniformResourceName collectionUrn = new UniformResourceName(OAISIdentifier.AIP, EntityType.COLLECTION,
                "PROJECT", UUID.randomUUID(), 1);
        Collection coll = new Collection(model1, collectionUrn, "coll");
        coll.setSipId("IpID");
        Collection collection1 = collectionRepository.save(coll);
        Collection collection2 = collectionRepository.findOne(collection1.getId());
        Assert.assertEquals(collection1, collection2);
    }

}
