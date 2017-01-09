package fr.cnes.regards.modules.entities.domain;

import javax.persistence.Entity;

import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * Class identifying a linkable entity (i.e. Collection or DataSet)
 */
@Entity
public class AbstractLinkEntity extends AbstractEntity {

    protected AbstractLinkEntity(EntityType pEntityType) {
        this(null, null, pEntityType);
    }

    protected AbstractLinkEntity(Model pModel, UniformResourceName pIpId, EntityType pEntityType) {
        super(pModel, pIpId, pEntityType);

    }

}
