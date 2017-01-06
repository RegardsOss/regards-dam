/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.module.rest.exception.EntityAlreadyExistsException;
import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotIdentifiableException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.models.dao.IAttributeModelRepository;
import fr.cnes.regards.modules.models.dao.IAttributePropertyRepository;
import fr.cnes.regards.modules.models.dao.IFragmentRepository;
import fr.cnes.regards.modules.models.dao.IRestrictionRepository;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.models.domain.attributes.AttributeProperty;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.models.domain.attributes.Fragment;
import fr.cnes.regards.modules.models.domain.attributes.restriction.RestrictionFactory;
import fr.cnes.regards.modules.models.service.exception.UnsupportedRestrictionException;

/**
 *
 * Test attribute model service
 *
 * @author Marc Sordi
 *
 */
public class AttributeModelServiceTest {

    /**
     * Default attribute name
     */
    private static final String ATT_NAME = "DEFAULT_NAME";

    /**
     * Attribute model service
     */
    private IAttributeModelService attributeModelService;

    /**
     * Attribute model repository
     */
    private IAttributeModelRepository mockAttModelR;

    /**
     * Restriction repository
     */
    private IRestrictionRepository mockRestrictionR;

    /**
     * Fragment repository
     */
    private IFragmentRepository mockFragmentR;

    /**
     * {@link AttributeProperty} repository
     */
    private IAttributePropertyRepository mockAttPropertyR;

    /**
     * Publish for model changes
     */
    private IPublisher mockPublisher;

    @Before
    public void beforeTest() {
        mockAttModelR = Mockito.mock(IAttributeModelRepository.class);
        mockRestrictionR = Mockito.mock(IRestrictionRepository.class);
        mockFragmentR = Mockito.mock(IFragmentRepository.class);
        mockAttPropertyR = Mockito.mock(IAttributePropertyRepository.class);
        mockPublisher = Mockito.mock(IPublisher.class);
        attributeModelService = new AttributeModelService(mockAttModelR, mockRestrictionR, mockFragmentR,
                mockAttPropertyR, mockPublisher);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_060")
    @Purpose("Retrieve list of project attributes")
    public void getAttributesTest() {
        final List<AttributeModel> expectedAttModels = new ArrayList<>();
        expectedAttModels.add(AttributeModelBuilder.build("FIRST", AttributeType.STRING).get());
        expectedAttModels.add(AttributeModelBuilder.build("SECOND", AttributeType.BOOLEAN).get());

        Mockito.when(mockAttModelR.findAll()).thenReturn(expectedAttModels);

        final List<AttributeModel> attModels = attributeModelService.getAttributes(null, null);
        Assert.assertEquals(2, attModels.size());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    @Requirement("REGARDS_DSL_DAM_MOD_060")
    @Purpose("Retrieve list of project attributes by type")
    public void getAttributesByTypeTest() {
        final List<AttributeModel> expectedAttModels = new ArrayList<>();
        expectedAttModels.add(AttributeModelBuilder.build("FIRST_STRING", AttributeType.STRING).get());

        Mockito.when(mockAttModelR.findByType(AttributeType.STRING)).thenReturn(expectedAttModels);

        final List<AttributeModel> attModels = attributeModelService.getAttributes(AttributeType.STRING, null);
        Assert.assertEquals(1, attModels.size());
    }

    /**
     * Test attribute creation
     *
     * @throws ModuleException
     *             if error occurs!
     */
    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    @Purpose("Create an attribute and automatically bind it to default fragment (i.e. namespace)")
    public void addAttributeTest() throws ModuleException {
        final String attName = "MISSION";
        final AttributeType attType = AttributeType.STRING;
        final AttributeModel expectedAttModel = AttributeModelBuilder.build(attName, attType).withoutRestriction();

        Mockito.when(mockFragmentR.findByName(Fragment.getDefaultName())).thenReturn(Fragment.buildDefault());
        Mockito.when(mockAttModelR.findByNameAndFragmentName(attName, Fragment.getDefaultName())).thenReturn(null);
        Mockito.when(mockAttModelR.save(expectedAttModel)).thenReturn(expectedAttModel);

        attributeModelService.addAttribute(expectedAttModel);
        Assert.assertTrue(expectedAttModel.getFragment().isDefaultFragment());
        Assert.assertNull(expectedAttModel.getRestriction());
    }

    /**
     *
     * @throws ModuleException
     *             if error occurs!
     */
    @Test
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    @Requirement("REGARDS_DSL_DAM_MOD_050")
    @Purpose("Manage a GEO fragment (i.e. consistent object)")
    public void addAttributeInFragmentTest() throws ModuleException {
        final String attName = "COORDINATE";
        final AttributeType attType = AttributeType.GEOMETRY;
        final Fragment fragment = Fragment.buildFragment("GEO", "Coordinate + CRS");

        final AttributeModel expectedAttModel = AttributeModelBuilder.build(attName, attType).fragment(fragment)
                .withoutRestriction();

        Mockito.when(mockFragmentR.findByName(Fragment.getDefaultName())).thenReturn(Fragment.buildDefault());
        Mockito.when(mockFragmentR.save(fragment)).thenReturn(fragment);
        Mockito.when(mockAttModelR.findByNameAndFragmentName(attName, Fragment.getDefaultName())).thenReturn(null);
        Mockito.when(mockAttModelR.save(expectedAttModel)).thenReturn(expectedAttModel);

        attributeModelService.addAttribute(expectedAttModel);
        Assert.assertTrue(expectedAttModel.getFragment().equals(fragment));
        Assert.assertNull(expectedAttModel.getRestriction());
    }

    /**
     *
     * @throws ModuleException
     *             if error occurs!
     */
    @Test(expected = UnsupportedRestrictionException.class)
    public void addAttributeWithUnsupportedRestriction() throws ModuleException {
        final String attName = "RESTRICTED";
        final AttributeType attType = AttributeType.STRING;
        final AttributeModel expectedAttModel = AttributeModelBuilder.build(attName, attType).withoutRestriction();
        // Bypass builder to set a bad restriction
        // CHECKSTYLE:OFF
        expectedAttModel.setRestriction(RestrictionFactory.buildIntegerRangeRestriction(0, 10, false, false));
        // CHECKSTYLE:ON
        Mockito.when(mockFragmentR.findByName(Fragment.getDefaultName())).thenReturn(Fragment.buildDefault());
        Mockito.when(mockAttModelR.findByNameAndFragmentName(attName, Fragment.getDefaultName())).thenReturn(null);
        Mockito.when(mockAttModelR.save(expectedAttModel)).thenReturn(expectedAttModel);

        attributeModelService.addAttribute(expectedAttModel);
    }

    /**
     * Add an already existing attribute
     *
     * @throws ModuleException
     *             when conflict is detected
     */
    @Test(expected = EntityAlreadyExistsException.class)
    public void addConflictAttributeTest() throws ModuleException {
        final String attName = "CONFLICT";
        final AttributeType attType = AttributeType.STRING;
        final AttributeModel expectedAttModel = AttributeModelBuilder.build(attName, attType).withoutRestriction();

        Mockito.when(mockFragmentR.findByName(Fragment.getDefaultName())).thenReturn(Fragment.buildDefault());
        Mockito.when(mockAttModelR.findByNameAndFragmentName(attName, Fragment.getDefaultName()))
                .thenReturn(expectedAttModel);

        attributeModelService.addAttribute(expectedAttModel);
    }

    @Test(expected = EntityNotFoundException.class)
    public void getUnknownAttributeTest() throws ModuleException {
        final Long attributeId = 1L;

        Mockito.when(mockAttModelR.exists(attributeId)).thenReturn(Boolean.FALSE);
        attributeModelService.getAttribute(attributeId);
    }

    @Test
    public void getAttributeTest() throws ModuleException {
        final Long attributeId = 1L;

        final AttributeModel expectedAttModel = AttributeModelBuilder.build("EXISTING", AttributeType.DOUBLE)
                .withoutRestriction();
        Mockito.when(mockAttModelR.exists(attributeId)).thenReturn(Boolean.TRUE);
        Mockito.when(mockAttModelR.findOne(attributeId)).thenReturn(expectedAttModel);

        final AttributeModel attModel = attributeModelService.getAttribute(attributeId);
        Assert.assertNotNull(attModel);
    }

    @Test(expected = EntityNotIdentifiableException.class)
    public void updateNotIdentifiableAttributeTest() throws ModuleException {
        final AttributeModel expectedAttModel = AttributeModelBuilder.build(ATT_NAME, AttributeType.DOUBLE)
                .withoutRestriction();
        attributeModelService.updateAttribute(1L, expectedAttModel);
    }

    @Test(expected = EntityInconsistentIdentifierException.class)
    public void updateInconsistentAttributeTest() throws ModuleException {
        final AttributeModel expectedAttModel = AttributeModelBuilder.build(ATT_NAME, AttributeType.DOUBLE)
                .withoutRestriction();
        expectedAttModel.setId(1L);
        attributeModelService.updateAttribute(2L, expectedAttModel);

    }

    @Test
    public void updateAttributeTest() throws ModuleException {
        final Long attributeId = 1L;
        final AttributeModel expectedAttModel = AttributeModelBuilder.build(ATT_NAME, AttributeType.DOUBLE)
                .withoutRestriction();
        expectedAttModel.setId(attributeId);

        Mockito.when(mockAttModelR.exists(attributeId)).thenReturn(Boolean.TRUE);
        Mockito.when(mockAttModelR.save(expectedAttModel)).thenReturn(expectedAttModel);

        final AttributeModel attModel = attributeModelService.updateAttribute(attributeId, expectedAttModel);
        Assert.assertNotNull(attModel);
    }

    /**
     * Delete attribute
     */
    @Test
    public void deleteAttributeTest() {
        final Long attributeId = 1L;
        final AttributeModel expectedAttModel = AttributeModelBuilder.build(ATT_NAME, AttributeType.DOUBLE)
                .withoutRestriction();
        expectedAttModel.setId(attributeId);

        Mockito.when(mockAttModelR.exists(attributeId)).thenReturn(Boolean.FALSE);

        attributeModelService.deleteAttribute(attributeId);

        Mockito.when(mockAttModelR.exists(attributeId)).thenReturn(Boolean.TRUE);
        final IAttributeModelService spy = Mockito.spy(attributeModelService);
        Mockito.doNothing().when(spy).deleteAttribute(attributeId);

        attributeModelService.deleteAttribute(attributeId);
    }

    @Test
    public void isFragmentAttributeTest() throws ModuleException {
        final Long attributeId = 1L;
        final Fragment fragment = Fragment.buildFragment("CONTACT", "Name + Surname + Phone + ...");

        final AttributeModel expectedAttModel = AttributeModelBuilder.build("PHONE", AttributeType.STRING)
                .fragment(fragment).withoutRestriction();

        Mockito.when(mockAttModelR.findOne(attributeId)).thenReturn(expectedAttModel);

        final boolean is = attributeModelService.isFragmentAttribute(attributeId);
        Assert.assertTrue(is);
    }
}
