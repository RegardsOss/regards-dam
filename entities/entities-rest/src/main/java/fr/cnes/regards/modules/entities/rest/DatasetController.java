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
package fr.cnes.regards.modules.entities.rest;

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.hateoas.LinkRels;
import fr.cnes.regards.framework.hateoas.MethodParamFactory;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.module.rest.utils.Validity;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.DescriptionFile;
import fr.cnes.regards.modules.entities.service.IDatasetService;
import fr.cnes.regards.modules.entities.service.visitor.SubsettingCoherenceVisitor;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.opensearch.service.IOpenSearchService;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;

/**
 * Rest controller managing {@link Dataset}s
 *
 * @author Sylvain Vissiere-Guerinet
 * @author Xavier-Alexandre Brochard
 */
@RestController
@RequestMapping(value = DatasetController.DATASET_PATH)
public class DatasetController implements IResourceController<Dataset> {

    /**
     * Endpoint for datasets
     */
    public static final String DATASET_PATH = "/datasets";

    /**
     * Endpoint for data attributes
     */
    public static final String DATASET_DATA_ATTRIBUTES_PATH = "/data/attributes";

    /**
     * Endpoint for a specific dataset
     */
    public static final String DATASET_ID_PATH = "/{dataset_id}";

    /**
     * Endpoint to associate dataset
     */
    public static final String DATASET_ID_ASSOCIATE_PATH = DATASET_ID_PATH + "/associate";

    /**
     * Endpoint to dissociate dataset
     */
    public static final String DATASET_ID_DISSOCIATE_PATH = DATASET_ID_PATH + "/dissociate";

    public static final String DATASET_IPID_PATH_FILE = "/{dataset_ipId}/file";

    public static final String DATA_SUB_SETTING_VALIDATION = "/isValidSubsetting";

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
     * Service parsing/converting OpenSearch string requests to {@link ICriterion}
     */
    @Autowired
    private IOpenSearchService openSearchService;

    /**
     * Create a dataset
     *
     * @param pDataset
     *            the dataset to create
     * @param descriptionFile
     *            the description file
     * @param pResult
     *            for validation of entites' properties
     * @return the created dataset wrapped in an HTTP response
     * @throws ModuleException
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResourceAccess(description = "create and send the dataset")
    public ResponseEntity<Resource<Dataset>> createDataset(@Valid @RequestPart("dataset") final Dataset pDataset,
            @RequestPart(value = "file", required = false) final MultipartFile descriptionFile,
            final BindingResult pResult) throws ModuleException, IOException {

        // Validate dynamic model
        service.validate(pDataset, pResult, false);

        final Dataset created = service.create(pDataset, descriptionFile);
        return new ResponseEntity<>(toResource(created), HttpStatus.CREATED);
    }

    /**
     * Retrieve datasets
     *
     * @param pPageable
     *            the page
     * @param pAssembler
     *            the dataset resources assembler
     * @return the page of dataset wrapped in an HTTP response
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResourceAccess(description = "endpoint to retrieve the list of all datasets")
    public ResponseEntity<PagedResources<Resource<Dataset>>> retrieveDatasets(final Pageable pPageable,
            final PagedResourcesAssembler<Dataset> pAssembler) {

        final Page<Dataset> datasets = service.findAll(pPageable);
        final PagedResources<Resource<Dataset>> resources = toPagedResources(datasets, pAssembler);
        return new ResponseEntity<>(resources, HttpStatus.OK);

    }

    /**
     * Retrieve the dataset of passed id
     *
     * @param pDatasetId
     *            the id of the dataset
     * @return the dataset of passed id
     * @throws EntityNotFoundException
     *             Thrown when no dataset with given id could be found
     */
    @RequestMapping(method = RequestMethod.GET, value = DATASET_ID_PATH)
    @ResourceAccess(description = "Retrieves a dataset")
    public ResponseEntity<Resource<Dataset>> retrieveDataset(@PathVariable("dataset_id") final Long pDatasetId)
            throws EntityNotFoundException {
        final Dataset dataset = service.load(pDatasetId);
        if (dataset == null) {
            throw new EntityNotFoundException(pDatasetId);
        }
        final Resource<Dataset> resource = toResource(dataset);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = DATASET_IPID_PATH_FILE)
    @ResourceAccess(description = "remove a dataset description file content")
    public ResponseEntity<Void> removeDatasetDescription(@PathVariable("dataset_ipId") String datasetIpId)
            throws EntityNotFoundException {
        service.removeDescription(UniformResourceName.fromString(datasetIpId));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Retrieves a dataset description - only for rs-catalog because permissions not checked right here
     */
    @RequestMapping(method = RequestMethod.GET, value = DATASET_IPID_PATH_FILE)
    @ResourceAccess(description = "Retrieves a dataset description file content")
    @ResponseBody
    public ResponseEntity<InputStreamResource> retrieveDatasetDescription(@PathVariable("dataset_ipId") String datasetIpId)
            throws EntityNotFoundException, IOException {
        DescriptionFile file = service.retrieveDescription(UniformResourceName.fromString(datasetIpId));
        InputStreamResource isr = new InputStreamResource(new ByteArrayInputStream(file.getContent()));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentLength(file.getContent().length);
        headers.setContentType(file.getType());
        return new ResponseEntity<>(isr, headers, HttpStatus.OK);
    }

    /**
     * Delete dataset of given id
     *
     * @param pDatasetId
     *            the id of the dataset to delete
     * @return a no content HTTP response
     * @throws EntityNotFoundException
     */
    @RequestMapping(method = RequestMethod.DELETE, value = DATASET_ID_PATH)
    @ResourceAccess(description = "Deletes a dataset")
    public ResponseEntity<Void> deleteDataset(@PathVariable("dataset_id") final Long pDatasetId)
            throws EntityNotFoundException {
        service.delete(pDatasetId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Update dataset of given id
     *
     * @param pDatasetId
     *            the id of the dataset to update
     * @param pDataset
     *            the new values of the dataset
     * @param pResult
     *            for validation of entites' properties
     * @return the updated dataset wrapped in an HTTP response
     * @throws ModuleException
     */
    @RequestMapping(method = RequestMethod.POST, value = DATASET_ID_PATH)
    @ResourceAccess(description = "Updates a Dataset")
    public ResponseEntity<Resource<Dataset>> updateDataset(@PathVariable("dataset_id") final Long pDatasetId,
            @Valid @RequestPart("dataset") final Dataset pDataset,
            @RequestPart(value = "file", required = false) final MultipartFile descriptionFile,
            final BindingResult pResult) throws ModuleException, IOException {
        // Validate dynamic model
        service.validate(pDataset, pResult, true);

        final Dataset dataSet = service.update(pDatasetId, pDataset, descriptionFile);
        final Resource<Dataset> resource = toResource(dataSet);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /**
     * Entry point to handle dissociation of {@link Dataset} specified by its id to other entities
     *
     * @param pDatasetId
     *            {@link Dataset} id
     * @param pToBeDissociated
     *            entity to dissociate
     * @return {@link Dataset} as a {@link Resource}
     * @throws ModuleException
     *             if error occurs
     */
    @RequestMapping(method = RequestMethod.PUT, value = DATASET_ID_DISSOCIATE_PATH)
    @ResourceAccess(description = "Dissociate a list of entities from a dataset")
    public ResponseEntity<Resource<Dataset>> dissociate(@PathVariable("dataset_id") final Long pDatasetId,
            @Valid @RequestBody final Set<UniformResourceName> pToBeDissociated) throws ModuleException {
        service.dissociate(pDatasetId, pToBeDissociated);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Entry point to handle association of {@link Dataset} specified by its id to other entities
     *
     * @param pDatasetId
     *            {@link Dataset} id
     * @param pToBeAssociatedWith
     *            entities to be associated
     * @return {@link Dataset} as a {@link Resource}
     * @throws ModuleException
     *             if error occurs
     */
    @RequestMapping(method = RequestMethod.PUT, value = DATASET_ID_ASSOCIATE_PATH)
    @ResourceAccess(description = "associate the list of entities to the dataset")
    public ResponseEntity<Void> associate(@PathVariable("dataset_id") final Long pDatasetId,
            @Valid @RequestBody final Set<UniformResourceName> pToBeAssociatedWith) throws ModuleException {
        service.associate(pDatasetId, pToBeAssociatedWith);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Retrieve data attributes of datasets of given URNs and given model name
     *
     * @param pUrns
     *            the URNs of datasets
     * @param pModelIds
     *            the id of dataset models
     * @param pPageable
     *            the page
     * @param pAssembler
     *            the resources assembler
     * @return the page of attribute models wrapped in an HTTP response
     * @throws ModuleException
     */
    @RequestMapping(method = RequestMethod.GET, value = DATASET_DATA_ATTRIBUTES_PATH)
    @ResourceAccess(description = "Retrieves data attributes of given datasets")
    public ResponseEntity<PagedResources<Resource<AttributeModel>>> retrieveDataAttributes(
            @RequestParam(name = "datasetIds", required = false) final Set<UniformResourceName> pUrns,
            @RequestParam(name = "modelIds", required = false) final Set<Long> pModelIds, final Pageable pPageable,
            final PagedResourcesAssembler<AttributeModel> pAssembler) throws ModuleException {
        final Page<AttributeModel> result = service.getDataAttributeModels(pUrns, pModelIds, pPageable);
        return new ResponseEntity<>(pAssembler.toResource(result), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, value = DATA_SUB_SETTING_VALIDATION)
    @ResourceAccess(description = "Validate if a subsetting is correct and coherent regarding a data model")
    public ResponseEntity<Validity> validateSubSettingClause(@RequestParam("dataModelId") Long dataModelId,
            @RequestBody Query query) throws ModuleException {
        // we have to add "q=" to be able to parse the query
        try {
            ICriterion criterionToBeVisited = openSearchService.parse("q=" + query.getQuery());
            SubsettingCoherenceVisitor visitor = service.getSubsettingCoherenceVisitor(dataModelId);
            return ResponseEntity.ok(new Validity(criterionToBeVisited.accept(visitor)));
        } catch (OpenSearchParseException e) {
            return ResponseEntity.ok(new Validity(false));
        }
    }

    @Override
    public Resource<Dataset> toResource(final Dataset pElement, final Object... pExtras) {
        final Resource<Dataset> resource = resourceService.toResource(pElement);
        resourceService.addLink(resource, this.getClass(), "retrieveDataset", LinkRels.SELF,
                                MethodParamFactory.build(Long.class, pElement.getId()));
        resourceService.addLink(resource, this.getClass(), "retrieveDatasets", LinkRels.LIST,
                                MethodParamFactory.build(Pageable.class),
                                MethodParamFactory.build(PagedResourcesAssembler.class));
        resourceService.addLink(resource, this.getClass(), "deleteDataset", LinkRels.DELETE,
                                MethodParamFactory.build(Long.class, pElement.getId()));
        resourceService.addLink(resource, this.getClass(), "updateDataset", LinkRels.UPDATE,
                                MethodParamFactory.build(Long.class, pElement.getId()),
                                MethodParamFactory.build(Dataset.class), MethodParamFactory.build(MultipartFile.class),
                                MethodParamFactory.build(BindingResult.class));
        resourceService.addLink(resource, this.getClass(), "dissociate", "dissociate",
                                MethodParamFactory.build(Long.class, pElement.getId()),
                                MethodParamFactory.build(Set.class));
        resourceService.addLink(resource, this.getClass(), "associate", "associate",
                                MethodParamFactory.build(Long.class, pElement.getId()),
                                MethodParamFactory.build(Set.class));
        return resource;
    }

    /**
     * Query POJO sent by our front to be validated
     */
    public static class Query {

        private String query;

        private Query() {
        }

        public Query(String query) {
            this.query = query;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }
    }

}
