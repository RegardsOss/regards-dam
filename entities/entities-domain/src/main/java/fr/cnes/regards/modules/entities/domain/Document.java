/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain;

import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

/**
 *
 * TODO: to be implemented, just created for the handling of links between entities
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
public class Document extends DataEntity {

    public Document(Model pModel) {
        super(EntityType.DOCUMENT, pModel);
    }

}
