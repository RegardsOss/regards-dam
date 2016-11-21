/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import fr.cnes.regards.framework.module.rest.exception.EntityAlreadyExistsException;
import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotEmptyException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotIdentifiableException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.models.dao.IAttributeModelRepository;
import fr.cnes.regards.modules.models.dao.IFragmentRepository;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;

/**
 * Test fragment service
 *
 * @author Marc Sordi
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class FragmentServiceTest {

    /**
     * Test fragment name
     */
    private static final String TEST_FRAG_NAME = "FRAG";

    /**
     * Test fragment name
     */
    private static final String TEST_FRAG_DESC = "Test fragment";

    /**
     * Fragment repository
     */
    @Mock
    private IFragmentRepository mockFragmentR;

    /**
     * Attribute model repository
     */
    @Mock
    private IAttributeModelRepository mockAttModelR;

    /**
     * Mocked fragment service
     */
    private IFragmentService fragmentService;

    @Before
    public void beforeTest() {
        fragmentService = new FragmentService(mockFragmentR, mockAttModelR);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    @Purpose("Create a fragment")
    public void addFragmentTest() throws ModuleException {
        final Fragment expected = Fragment.buildFragment(TEST_FRAG_NAME, TEST_FRAG_DESC);

        Mockito.when(mockFragmentR.findByName(TEST_FRAG_NAME)).thenReturn(null);
        Mockito.when(mockFragmentR.save(expected)).thenReturn(expected);

        final Fragment retrieved = fragmentService.addFragment(expected);
        Assert.assertNotNull(retrieved);
    }

    @Requirement("REGARDS_DSL_DAM_MOD_020")
    @Purpose("Manage conflicting fragment")
    @Test(expected = EntityAlreadyExistsException.class)
    public void addExistingFragmentTest() throws ModuleException {
        final Fragment expected = Fragment.buildFragment(TEST_FRAG_NAME, TEST_FRAG_DESC);

        Mockito.when(mockFragmentR.findByName(TEST_FRAG_NAME)).thenReturn(expected);

        fragmentService.addFragment(expected);
    }

    @Test(expected = EntityNotIdentifiableException.class)
    public void updateNotIdetnfiableFragmentTest() throws ModuleException {
        final Long fragmentId = 1L;
        final Fragment expected = Fragment.buildFragment(TEST_FRAG_NAME, TEST_FRAG_DESC);

        fragmentService.updateFragment(fragmentId, expected);
    }

    @Test(expected = EntityInconsistentIdentifierException.class)
    public void updateInconsistentFragmentTest() throws ModuleException {
        final Long fragmentId = 1L;
        final Fragment expected = Fragment.buildFragment(TEST_FRAG_NAME, TEST_FRAG_DESC);
        expected.setId(2L);

        fragmentService.updateFragment(fragmentId, expected);
    }

    @Test(expected = EntityNotFoundException.class)
    public void updateUnknownFragmentTest() throws ModuleException {
        final Long fragmentId = 1L;
        final Fragment expected = Fragment.buildFragment(TEST_FRAG_NAME, TEST_FRAG_DESC);
        expected.setId(fragmentId);

        Mockito.when(mockFragmentR.exists(fragmentId)).thenReturn(false);

        fragmentService.updateFragment(fragmentId, expected);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    @Purpose("Update a fragment")
    public void updateFragmentTest() throws ModuleException {
        final Long fragmentId = 1L;
        final Fragment expected = Fragment.buildFragment(TEST_FRAG_NAME, TEST_FRAG_DESC);
        expected.setId(fragmentId);

        Mockito.when(mockFragmentR.exists(fragmentId)).thenReturn(true);
        Mockito.when(mockFragmentR.save(expected)).thenReturn(expected);

        Assert.assertNotNull(fragmentService.updateFragment(fragmentId, expected));
    }

    @Requirement("REGARDS_DSL_DAM_MOD_020")
    @Purpose("Manage fragment deletion")
    @Test(expected = EntityNotEmptyException.class)
    public void deleteNonEmptyFragment() throws ModuleException {
        final Long fragmentId = 1L;
        final List<AttributeModel> attModels = new ArrayList<>();
        attModels.add(AttributeModelBuilder.build("MOCK", AttributeType.GEOMETRY).withoutRestriction());

        Mockito.when(mockAttModelR.findByFragmentId(fragmentId)).thenReturn(attModels);

        fragmentService.deleteFragment(fragmentId);
    }
}
