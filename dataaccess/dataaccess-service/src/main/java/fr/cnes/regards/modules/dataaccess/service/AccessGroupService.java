/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import fr.cnes.regards.modules.dataaccess.dao.AccessGroupDao;
import fr.cnes.regards.modules.dataaccess.domain.AccessGroup;

/**
 *
 * TODO Description
 *
 * @author TODO
 *
 */
@Service
public class AccessGroupService {

    private final AccessGroupDao accessGroupDao;

    public AccessGroupService(AccessGroupDao pAccessGroupDao) {
        accessGroupDao = pAccessGroupDao;
    }

    /**
     * @param pPageable
     * @return
     */
    public Page<AccessGroup> retrieveAccessGroups(Pageable pPageable) {
        return accessGroupDao.findAll(pPageable);
    }

    /**
     * @param pToBeCreated
     * @return
     */
    public AccessGroup createAccessGroup(AccessGroup pToBeCreated) {
        return accessGroupDao.save(pToBeCreated);
    }

}