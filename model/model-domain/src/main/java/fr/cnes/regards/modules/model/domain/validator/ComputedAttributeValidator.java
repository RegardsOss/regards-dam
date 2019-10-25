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
package fr.cnes.regards.modules.model.domain.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.utils.RsRuntimeException;
import fr.cnes.regards.framework.utils.plugins.PluginUtils;
import fr.cnes.regards.modules.model.domain.ComputationMode;
import fr.cnes.regards.modules.model.domain.ComputationPlugin;
import fr.cnes.regards.modules.model.domain.IComputedAttribute;
import fr.cnes.regards.modules.model.domain.Model;
import fr.cnes.regards.modules.model.domain.ModelAttrAssoc;

/**
 * Enforce the constraints described by {@link ComputedAttribute}
 *
 * @author Sylvain Vissiere-Guerinet
 */
public class ComputedAttributeValidator implements ConstraintValidator<ComputedAttribute, ModelAttrAssoc> {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ComputedAttributeValidator.class);

    @Override
    public void initialize(ComputedAttribute pConstraintAnnotation) {
        // nothing special to initialize
    }

    @Override
    public boolean isValid(ModelAttrAssoc modelAttrAssoc, ConstraintValidatorContext context) {
        if (modelAttrAssoc == null) {
            return true;
        }
        if (modelAttrAssoc.getAttribute() == null) {
            String msg = "ModelAttrAssoc to validate has no AttributeModel specified";
            LOG.debug(msg);
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(msg).addConstraintViolation();
            return false;
        }
        PluginConfiguration computationConf = modelAttrAssoc.getComputationConf();
        if (computationConf != null) {
            computationConf.setMetaData(PluginUtils.getPlugins().get(computationConf.getPluginId()));
        }
        if (modelAttrAssoc.getMode() == ComputationMode.COMPUTED) {
            // If computed attribute, check that the model is a dataset model
            Model model = modelAttrAssoc.getModel();
            if (model.getType() != EntityType.DATASET) {
                String msg = String.format(ComputedAttribute.WRONG_MODEL_TYPE, modelAttrAssoc.getAttribute().getName(),
                                           computationConf, EntityType.DATASET);
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(msg).addConstraintViolation();
                return false;
            }
            // If computed attribute, check that computation plugin mechanism is correct
            if (modelAttrAssoc.getMode() == ComputationMode.COMPUTED && computationConf != null
                    && computationConf.getInterfaceNames().contains(IComputedAttribute.class.getName())) {

                try {
                    // Retrieve annotation and check plugin supported type
                    ComputationPlugin computationPlugin = Class.forName(computationConf.getPluginClassName())
                            .getAnnotation(ComputationPlugin.class);
                    // Check validated attribute type is the same as plugin managed attribute type
                    boolean ok = computationPlugin.supportedType() == modelAttrAssoc.getAttribute().getType();
                    if (!ok) {
                        String msg = String.format(ComputedAttribute.INCOMPATIBLE_TYPE,
                                                   modelAttrAssoc.getAttribute().getName(), computationConf);
                        context.disableDefaultConstraintViolation();
                        context.buildConstraintViolationWithTemplate(msg).addConstraintViolation();
                    }
                    return ok;
                } catch (ClassNotFoundException e) {
                    LOG.error("ModelAttrAssoc of id: " + modelAttrAssoc.getId()
                            + " cannot be validated because we couldn't find annotation"
                            + ComputationPlugin.class.getName() + " on the associated plugin to check the "
                            + "coherence of its return type.");
                    throw new RsRuntimeException(e);
                }
            }
        }
        // It is not a computed attribute so it must be GIVEN one
        boolean ok = modelAttrAssoc.getMode() == ComputationMode.GIVEN;
        if (!ok) {
            String msg = String.format(ComputedAttribute.DEFAULT_TEMPLATE, modelAttrAssoc.getAttribute().getName(),
                                       computationConf);
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(msg).addConstraintViolation();
        }
        return ok;
    }

}
