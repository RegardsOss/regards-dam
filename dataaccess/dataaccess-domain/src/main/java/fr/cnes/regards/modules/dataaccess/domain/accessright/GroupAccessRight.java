/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain.accessright;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;
import fr.cnes.regards.modules.entities.domain.Dataset;

/**
 * Group access right
 *
 * @author Sylvain Vissiere-Guerinet
 */
@Entity
@DiscriminatorValue("G")
public class GroupAccessRight extends AbstractAccessRight {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "access_group_id", foreignKey = @ForeignKey(name = "fk_access_right_access_group_id"),
            updatable = false)
    private AccessGroup accessGroup;

    public GroupAccessRight(QualityFilter pQualityFilter, AccessLevel pAccessLevel, Dataset pDataset,
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
