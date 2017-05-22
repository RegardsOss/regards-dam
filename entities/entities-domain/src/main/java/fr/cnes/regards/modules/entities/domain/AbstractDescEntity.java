/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import javax.persistence.*;

import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * Class identifying a descriptable entity
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class AbstractDescEntity extends AbstractEntity {

    /**
     * Description file
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private DescriptionFile descriptionFile;

    protected AbstractDescEntity() {
        this(null, null, null);
    }

    protected AbstractDescEntity(Model pModel, UniformResourceName pIpId, String pLabel) {
        super(pModel, pIpId, pLabel);
    }

    public DescriptionFile getDescriptionFile() {
        return descriptionFile;
    }

    public void setDescriptionFile(DescriptionFile pDescriptionFile) {
        descriptionFile = pDescriptionFile;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

}
