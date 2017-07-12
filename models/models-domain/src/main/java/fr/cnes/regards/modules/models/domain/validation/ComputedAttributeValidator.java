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
package fr.cnes.regards.modules.models.domain.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.modules.models.domain.ComputationMode;
import fr.cnes.regards.modules.models.domain.IComputedAttribute;
import fr.cnes.regards.modules.models.domain.ModelAttrAssoc;

/**
 * Enforce the constraints described by {@link ComputedAttribute}
 *
 * @author Sylvain Vissiere-Guerinet
 */
public class ComputedAttributeValidator implements ConstraintValidator<ComputedAttribute, ModelAttrAssoc> {

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
        String template = context.getDefaultConstraintMessageTemplate();
        if (modelAttrAssoc.getAttribute() == null) {
            LOG.debug("ModelAttrAssoc to validate has no AttributeModel specified");
            String msg = String.format(template, "null", "undefined");
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(msg).addConstraintViolation();
            return false;
        }
        // If computed attribute, check that computation plugin mechanism is correct
        PluginConfiguration computationConf = modelAttrAssoc.getComputationConf();
        if ((modelAttrAssoc.getMode() == ComputationMode.COMPUTED) && (computationConf != null) && computationConf
                .getInterfaceNames().contains(IComputedAttribute.class.getName())) {

            IComputedAttribute<?, ?> plugin;
            try {
                // Retrieve computation plugin and instance it
                plugin = (IComputedAttribute<?, ?>) Class.forName(computationConf.getPluginClassName()).newInstance();
                // Check validated attribute type is the same as plugin managed attribute type
                boolean ok = (plugin.getSupported() == modelAttrAssoc.getAttribute().getType());
                if (!ok) {
                    String msg = String.format(template, modelAttrAssoc.getAttribute().getName(),
                                               computationConf);
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(msg).addConstraintViolation();
                }
                return ok;
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                LOG.error("ModelAttrAssoc of id: " + modelAttrAssoc.getId()
                                  + " cannot be validated because we couldn't instanciate the associated plugin to check the "
                                  + "coherence of its return type.");
                throw new RuntimeException(e); // NOSONAR
            }
        }
        // It is not a computed attribute so it must be GIVEN one
        boolean ok = (modelAttrAssoc.getMode() == ComputationMode.GIVEN);
        if (!ok) {
            String msg = String.format(template, modelAttrAssoc.getAttribute().getName(),
                                       computationConf);
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(msg).addConstraintViolation();
        }
        return ok;
    }

}
