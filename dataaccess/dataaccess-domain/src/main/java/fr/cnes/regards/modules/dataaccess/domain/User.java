/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain;

/**
 * mirror of a ProjectUser for data access purpose, only contains email
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
public class User {

    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String pEmail) {
        email = pEmail;
    }

}
