/*
 * LICENSE_PLACEHOLDER
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
    public boolean isValid(ModelAttrAssoc pValue, ConstraintValidatorContext pContext) {
        if (pValue == null) {
            return true;
        }
        if (pValue.getAttribute() == null) {
            LOG.debug("ModelAttrAssoc to validate has no AttributeModel specified");
            return false;
        }
        PluginConfiguration computationConf = pValue.getComputationConf();
        if (pValue.getMode().equals(ComputationMode.COMPUTED) && (computationConf != null)
                && computationConf.getInterfaceName().equals(IComputedAttribute.class.getName())) {

            IComputedAttribute<?, ?> plugin;
            try {
                plugin = (IComputedAttribute<?, ?>) Class.forName(computationConf.getPluginClassName()).newInstance();
                return plugin.getSupported().equals(pValue.getAttribute().getType());
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                LOG.error("ModelAttrAssoc of id: " + pValue.getId()
                        + " cannot be validated because we couldn't instanciate the associated plugin to check the coherence of its return type.");
                throw new RuntimeException(e); // NOSONAR
            }
        }
        return ComputationMode.GIVEN.equals(pValue.getMode());
    }

}
