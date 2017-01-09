package fr.cnes.regards.modules.entities.domain;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * Class identifying a linkable entity (ie Collection or DataSet)
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class AbstractLinkEntity extends AbstractEntity {

    protected AbstractLinkEntity(EntityType pEntityType) {
        this(null, pEntityType);
    }

    protected AbstractLinkEntity(Model pModel, EntityType pEntityType) {
        super(pModel, pEntityType);

    }

}
