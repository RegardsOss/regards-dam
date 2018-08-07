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
package fr.cnes.regards.modules.opensearch.service.cache.attributemodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;

import fr.cnes.regards.framework.amqp.ISubscriber;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.dam.client.models.IAttributeModelClient;
import fr.cnes.regards.modules.dam.domain.entities.StaticProperties;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModel;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeType;
import fr.cnes.regards.modules.dam.domain.models.attributes.Fragment;

/**
 * Test attribute property map algorithm
 *
 * @author Marc Sordi
 *
 */
public class PropertyMapChecker {

    private static final String TENANT = "tenant";

    private static final int STATICS_NB = 6;

    private AttributeModelCache cache;

    private IAttributeModelClient attributeModelClientMock;

    private List<AttributeModel> atts;

    @Before
    public void init() {
        atts = new ArrayList<>();
        attributeModelClientMock = Mockito.mock(IAttributeModelClient.class);
        cache = new AttributeModelCache(attributeModelClientMock, Mockito.mock(ISubscriber.class),
                Mockito.mock(IRuntimeTenantResolver.class));
    }

    private Map<String, AttributeModel> getBuiltMap(List<AttributeModel> atts) {
        List<Resource<AttributeModel>> resAtts = new ArrayList<>();
        atts.forEach(att -> resAtts.add(new Resource<AttributeModel>(att)));
        Mockito.when(attributeModelClientMock.getAttributes(null, null)).thenReturn(ResponseEntity.ok(resAtts));
        cache.getAttributeModels(TENANT);
        // Return built map
        return cache.getPropertyMap().get(TENANT);
    }

    private void assertCount(Map<String, AttributeModel> builtMap, int expectedDynamics) {
        Assert.assertNotNull(builtMap);
        int countStatics = 0;
        int countDynamics = 0;
        for (Map.Entry<String, AttributeModel> entry : builtMap.entrySet()) {
            if (entry.getValue().isDynamic()) {
                countDynamics++;
            } else {
                countStatics++;
            }
        }
        Assert.assertEquals(STATICS_NB, countStatics);
        Assert.assertEquals(expectedDynamics, countDynamics);
    }

    @Test
    public void conflict() {
        // Define attributes
        atts.add(AttributeModelBuilder
                .build(StaticProperties.FEATURE_TAGS, AttributeType.BOOLEAN, "Conflictual dynamic tags").get());

        // Build and get map
        Map<String, AttributeModel> builtMap = getBuiltMap(atts);
        assertCount(builtMap, 1);
    }

    @Test
    public void baseProperty() {
        // Define attributes
        String startDate = "START_DATE";
        AttributeModel startDateModel = AttributeModelBuilder.build(startDate, AttributeType.DATE_ISO8601, "Start date")
                .get();
        atts.add(startDateModel);

        // Build and get map
        Map<String, AttributeModel> builtMap = getBuiltMap(atts);
        assertCount(builtMap, 2);

        Assert.assertTrue(builtMap.containsKey(startDate));
        Assert.assertTrue(builtMap.containsKey(startDateModel.buildJsonPath(StaticProperties.FEATURE_PROPERTIES)));
    }

    @Test
    public void fragmentProperty() {
        // Define attributes
        String startDate = "START_DATE";
        String fragment = "fragment";
        AttributeModel startDateModel = AttributeModelBuilder.build(startDate, AttributeType.DATE_ISO8601, "Start date")
                .fragment(Fragment.buildFragment(fragment, "description")).get();
        atts.add(startDateModel);

        // Build and get map
        Map<String, AttributeModel> builtMap = getBuiltMap(atts);
        assertCount(builtMap, 3);

        Assert.assertTrue(builtMap.containsKey(startDate));
        Assert.assertTrue(builtMap.containsKey(startDateModel.buildJsonPath("")));
        Assert.assertTrue(builtMap.containsKey(startDateModel.buildJsonPath(StaticProperties.FEATURE_PROPERTIES)));
    }

    @Test
    public void conflicts() {
        // Define attributes
        String startDate = "START_DATE";
        String fragment1 = "fragment1";
        AttributeModel startDateModel = AttributeModelBuilder.build(startDate, AttributeType.DATE_ISO8601, "Start date")
                .fragment(Fragment.buildFragment(fragment1, "description")).get();
        atts.add(startDateModel);

        // Define conflictual attribute
        String fragment2 = "fragment2";
        AttributeModel startDateModel2 = AttributeModelBuilder
                .build(startDate, AttributeType.DATE_ISO8601, "Start date 2")
                .fragment(Fragment.buildFragment(fragment2, "description")).get();
        atts.add(startDateModel2);

        // Build and get map
        Map<String, AttributeModel> builtMap = getBuiltMap(atts);
        assertCount(builtMap, 4);

        Assert.assertTrue(!builtMap.containsKey(startDate));
        Assert.assertTrue(builtMap.containsKey(startDateModel.buildJsonPath("")));
        Assert.assertTrue(builtMap.containsKey(startDateModel.buildJsonPath(StaticProperties.FEATURE_PROPERTIES)));
        Assert.assertTrue(builtMap.containsKey(startDateModel2.buildJsonPath("")));
        Assert.assertTrue(builtMap.containsKey(startDateModel2.buildJsonPath(StaticProperties.FEATURE_PROPERTIES)));
    }
}
