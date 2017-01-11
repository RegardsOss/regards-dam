/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain.accessright;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.modules.dataaccess.domain.accessgroup.User;
import fr.cnes.regards.modules.dataaccess.domain.jpa.converters.UserConverter;
import fr.cnes.regards.modules.entities.domain.DataSet;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@Entity
@DiscriminatorValue("U")
public class UserAccessRight extends AbstractAccessRight {

    @NotNull
    @Column(name = "user_email")
    @Convert(converter = UserConverter.class)
    private User user;

    public UserAccessRight(QualityFilter pQualityFilter, AccessLevel pAccessLevel, DataSet pDataset, User pUser) {
        super(pQualityFilter, pAccessLevel, pDataset);
        user = pUser;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User pUser) {
        user = pUser;
    }

}
