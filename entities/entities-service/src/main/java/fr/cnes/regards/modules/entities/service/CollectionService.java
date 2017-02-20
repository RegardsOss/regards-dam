/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import java.util.List;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.dao.ICollectionRepository;
import fr.cnes.regards.modules.entities.dao.IDataSetRepository;
import fr.cnes.regards.modules.entities.dao.deleted.IDeletedEntityRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.models.service.IModelAttributeService;
import fr.cnes.regards.modules.models.service.IModelService;

/**
 * @author lmieulet
 * @author Sylvain Vissiere-Guerinet
 */
@Service
public class CollectionService extends AbstractEntityService implements ICollectionService {

    // TODO: interactions with catalog
    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionService.class);

    public CollectionService(ICollectionRepository pCollectionRepository,
            IAbstractEntityRepository<AbstractEntity> pAbstractEntityRepository,
            IModelAttributeService pModelAttributeService, IModelService pModelService,
            IDeletedEntityRepository deletedEntityRepository, IDataSetRepository pDatasetRepository,
            EntityManager pEm) {
        super(pModelAttributeService, pAbstractEntityRepository, pModelService, deletedEntityRepository,
              pCollectionRepository, pDatasetRepository, pEm);
    }

    @Override
    public Collection retrieveCollectionById(Long pCollectionId) {
        return collectionRepository.findOne(pCollectionId);
    }

    @Override
    public List<Collection> retrieveCollectionList() {
        return collectionRepository.findAll();
    }

    @Override
    public Collection retrieveCollectionByIpId(String pCollectionIpId) {
        return collectionRepository.findOneByIpId(pCollectionIpId);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected AbstractEntity beforeUpdate(AbstractEntity pEntity) {
        // nothing specific to do
        return pEntity;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected AbstractEntity doCheck(AbstractEntity pEntity) throws ModuleException {
        // nothing specific to check
        return pEntity;
    }

    @Override
    protected <T extends AbstractEntity> T beforeCreate(T pNewEntity) throws ModuleException {
        // nothing specific to do
        return pNewEntity;
    }

}
