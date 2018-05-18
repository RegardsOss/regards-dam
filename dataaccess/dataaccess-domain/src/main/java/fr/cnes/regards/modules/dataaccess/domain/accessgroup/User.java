/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.dataaccess.domain.accessgroup;

import javax.validation.constraints.NotNull;

/**
 * Mirror of a ProjectUser for data access purpose, only contains email. Access rights are attribited to a user thanks
 * to {@link UserAccessRight}
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
public class User {

    /**
     * The email
     */
    @NotNull
    private String email;

    public User() { // NOSONAR - Deserialization
    }

    /**
     * Contructor setting the parameter as attribute
     * @param pEmail
     */
    public User(String pEmail) {
        email = pEmail;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Set the email
     * @param pEmail
     */
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
