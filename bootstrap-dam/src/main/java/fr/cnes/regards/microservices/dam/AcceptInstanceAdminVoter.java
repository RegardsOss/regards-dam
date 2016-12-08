/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.microservices.dam;

import java.util.Collection;
import java.util.List;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import fr.cnes.regards.framework.security.utils.endpoint.IInstanceAdminAccessVoter;
import fr.cnes.regards.framework.security.utils.endpoint.RoleAuthority;
import fr.cnes.regards.framework.security.utils.jwt.JWTAuthentication;

/**
 *
 * Class AcceptInstanceAdminVoter
 *
 * MethodAuthorization voter to accept access to all endpoints for instance administrator.
 *
 * @author Sébastien Binda
 * @since 1.0-SNAPSHOT
 */
public class AcceptInstanceAdminVoter implements IInstanceAdminAccessVoter {

    /**
     * Instance administrator user login
     */
    private final String instanceAdminUserLogin;

    public AcceptInstanceAdminVoter(final String pInstanceAdminUserLogin) {
        super();
        instanceAdminUserLogin = pInstanceAdminUserLogin;
    }

    @Override
    public int vote(final Authentication pAuthentication, final Object pObject,
            final Collection<ConfigAttribute> pAttributes) {
        // Default behavior : deny access
        int access = ACCESS_DENIED;

        // Get authorized ip associated to given role
        final JWTAuthentication authentication = (JWTAuthentication) pAuthentication;

        // If authenticated user is the instance admin user allow all.
        @SuppressWarnings("unchecked")
        final List<RoleAuthority> roles = (List<RoleAuthority>) authentication.getAuthorities();
        if (authentication.getUser().getName().equals(instanceAdminUserLogin)
                && RoleAuthority.isInstanceAdminRole(roles.get(0).getAuthority())) {
            access = ACCESS_GRANTED;
        }

        return access;
    }

    @Override
    public boolean supports(final ConfigAttribute pArg0) {
        return true;
    }

    @Override
    public boolean supports(final Class<?> pArg0) {
        return true;
    }

}
