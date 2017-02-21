/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * Class identifying a linkable entity (i.e. Collection or Dataset)
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class AbstractLinkEntity extends AbstractEntity {

    @Embedded
    private DescriptionFile descriptionFile;

    protected AbstractLinkEntity() {
        this(null, null, null);
    }

    protected AbstractLinkEntity(Model pModel, UniformResourceName pIpId, String pLabel) {
        super(pModel, pIpId, pLabel);
    }

    public DescriptionFile getDescriptionFile() {
        return descriptionFile;
    }

    public void setDescriptionFile(DescriptionFile pDescriptionFile) {
        descriptionFile = pDescriptionFile;
    }

}
