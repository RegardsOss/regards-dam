/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.jpa.multitenant.test.AbstractDaoTransactionalTest;
import fr.cnes.regards.modules.collections.domain.Collection;
import fr.cnes.regards.modules.models.dao.IModelRepository;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.EntityType;

/**
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
// @ContextConfiguration(classes = { CollectionTestConfiguration.class })
@TestPropertySource("classpath:application-test.properties")
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
        Collection collection1 = collectionRepository.save(new Collection("IpID", model1, "pDescription", "pName"));
        Collection collection2 = collectionRepository.findOne(collection1.getId());
        Assert.assertEquals(collection1, collection2);
    }

}
