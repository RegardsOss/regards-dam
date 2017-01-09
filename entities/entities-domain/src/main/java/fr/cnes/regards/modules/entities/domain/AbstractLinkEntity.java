package fr.cnes.regards.modules.entities.domain;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * Class identifying a linkable entity (i.e. Collection or DataSet)
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class AbstractLinkEntity extends AbstractEntity {

    protected AbstractLinkEntity() {
        this(null, null, null);
    }

    protected AbstractLinkEntity(Model pModel, UniformResourceName pIpId, String pLabel) {
        super(pModel, pIpId, pLabel);
    }
}
