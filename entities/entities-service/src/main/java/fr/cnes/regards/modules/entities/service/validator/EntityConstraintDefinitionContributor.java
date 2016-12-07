/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.validator;

import org.hibernate.validator.spi.constraintdefinition.ConstraintDefinitionContributor;

import fr.cnes.regards.modules.entities.domain.validator.CheckModel;

/**
 * Entity constraint configuration
 *
 * @author Marc Sordi
 *
 */
public class EntityConstraintDefinitionContributor implements ConstraintDefinitionContributor {

    @Override
    public void collectConstraintDefinitions(ConstraintDefinitionBuilder pConstraintDefinitionContributionBuilder) {
        pConstraintDefinitionContributionBuilder.constraint(CheckModel.class).validatedBy(ModelValidator.class);
    }

}
