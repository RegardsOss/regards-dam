/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import java.util.List;

import javax.persistence.EntityManager;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.dao.ICollectionRepository;
import fr.cnes.regards.modules.entities.dao.IDatasetRepository;
import fr.cnes.regards.modules.entities.dao.deleted.IDeletedEntityRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.service.IModelAttrAssocService;
import fr.cnes.regards.modules.models.service.IModelService;

/**
 * Entity Service to be removed as soon as possible or sooner
 *
 * @author Marc Sordi
 * @author Sylvain Vissiere-Guerinet
 * @author oroussel
 */
@Deprecated
public class EntityService extends AbstractEntityService<AbstractEntity> implements IEntityService<AbstractEntity> {

    public EntityService(IModelAttrAssocService pModelAttributeService,
            IAbstractEntityRepository<AbstractEntity> pEntityRepository, IModelService pModelService,
            IDeletedEntityRepository pDeletedEntityRepository, ICollectionRepository pCollectionRepository,
            IDatasetRepository pDatasetRepository, IAbstractEntityRepository<AbstractEntity> pRepository,
            EntityManager pEm, IPublisher pPublisher) {
        super(pModelAttributeService, pEntityRepository, pModelService, pDeletedEntityRepository, pCollectionRepository,
              pDatasetRepository, pRepository, pEm, pPublisher);

    }

    @Override
    public AbstractEntity loadWithRelations(UniformResourceName pIpId) {
        return null;
    }

    @Override
    public List<AbstractEntity> loadAllWithRelations(UniformResourceName... pIpIds) {
        return null;
    }

}