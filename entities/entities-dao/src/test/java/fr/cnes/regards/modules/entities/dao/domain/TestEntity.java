/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.dao.domain;

import javax.persistence.Entity;

import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Entity
public class TestEntity extends AbstractEntity {

    public TestEntity(Model pModel, UniformResourceName pIpId) {
        super(pModel, pIpId, EntityType.DATA);
    }

}
