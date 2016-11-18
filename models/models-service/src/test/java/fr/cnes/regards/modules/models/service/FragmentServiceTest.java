/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.models.dao.IAttributeModelRepository;
import fr.cnes.regards.modules.models.dao.IFragmentRepository;
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
    public void addFragmentTest() throws ModuleException {
        final String name = "FRAG";
        final Fragment expected = Fragment.buildFragment(name, "test frag");

        Mockito.when(mockFragmentR.findByName(name)).thenReturn(null);
        Mockito.when(mockFragmentR.save(expected)).thenReturn(expected);

        final Fragment retrieved = fragmentService.addFragment(expected);
        Assert.assertNotNull(retrieved);
    }

    // @Override
    // public Fragment addFragment(Fragment pFragment) throws ModuleException {
    // final Fragment existing = fragmentRepository.findByName(pFragment.getName());
    // if (existing != null) {
    // throw new EntityAlreadyExistsException(
    // String.format("Fragment with name \"%s\" already exists!", pFragment.getName()));
    // }
    // return fragmentRepository.save(pFragment);
    // }
}
