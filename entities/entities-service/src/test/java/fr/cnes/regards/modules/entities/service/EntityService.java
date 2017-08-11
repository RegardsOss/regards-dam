/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.entities.service;

import javax.persistence.EntityManager;
import java.util.List;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.dao.ICollectionRepository;
import fr.cnes.regards.modules.entities.dao.IDatasetRepository;
import fr.cnes.regards.modules.entities.dao.IDescriptionFileRepository;
import fr.cnes.regards.modules.entities.dao.deleted.IDeletedEntityRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.framework.urn.UniformResourceName;
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
            EntityManager pEm, IPublisher pPublisher, IRuntimeTenantResolver runtimeTenantResolver, IDescriptionFileRepository descriptionFileRepository) {
        super(pModelAttributeService, pEntityRepository, pModelService, pDeletedEntityRepository, pCollectionRepository,
              pDatasetRepository, pRepository, pEm, pPublisher, runtimeTenantResolver, descriptionFileRepository);

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