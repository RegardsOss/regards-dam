/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import fr.cnes.regards.modules.entities.urn.UniformResourceName;
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

    public Document(Model pModel, UniformResourceName pIpId) {
        super(pModel, pIpId, EntityType.DOCUMENT);
    }

    public Document() {
        this(null, null);
    }

}
