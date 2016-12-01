/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.jpa.multitenant.test.AbstractDaoTransactionalTest;
import fr.cnes.regards.modules.entities.dao.domain.TestEntity;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Tag;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.dao.IModelRepository;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.ModelType;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@TestPropertySource("classpath:application-test.properties")
public class AbstractEntityDaoTest extends AbstractDaoTransactionalTest {

    private static final Tag TAG_TO_SEARCH = new Tag("tag");

    private static final Tag TAG_TEST = new Tag("TEST");

    private static final Tag TAG_LAST = new Tag("LAST");

    @Autowired
    private IAbstractEntityRepository<AbstractEntity> entityRepository;

    @Autowired
    private IModelRepository modelRepository;

    @Test
    public void testfindByTagsValue() {

        final Model model = Model.build("name", "desc", ModelType.COLLECTION);
        modelRepository.save(model);

        TestEntity entity1 = new TestEntity(model, "Entity");
        final Set<Tag> entity1Tags = new HashSet<>();
        entity1Tags.add(TAG_TO_SEARCH);
        entity1Tags.add(TAG_TEST);
        entity1Tags.add(TAG_LAST);
        entity1.setTags(entity1Tags);
        entity1 = entityRepository.save(entity1);

        TestEntity entity2 = new TestEntity(model, "Entity");
        final Set<Tag> entity2Tags = new HashSet<>();
        entity2Tags.add(TAG_TEST);
        entity2Tags.add(TAG_LAST);
        entity2Tags.add(TAG_TO_SEARCH);
        entity2.setTags(entity2Tags);
        entity2 = entityRepository.save(entity2);

        TestEntity entity3 = new TestEntity(model, "Entity");
        final Set<Tag> entity3Tags = new HashSet<>();
        entity3Tags.add(TAG_TEST);
        entity3Tags.add(TAG_TO_SEARCH);
        entity3Tags.add(TAG_LAST);
        entity3.setTags(entity3Tags);
        entity3 = entityRepository.save(entity3);

        TestEntity entity4 = new TestEntity(model, "Entity");
        final Set<Tag> entity4Tags = new HashSet<>();
        entity4Tags.add(TAG_TEST);
        entity4Tags.add(TAG_LAST);
        entity4.setTags(entity4Tags);
        entity4 = entityRepository.save(entity4);

        final List<AbstractEntity> result = entityRepository.findByTagsValue(TAG_TO_SEARCH.getValue());
        Assert.assertTrue(result.contains(entity1));
        Assert.assertTrue(result.contains(entity2));
        Assert.assertTrue(result.contains(entity3));
        Assert.assertFalse(result.contains(entity4));

    }

    @Test
    public void testFindByIpIdIn() {
        final Model model = Model.build("name", "desc", ModelType.COLLECTION);
        modelRepository.save(model);

        TestEntity entity1 = new TestEntity(model, "Entity");
        entity1 = entityRepository.save(entity1);

        TestEntity entity2 = new TestEntity(model, "Entity");
        entity2 = entityRepository.save(entity2);

        TestEntity entity3 = new TestEntity(model, "Entity");
        entity3 = entityRepository.save(entity3);

        TestEntity entity4 = new TestEntity(model, "Entity");
        entity4 = entityRepository.save(entity4);

        final Set<UniformResourceName> ipIds = new HashSet<>();
        ipIds.add(entity1.getIpId());
        ipIds.add(entity3.getIpId());
        ipIds.add(entity2.getIpId());

        final List<AbstractEntity> result = entityRepository.findByIpIdIn(ipIds);
        Assert.assertTrue(result.contains(entity1));
        Assert.assertTrue(result.contains(entity2));
        Assert.assertTrue(result.contains(entity3));
        Assert.assertFalse(result.contains(entity4));
    }

    @Test
    public void testFindByIpIdInEmptySet() {
        final Model model = Model.build("name", "desc", ModelType.COLLECTION);
        modelRepository.save(model);

        TestEntity entity1 = new TestEntity(model, "Entity");
        entity1 = entityRepository.save(entity1);

        TestEntity entity2 = new TestEntity(model, "Entity");
        entity2 = entityRepository.save(entity2);

        TestEntity entity3 = new TestEntity(model, "Entity");
        entity3 = entityRepository.save(entity3);

        TestEntity entity4 = new TestEntity(model, "Entity");
        entity4 = entityRepository.save(entity4);

        final Set<UniformResourceName> ipIds = new HashSet<>();

        final List<AbstractEntity> result = entityRepository.findByIpIdIn(ipIds);
        Assert.assertEquals(0, result.size());
    }

}
