/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.identification;

import java.util.UUID;

import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.multitenant.IThreadTenantResolver;
import fr.cnes.regards.modules.entities.urn.OAISIdentifier;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.domain.EntityType;

/**
 *
 * Each entity has to be identified as an URN. This service provides tools to compute this unique resource name.
 *
 * @author Marc Sordi
 *
 */
@Service
public class IdentificationService {

    /**
     * Resolve request tenant
     */
    private final IThreadTenantResolver threadTenantResolver;

    public IdentificationService(IThreadTenantResolver pThreadTenantResolver) {
        this.threadTenantResolver = pThreadTenantResolver;
    }

    public UniformResourceName getRandomUrn(OAISIdentifier pOAISId, EntityType pEntityType) {
        return new UniformResourceName(pOAISId, pEntityType, threadTenantResolver.getTenant(), UUID.randomUUID(), 1);
    }
}
