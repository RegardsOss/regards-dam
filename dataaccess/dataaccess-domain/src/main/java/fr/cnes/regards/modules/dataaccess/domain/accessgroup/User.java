/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain.accessgroup;

import javax.validation.constraints.NotNull;

import fr.cnes.regards.modules.dataaccess.domain.accessright.UserAccessRight;

/**
 * Mirror of a ProjectUser for data access purpose, only contains email. Access rights are attribited to a user thanks
 * to {@link UserAccessRight}
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
public class User {

    @NotNull
    private String email;

    /**
     * @param pDbData
     */
    public User(String pEmail) {
        email = pEmail;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String pEmail) {
        email = pEmail;
    }

    @Override
    public boolean equals(Object pOther) {
        return (pOther instanceof User) && ((User) pOther).email.equals(email);
    }

    @Override
    public int hashCode() {
        return email.hashCode();
    }

}
