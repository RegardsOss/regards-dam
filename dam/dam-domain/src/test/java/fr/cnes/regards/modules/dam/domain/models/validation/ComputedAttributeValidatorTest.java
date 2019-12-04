/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.domain.models.validation;

import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.BeforeClass;
import org.junit.Test;

import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginMetaData;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.model.domain.IComputedAttribute;
import fr.cnes.regards.modules.model.domain.Model;
import fr.cnes.regards.modules.model.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.model.dto.properties.PropertyType;

/**
 * @author Sylvain Vissiere-Guerinet
 */
public class ComputedAttributeValidatorTest {

    /**
     * Validator
     */
    private static Validator validator;

    @BeforeClass
    public static void init() {
        final ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test(expected = ValidationException.class)
    public void testUnknownPlugin() {
        // initialize the association
        ModelAttrAssoc invalidAssoc = new ModelAttrAssoc(
                AttributeModelBuilder.build("forTest", PropertyType.INTEGER, "ForTests").get(),
                Model.build("testModel", "pDescription", EntityType.DATASET));
        // get a PluginConfiguration
        PluginMetaData metaData = new PluginMetaData();
        metaData.setPluginId("tata");
        metaData.setAuthor("toto");
        metaData.setDescription("titi");
        metaData.setVersion("tutu");
        metaData.getInterfaceNames().add(IComputedAttribute.class.getName());
        metaData.setPluginClassName("toto");
        PluginConfiguration conf = new PluginConfiguration("testConf", metaData.getPluginId());
        invalidAssoc.setComputationConf(conf);
        validator.validate(invalidAssoc);
    }
}
