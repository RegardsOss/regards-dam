/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.domain;

import java.util.Set;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
public class AccessGroup {

    private String name;

    private Set<User> subscribers;

    private Set<AccessRight> accesRights;

}
