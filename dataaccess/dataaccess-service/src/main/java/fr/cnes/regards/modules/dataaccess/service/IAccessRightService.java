package fr.cnes.regards.modules.dataaccess.service;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.modules.dataaccess.domain.accessright.AccessLevel;
import fr.cnes.regards.modules.dataaccess.domain.accessright.AccessRight;

/**
 * Created by oroussel on 03/07/17.
 */
public interface IAccessRightService {

    Page<AccessRight> retrieveAccessRights(String pAccessGroupName, UniformResourceName pDatasetIpId,
            Pageable pPageable) throws EntityNotFoundException;

    Map<String, AccessLevel> retrieveGroupAccessLevelMap(UniformResourceName datasetIpId);

    AccessRight createAccessRight(AccessRight pAccessRight) throws ModuleException;

    AccessRight retrieveAccessRight(Long pId) throws EntityNotFoundException;

    AccessRight updateAccessRight(Long pId, AccessRight pToBe) throws ModuleException;

    void deleteAccessRight(Long pId) throws ModuleException;
}
