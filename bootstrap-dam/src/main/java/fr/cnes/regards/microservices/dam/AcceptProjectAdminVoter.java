/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.microservices.dam;

import java.util.Collection;
import java.util.List;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import fr.cnes.regards.framework.security.utils.endpoint.IProjectAdminAccessVoter;
import fr.cnes.regards.framework.security.utils.endpoint.RoleAuthority;
import fr.cnes.regards.framework.security.utils.jwt.JWTAuthentication;

/**
 *
 * MethodAuthorization voter to accept access to all endpoints for project administrator.
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
public class AcceptProjectAdminVoter implements IProjectAdminAccessVoter {

    @Override
    public boolean supports(ConfigAttribute pAttribute) {
        return true;
    }

    @Override
    public boolean supports(Class<?> pClazz) {
        return true;
    }

    @Override
    public int vote(Authentication pAuthentication, Object pObject, Collection<ConfigAttribute> pAttributes) {

        final JWTAuthentication authentication = (JWTAuthentication) pAuthentication;

        // If authenticated user is one of the project admins allow all.
        @SuppressWarnings("unchecked")
        final List<RoleAuthority> roles = (List<RoleAuthority>) authentication.getAuthorities();
        if (RoleAuthority.isProjectAdminRole(roles.get(0).getAuthority())) {
            return ACCESS_GRANTED;
        }

        return ACCESS_DENIED;
    }

}
