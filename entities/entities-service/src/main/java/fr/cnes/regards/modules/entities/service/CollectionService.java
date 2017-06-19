/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.dao.ICollectionRepository;
import fr.cnes.regards.modules.entities.dao.IDatasetRepository;
import fr.cnes.regards.modules.entities.dao.IDescriptionFileRepository;
import fr.cnes.regards.modules.entities.dao.deleted.IDeletedEntityRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.DescriptionFile;
import fr.cnes.regards.modules.models.service.IModelAttrAssocService;
import fr.cnes.regards.modules.models.service.IModelService;

/**
 * Specific EntityService for collections
 * @author Sylvain Vissiere-Guerinet
 * @author oroussel
 */
@Service
public class CollectionService extends AbstractEntityService<Collection> implements ICollectionService {

    public CollectionService(IModelAttrAssocService pModelAttributeService,
            IAbstractEntityRepository<AbstractEntity> pEntityRepository, IModelService pModelService,
            IDeletedEntityRepository pDeletedEntityRepository, ICollectionRepository pCollectionRepository,
            IDatasetRepository pDatasetRepository, IAbstractEntityRepository<Collection> pRepository, EntityManager pEm,
            IPublisher pPublisher, IRuntimeTenantResolver runtimeTenantResolver, IDescriptionFileRepository descriptionFileRepository) {
        super(pModelAttributeService, pEntityRepository, pModelService, pDeletedEntityRepository, pCollectionRepository,
              pDatasetRepository, pRepository, pEm, pPublisher, runtimeTenantResolver, descriptionFileRepository);
    }

    @Override
    public DescriptionFile retrieveDescription(Long collectionId) throws EntityNotFoundException {
        Collection col=collectionRepository.findOneWithDescriptionFile(collectionId);
        if(col==null) {
            throw new EntityNotFoundException(collectionId, Dataset.class);
        }
        DescriptionFile desc=col.getDescriptionFile();
        return desc;
    }

    @Override
    public void removeDescription(Long collectionId) throws EntityNotFoundException {
        Collection col=collectionRepository.findOneWithDescriptionFile(collectionId);
        if(col==null) {
            throw new EntityNotFoundException(collectionId, Dataset.class);
        }
        DescriptionFile desc=col.getDescriptionFile();
        col.setDescriptionFile(null);
        descriptionFileRepository.delete(desc);
    }
}
