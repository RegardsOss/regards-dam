/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FragmentServiceTest.class);

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
     * Attribute model service
     */
    @Mock
    private IAttributeModelService mockAttModelS;

    @Captor
    private ArgumentCaptor<Iterable<AttributeModel>> attModelCaptor;

    /**
     * Mocked fragment service
     */
    private IFragmentService fragmentService;

    @Before
    public void beforeTest() {
        fragmentService = new FragmentService(mockFragmentR, mockAttModelR, mockAttModelS);
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

    /**
     * Test fragment export
     *
     * @throws ModuleException
     *             if error occurs!
     */
    @Test
    public void exportFragmentTest() throws ModuleException {
        final Long fragmentId = 1L;
        final Fragment expected = Fragment.buildFragment(TEST_FRAG_NAME, TEST_FRAG_DESC);
        expected.setId(fragmentId);

        final List<AttributeModel> attModels = new ArrayList<>();
        attModels.add(AttributeModelBuilder.build("NAME", AttributeType.BOOLEAN).withoutRestriction());
        attModels.add(AttributeModelBuilder.build("PROFILE", AttributeType.STRING)
                .withEnumerationRestriction("public", "scientist", "user"));
        attModels.add(AttributeModelBuilder.build("DATA", AttributeType.FLOAT_ARRAY).description("physical data")
                .withoutRestriction());

        Mockito.when(mockFragmentR.exists(fragmentId)).thenReturn(true);
        Mockito.when(mockFragmentR.findOne(fragmentId)).thenReturn(expected);
        Mockito.when(mockAttModelR.findByFragmentId(fragmentId)).thenReturn(attModels);

        try {
            final OutputStream output = Files.newOutputStream(Paths.get("target", expected.getName() + ".xml"));
            fragmentService.exportFragment(fragmentId, output);
        } catch (IOException e) {
            LOGGER.debug("Cannot export fragment");
            Assert.fail();
        }
    }

    @Test
    public void importFragmentTest() throws ModuleException {
        try {
            final InputStream input = Files
                    .newInputStream(Paths.get("src", "test", "resources", "sample-fragment.xml"));

            fragmentService.importFragment(input);

            // Capture read data
            Mockito.verify(mockAttModelS).addAllAttributes(attModelCaptor.capture());
            final Iterable<AttributeModel> attModels = attModelCaptor.getValue();
            LOGGER.debug("argument captured");

        } catch (IOException e) {
            LOGGER.debug("Cannot import fragment");
            Assert.fail();
        }
    }
}
