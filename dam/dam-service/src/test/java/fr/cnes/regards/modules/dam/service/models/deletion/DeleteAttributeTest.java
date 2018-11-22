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
package fr.cnes.regards.modules.dam.service.models.deletion;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.jpa.multitenant.test.AbstractMultitenantServiceTest;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.EntityOperationForbiddenException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.dam.domain.models.Model;
import fr.cnes.regards.modules.dam.domain.models.ModelAttrAssoc;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModel;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModelBuilder;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeType;
import fr.cnes.regards.modules.dam.service.entities.IDatasetService;
import fr.cnes.regards.modules.dam.service.models.IAttributeModelService;
import fr.cnes.regards.modules.dam.service.models.IModelAttrAssocService;
import fr.cnes.regards.modules.dam.service.models.IModelService;

/**
 * Test attribute model deletion restrictions
 * @author Marc Sordi
 */
@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=deletion" },
        locations = "classpath:es.properties")
@MultitenantTransactional
public class DeleteAttributeTest extends AbstractMultitenantServiceTest {

    @Autowired
    private IModelService modelService;

    @Autowired
    private IAttributeModelService attModelService;

    @Autowired
    private IModelAttrAssocService assocService;

    @Autowired
    private IDatasetService datasetService;

    private AttributeModel toBeDeleted;

    @Before
    public void setup() throws ModuleException {
        toBeDeleted = AttributeModelBuilder.build("TO_BE_DELETED", AttributeType.STRING, "del").get();
        attModelService.createAttribute(toBeDeleted);
    }

    /**
     * An attribute not linked to any model nor entity can be deleted
     * @throws ModuleException
     */
    @Test
    public void deleteUnlinked() throws ModuleException {
        attModelService.deleteAttribute(toBeDeleted.getId());
    }

    /**
     * An attribute linked to a non used model can be deleted
     * @throws ModuleException
     */
    @Test
    public void deleteWithLinkToModel() throws ModuleException {
        Model model = Model.build("DEL", "Model for deletion test", EntityType.COLLECTION);
        modelService.createModel(model);
        assocService.bindAttributeToModel(model.getName(), new ModelAttrAssoc(toBeDeleted, model));
        attModelService.deleteAttribute(toBeDeleted.getId());
    }

    /**
     * An attribute linked to a used model cannot be deleted
     * @throws ModuleException
     */
    @Test(expected = EntityOperationForbiddenException.class)
    public void deleteWithLinkToUsedModel() throws ModuleException {
        Model model = Model.build("DEL", "Model for deletion test", EntityType.DATASET);
        modelService.createModel(model);
        assocService.bindAttributeToModel(model.getName(), new ModelAttrAssoc(toBeDeleted, model));

        Dataset dataset = new Dataset(model, getDefaultTenant(), "DSDEL", "DS label");
        datasetService.create(dataset);

        attModelService.deleteAttribute(toBeDeleted.getId());
    }

    /**
     * An attribute linked to a datasource cannot be deleted
     * @throws ModuleException
     */
    @Test(expected = EntityOperationForbiddenException.class)
    public void deleteWithLinkToDatasource() throws ModuleException {
        Model datasetModel = Model.build("DSM", "Model for deletion test", EntityType.DATASET);
        modelService.createModel(datasetModel);

        Model dataModel = Model.build("DATA", "Model for deletion test", EntityType.DATA);
        modelService.createModel(dataModel);
        assocService.bindAttributeToModel(dataModel.getName(), new ModelAttrAssoc(toBeDeleted, dataModel));

        Dataset dataset = new Dataset(datasetModel, getDefaultTenant(), "DSDEL", "DS label");
        dataset.setDataModel(dataModel.getName());
        datasetService.create(dataset);

        attModelService.deleteAttribute(toBeDeleted.getId());
    }
}
