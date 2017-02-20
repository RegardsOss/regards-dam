/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.dao.ICollectionRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.service.identification.IdentificationService;
import fr.cnes.regards.modules.models.service.IModelAttributeService;
import fr.cnes.regards.modules.models.service.IModelService;

/**
 * @author lmieulet
 * @author Sylvain Vissiere-Guerinet
 */
@Service
public class CollectionsRequestService extends AbstractEntityService implements ICollectionsRequestService {

    // TODO: interactions with catalog
    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionsRequestService.class);

    /**
     * DAO autowired by Spring
     */
    private final ICollectionRepository collectionRepository;

    public CollectionsRequestService(ICollectionRepository pCollectionRepository,
            IAbstractEntityRepository<AbstractEntity> pAbstractEntityRepository,
            IdentificationService pIdentificationService, IModelAttributeService pModelAttributeService,
            IModelService pModelService) {
        super(pModelAttributeService, pAbstractEntityRepository, pModelService, pIdentificationService);
        collectionRepository = pCollectionRepository;
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

    @Override
    protected AbstractEntity doUpdate(AbstractEntity pEntity) {
        // nothing specific to update
        return pEntity;
    }

    @Override
    protected AbstractEntity doCheck(AbstractEntity pEntity) throws ModuleException {
        // nothing specific to check
        return pEntity;
    }

    @Override
    protected <T extends AbstractEntity> T doCreate(T pNewEntity) throws ModuleException {
        // nothing specific to do
        return pNewEntity;
    }

}
