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

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.modules.entities.dao.*;
import fr.cnes.regards.modules.entities.dao.deleted.IDeletedEntityRepository;
import fr.cnes.regards.modules.entities.domain.*;
import fr.cnes.regards.modules.models.service.IModelAttrAssocService;
import fr.cnes.regards.modules.models.service.IModelService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * Specific EntityService for documents
 * @author lmieulet
 */
@Service
@MultitenantTransactional
public class DocumentService extends AbstractEntityService<Document> implements IDocumentService {

    public DocumentService(IModelAttrAssocService pModelAttributeService,
                           IAbstractEntityRepository<AbstractEntity> pEntityRepository, IModelService pModelService,
                           IDeletedEntityRepository pDeletedEntityRepository, ICollectionRepository pCollectionRepository,
                           IDatasetRepository pDatasetRepository, IDocumentRepository pDocumentRepository,
                           EntityManager pEm, IPublisher pPublisher, IRuntimeTenantResolver runtimeTenantResolver,
                           IDescriptionFileRepository descriptionFileRepository) {
        super(pModelAttributeService, pEntityRepository, pModelService, pDeletedEntityRepository, pCollectionRepository,
                pDatasetRepository, pDocumentRepository, pEm, pPublisher, runtimeTenantResolver, descriptionFileRepository);
    }

    @Override
    public Document addFiles(Long pDocumentId, MultipartFile [] files) {
        return null;
    }

    @Override
    public void deleteFile(Long pDocumentId, Long pFileId) {

    }
}
