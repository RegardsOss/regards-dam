/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain.accessright;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;
import fr.cnes.regards.modules.entities.domain.DataSet;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Entity
@DiscriminatorValue("G")
public class GroupAccessRight extends AbstractAccessRight {

    @NotNull
    @ManyToOne
    private AccessGroup accessGroup;

    public GroupAccessRight(QualityFilter pQualityFilter, AccessLevel pAccessLevel, DataSet pDataset,
            AccessGroup pAccessGroup) {
        super(pQualityFilter, pAccessLevel, pDataset);
        accessGroup = pAccessGroup;
    }

    public AccessGroup getAccessGroup() {
        return accessGroup;
    }

    public void setAccessGroup(AccessGroup pAccessGroup) {
        accessGroup = pAccessGroup;
    }

}
