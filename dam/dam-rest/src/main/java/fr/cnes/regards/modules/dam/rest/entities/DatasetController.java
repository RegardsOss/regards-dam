/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.rest.entities;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.hateoas.LinkRels;
import fr.cnes.regards.framework.hateoas.MethodParamFactory;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.module.rest.utils.Validity;
import fr.cnes.regards.framework.oais.urn.OaisUniformResourceName;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.dam.rest.entities.dto.DatasetDataAttributesRequestBody;
import fr.cnes.regards.modules.dam.rest.entities.exception.AssociatedAccessRightExistsException;
import fr.cnes.regards.modules.dam.service.entities.IDatasetService;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterionVisitor;
import fr.cnes.regards.modules.model.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.model.service.IModelAttrAssocService;
import fr.cnes.regards.modules.model.service.validation.ValidationMode;
import fr.cnes.regards.modules.opensearch.service.IOpenSearchService;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchParseException;

/**
 * Rest controller managing {@link Dataset}s
 * @author Sylvain Vissiere-Guerinet
 * @author Xavier-Alexandre Brochard
 */
@RestController
@RequestMapping(value = DatasetController.TYPE_MAPPING)
public class DatasetController implements IResourceController<Dataset> {

    /**
     * Endpoint for datasets
     */
    public static final String TYPE_MAPPING = "/datasets";

    /**
     * Endpoint for data attributes
     */
    public static final String DATASET_DATA_ATTRIBUTES_PATH = "/data/attributes";

    /**
     * Endpoint for dataset attributes
     */
    public static final String DATASET_ATTRIBUTES_PATH = "/attributes";

    /**
     * Endpoint for a specific dataset
     */
    public static final String DATASET_ID_PATH = "/{dataset_id}";

    public static final String DATASET_IP_ID_PATH = "/ipId/{dataset_ipId}";

    /**
     * Endpoint to associate dataset
     */
    public static final String DATASET_ID_ASSOCIATE_PATH = DATASET_ID_PATH + "/associate";

    /**
     * Endpoint to dissociate dataset
     */
    public static final String DATASET_ID_DISSOCIATE_PATH = DATASET_ID_PATH + "/dissociate";

    /**
     * Controller path for subsetting clause validation
     */
    public static final String DATA_SUB_SETTING_VALIDATION = "/isValidSubsetting";

    /**
     * Controller path to get all attributes associated to dataset
     */
    public static final String ENTITY_ASSOCS_MAPPING = "{datasetUrn}/assocs";

    private static final Logger LOG = LoggerFactory.getLogger(DatasetController.class);

    /**
     * Service handling hypermedia resources
     */
    @Autowired
    private IResourceService resourceService;

    /**
     * Service handling {@link Dataset}
     */
    @Autowired
    private IDatasetService service;

    /**
     * Model attribute association service
     */
    @Autowired
    private IModelAttrAssocService modelAttrAssocService;

    /**
     * Service parsing/converting OpenSearch string requests to {@link ICriterion}
     */
    @Autowired
    private IOpenSearchService openSearchService;

    @ResourceAccess(description = "Retrieve all attributes related to given entity")
    @RequestMapping(path = ENTITY_ASSOCS_MAPPING, method = RequestMethod.GET)
    public ResponseEntity<Collection<ModelAttrAssoc>> getModelAttrAssocsForDataInDataset(
            @RequestParam(name = "datasetUrn") OaisUniformResourceName datasetUrn) throws ModuleException {
        Dataset dataset = service.load(datasetUrn);
        Collection<ModelAttrAssoc> assocs = modelAttrAssocService.getModelAttrAssocs(dataset.getDataModel());
        return ResponseEntity.ok(assocs);
    }

    /**
     * Create a dataset
     * @param dataset the dataset to create
     * @param result for validation of entites' properties
     * @return the created dataset wrapped in an HTTP response
     * @throws ModuleException
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResourceAccess(description = "create and send the dataset")
    public ResponseEntity<EntityModel<Dataset>> createDataset(@Valid @RequestBody Dataset dataset, BindingResult result)
            throws ModuleException, IOException {
        service.checkAndOrSetModel(dataset);
        // Validate dynamic model
        service.validate(dataset, result, ValidationMode.CREATION);

        final Dataset created = service.create(dataset);
        return new ResponseEntity<>(toResource(created), HttpStatus.CREATED);
    }

    /**
     * Retrieve datasets
     * @param label
     * @param pageable the page
     * @param assembler the dataset resources assembler
     * @return the page of dataset wrapped in an HTTP response
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResourceAccess(description = "endpoint to retrieve the list of all datasets")
    public ResponseEntity<PagedModel<EntityModel<Dataset>>> retrieveDatasets(
            @RequestParam(name = "label", required = false) String label,
            @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            PagedResourcesAssembler<Dataset> assembler) {
        Page<Dataset> datasets = service.search(label, pageable);
        PagedModel<EntityModel<Dataset>> resources = toPagedResources(datasets, assembler);
        return new ResponseEntity<>(resources, HttpStatus.OK);

    }

    /**
     * Retrieve the dataset of passed id
     * @param datasetId the id of the dataset
     * @return the dataset of passed id
     * @throws ModuleException
     */
    @RequestMapping(method = RequestMethod.GET, value = DATASET_ID_PATH)
    @ResourceAccess(description = "Retrieves a dataset")
    public ResponseEntity<EntityModel<Dataset>> retrieveDataset(@PathVariable("dataset_id") final Long datasetId)
            throws ModuleException {
        Dataset dataset = service.load(datasetId);
        EntityModel<Dataset> resource = toResource(dataset);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /**
     * Retrieve dataset from its IP_ID
     * @param datasetIpId ip_id of the dataset
     * @return dataset lazily loaded
     * @throws ModuleException
     */
    @RequestMapping(method = RequestMethod.GET, value = DATASET_IP_ID_PATH)
    @ResourceAccess(description = "Retrieves a dataset")
    public ResponseEntity<Dataset> retrieveDataset(@PathVariable("dataset_ipId") final String datasetIpId)
            throws ModuleException {
        Dataset dataset = service.load(OaisUniformResourceName.fromString(datasetIpId));
        return new ResponseEntity<>(dataset, HttpStatus.OK);
    }

    /**
     * Delete dataset of given id
     * @param datasetId the id of the dataset to delete
     * @return a no content HTTP response
     * @throws ModuleException
     */
    @RequestMapping(method = RequestMethod.DELETE, value = DATASET_ID_PATH)
    @ResourceAccess(description = "Deletes a dataset")
    public ResponseEntity<Void> deleteDataset(@PathVariable("dataset_id") final Long datasetId) throws ModuleException {
        try {
            service.delete(datasetId);
        } catch (final RuntimeException e) {
            // Ugliest method to manage constraints on entites which are associated to this datasource but because
            // of the overuse of plugins everywhere a billion of dependencies exist with some cyclics if we try to
            // do things cleanly so let's be pigs and do shit without any problems....
            // And ugliest of the ugliest, this exception is thrown at transaction commit that's why it is done here and
            // not into service
            if (e.getMessage().contains("fk_access_right_access_dataset_id")) {
                throw new AssociatedAccessRightExistsException();
            }
            throw e;
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Update dataset of given id
     * @param datasetId the id of the dataset to update
     * @param dataset the new values of the dataset
     * @param result for validation of entites' properties
     * @return the updated dataset wrapped in an HTTP response
     * @throws ModuleException
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.PUT, value = DATASET_ID_PATH)
    @ResourceAccess(description = "Update a dataset")
    public ResponseEntity<EntityModel<Dataset>> updateDataset(@PathVariable("dataset_id") Long datasetId,
            @Valid @RequestBody Dataset dataset, BindingResult result) throws ModuleException, IOException {
        service.checkAndOrSetModel(dataset);
        // Validate dynamic model
        service.validate(dataset, result, ValidationMode.UPDATE);

        Dataset dataSet = service.update(datasetId, dataset);
        EntityModel<Dataset> resource = toResource(dataSet);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /**
     * Entry point to handle dissociation of {@link Dataset} specified by its id to other entities
     * @param datasetId {@link Dataset} id
     * @param toBeDissociated entity to dissociate
     * @return {@link Dataset} as a {@link EntityModel}
     * @throws ModuleException if error occurs
     */
    @RequestMapping(method = RequestMethod.PUT, value = DATASET_ID_DISSOCIATE_PATH)
    @ResourceAccess(description = "Dissociate a list of entities from a dataset")
    public ResponseEntity<Void> dissociate(@PathVariable("dataset_id") final Long datasetId,
            @Valid @RequestBody final Set<String> toBeDissociated) throws ModuleException {
        service.dissociate(datasetId, toBeDissociated);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Entry point to handle association of {@link Dataset} specified by its id to other entities
     * @param datasetId {@link Dataset} id
     * @param toBeAssociatedWith entities to be associated
     * @return {@link Dataset} as a {@link EntityModel}
     * @throws ModuleException if error occurs
     */
    @RequestMapping(method = RequestMethod.PUT, value = DATASET_ID_ASSOCIATE_PATH)
    @ResourceAccess(description = "associate the list of entities to the dataset")
    public ResponseEntity<Void> associate(@PathVariable("dataset_id") final Long datasetId,
            @Valid @RequestBody final Set<String> toBeAssociatedWith) throws ModuleException {
        service.associate(datasetId, toBeAssociatedWith);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Retrieve data attributes of datasets of given URNs and given model name
     * @param requestBody {@link DatasetDataAttributesRequestBody}
     * @param pageable the page
     * @param assembler the resources assembler
     * @return the page of attribute models wrapped in an HTTP response
     * @throws ModuleException
     */
    @RequestMapping(method = RequestMethod.POST, value = DATASET_DATA_ATTRIBUTES_PATH)
    @ResourceAccess(description = "Retrieves data attributes of given datasets")
    public ResponseEntity<PagedModel<EntityModel<AttributeModel>>> retrieveDataAttributes(
            @RequestBody DatasetDataAttributesRequestBody requestBody, final Pageable pageable,
            final PagedResourcesAssembler<AttributeModel> assembler) throws ModuleException {
        Page<AttributeModel> result = service.getDataAttributeModels(requestBody.getDatasetIds(),
                                                                     requestBody.getModelNames(), pageable);
        return new ResponseEntity<>(assembler.toModel(result), HttpStatus.OK);
    }

    /**
     * Retrieve data attributes of datasets of given URNs and given model name
     * @param pageable the page
     * @param assembler the resources assembler
     * @return the page of attribute models wrapped in an HTTP response
     * @throws ModuleException
     */
    @RequestMapping(method = RequestMethod.POST, value = DATASET_ATTRIBUTES_PATH)
    @ResourceAccess(description = "Retrieves data attributes of given datasets")
    public ResponseEntity<PagedModel<EntityModel<AttributeModel>>> retrieveAttributes(
            @RequestBody DatasetDataAttributesRequestBody body, final Pageable pageable,
            final PagedResourcesAssembler<AttributeModel> assembler) throws ModuleException {
        Page<AttributeModel> result = service.getAttributeModels(body.getDatasetIds(), body.getModelNames(), pageable);
        return new ResponseEntity<>(assembler.toModel(result), HttpStatus.OK);
    }

    /**
     * Validate an open search query for the given data model, represented by its id
     * @param dataModelName
     * @param query {@link Query}
     * @return whether the query is valid or not
     * @throws ModuleException
     */
    @RequestMapping(method = RequestMethod.POST, value = DATA_SUB_SETTING_VALIDATION)
    @ResourceAccess(description = "Validate if a subsetting is correct and coherent regarding a data model")
    public ResponseEntity<Validity> validateSubSettingClause(@RequestParam("dataModelName") String dataModelName,
            @RequestBody Query query) throws ModuleException {
        // we have to add "q=" to be able to parse the query
        try {
            ICriterion criterionToBeVisited = openSearchService.parse("q=" + query.getQuery());
            ICriterionVisitor<Boolean> visitor = service.getSubsettingCoherenceVisitor(dataModelName);
            return ResponseEntity.ok(new Validity(criterionToBeVisited.accept(visitor)));
        } catch (OpenSearchParseException e) {
            LOG.error(e.getMessage(), e);
            return ResponseEntity.ok(new Validity(false));
        }
    }

    @Override
    public EntityModel<Dataset> toResource(final Dataset element, final Object... extras) {
        final EntityModel<Dataset> resource = resourceService.toResource(element);
        resourceService.addLink(resource, this.getClass(), "retrieveDataset", LinkRels.SELF,
                                MethodParamFactory.build(Long.class, element.getId()));
        resourceService.addLink(resource, this.getClass(), "retrieveDatasets", LinkRels.LIST,
                                MethodParamFactory.build(String.class, element.getLabel()),
                                MethodParamFactory.build(Pageable.class),
                                MethodParamFactory.build(PagedResourcesAssembler.class));
        resourceService.addLink(resource, this.getClass(), "deleteDataset", LinkRels.DELETE,
                                MethodParamFactory.build(Long.class, element.getId()));
        resourceService.addLink(resource, this.getClass(), "updateDataset", LinkRels.UPDATE,
                                MethodParamFactory.build(Long.class, element.getId()),
                                MethodParamFactory.build(Dataset.class), MethodParamFactory.build(BindingResult.class));
        resourceService.addLink(resource, this.getClass(), "dissociate", LinkRelation.of("dissociate"),
                                MethodParamFactory.build(Long.class, element.getId()),
                                MethodParamFactory.build(Set.class));
        resourceService.addLink(resource, this.getClass(), "associate", LinkRelation.of("associate"),
                                MethodParamFactory.build(Long.class, element.getId()),
                                MethodParamFactory.build(Set.class));
        return resource;
    }

    /**
     * Open search query POJO sent by our front to be validated
     */
    public static class Query {

        /**
         * The query
         */
        private String query;

        /**
         * Default constructor for (de)serialization
         */
        @SuppressWarnings("unused")
        private Query() {
        }

        /**
         * Constructor setting the parameter as attribute
         * @param query
         */
        public Query(String query) {
            this.query = query;
        }

        /**
         * @return the query
         */
        public String getQuery() {
            return query;
        }

        /**
         * Set the query
         * @param query
         */
        public void setQuery(String query) {
            this.query = query;
        }
    }

}
