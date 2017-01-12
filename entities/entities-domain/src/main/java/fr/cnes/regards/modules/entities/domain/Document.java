/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

/**
 *
 * TODO: to be implemented, just created for the handling of links between entities
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Entity
@DiscriminatorValue("DOCUMENT")
public class Document extends AbstractDataEntity {

    /**
     * @param pId
     * @param pSidId
     * @param pModel
     * @param pFiles
     */
    public Document(Model pModel) {
        super(EntityType.DOCUMENT, pModel);
    }

    public Document() {
        this(null);
    }

}
