/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.validator;

import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 * @author Marc Sordi
 *
 */
@Component
public class XmlDefinedConstraintValidatorFactory implements org.springframework.validation.Validator, InitializingBean,
        ApplicationContextAware, ConstraintValidatorFactory {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(XmlDefinedConstraintValidatorFactory.class);

    /**
     * Custom validator
     */
    private Validator validator;

    /**
     * Spring application context
     */
    private ApplicationContext applicationContext;

    @Override
    public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> pKey) {
        Map<String, T> beansByNames = applicationContext.getBeansOfType(pKey);
        if (beansByNames.isEmpty()) {
            try {
                return pKey.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException("Could not instantiate constraint validator class '" + pKey.getName() + "'",
                        e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Could not instantiate constraint validator class '" + pKey.getName() + "'",
                        e);
            }
        }

        if (beansByNames.size() > 1) {
            throw new RuntimeException(
                    "Only one bean of type '" + pKey.getName() + "' is allowed in the application context");
        }
        return beansByNames.values().iterator().next();

    }

    @Override
    public void releaseInstance(ConstraintValidator<?, ?> pInstance) {
        LOGGER.debug("releaseinstance");
    }

    @Override
    public void setApplicationContext(ApplicationContext pApplicationContext) throws BeansException {
        this.applicationContext = pApplicationContext;

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ValidatorFactory validatorFactory = Validation.byDefaultProvider().configure().constraintValidatorFactory(this)
                .buildValidatorFactory();
        validator = validatorFactory.usingContext().getValidator();
    }

    @Override
    public boolean supports(Class<?> pClazz) {
        return true;
    }

    @Override
    public void validate(Object pTarget, Errors pErrors) {
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(pTarget);

        for (ConstraintViolation<Object> constraintViolation : constraintViolations) {
            String propertyPath = constraintViolation.getPropertyPath().toString();
            String message = constraintViolation.getMessage();
            pErrors.rejectValue(propertyPath, "", message);
        }

    }

}
