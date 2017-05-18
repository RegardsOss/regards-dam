/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.rest;

import java.io.IOException;
import java.util.Set;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.hateoas.LinkRels;
import fr.cnes.regards.framework.hateoas.MethodParamFactory;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.service.IDatasetService;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.opensearch.service.IOpenSearchService;

/**
 * Rest controller managing {@link Dataset}s
 *
 * @author Sylvain Vissiere-Guerinet
 * @author Xavier-Alexandre Brochard
 */
@RestController
@RequestMapping(value = DatasetController.DATASET_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
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

    /**
     * Endpoint to retrieve dataset description file
     */
    public static final String DATASET_ID_DESCRIPTION_PATH = DATASET_ID_PATH + "/description";

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
     * @param pDataset the dataset to create
     * @param descriptionFile the description file
     * @param pResult for validation of entites' properties
     * @return the created dataset wrapped in an HTTP response
     * @throws ModuleException
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResourceAccess(description = "create and send the dataset")
    public ResponseEntity<Resource<Dataset>> createDataset(@Valid @RequestPart("dataset") Dataset pDataset,
            @RequestPart(value = "file", required = false) MultipartFile descriptionFile, BindingResult pResult)
            throws ModuleException, IOException {

        // Validate dynamic model
        service.validate(pDataset, pResult, false);

        Dataset created = service.create(pDataset, descriptionFile);
        return new ResponseEntity<>(toResource(created), HttpStatus.CREATED);
    }

    /**
     * Retrieve datasets
     * @param pPageable the page
     * @param pAssembler the dataset resources assembler
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
     * @param pDatasetId the id of the dataset
     * @return the dataset of passed id
     * @throws EntityNotFoundException Thrown when no dataset with given id could be found
     */
    @RequestMapping(method = RequestMethod.GET, value = DATASET_ID_PATH)
    @ResourceAccess(description = "Retrieves a dataset")
    public ResponseEntity<Resource<Dataset>> retrieveDataset(@PathVariable("dataset_id") Long pDatasetId)
            throws EntityNotFoundException {
        Dataset dataset = service.load(pDatasetId);
        if (dataset == null) {
            throw new EntityNotFoundException(pDatasetId);
        }
        final Resource<Dataset> resource = toResource(dataset);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /**
     * Delete dataset of given id
     * @param pDatasetId the id of the dataset to delete
     * @return a no content HTTP response
     * @throws EntityNotFoundException
     */
    @RequestMapping(method = RequestMethod.DELETE, value = DATASET_ID_PATH)
    @ResourceAccess(description = "Deletes a dataset")
    public ResponseEntity<Void> deleteDataset(@PathVariable("dataset_id") Long pDatasetId)
            throws EntityNotFoundException {
        service.delete(pDatasetId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Update dataset of given id
     * @param pDatasetId the id of the dataset to update
     * @param pDataset the new values of the dataset
     * @param pResult for validation of entites' properties
     * @return the updated dataset wrapped in an HTTP response
     * @throws ModuleException
     */
    @RequestMapping(method = RequestMethod.PUT, value = DATASET_ID_PATH)
    @ResourceAccess(description = "Updates a Dataset")
    public ResponseEntity<Resource<Dataset>> updateDataset(@PathVariable("dataset_id") Long pDatasetId,
            @Valid @RequestBody Dataset pDataset, BindingResult pResult) throws ModuleException {
        // Validate dynamic model
        service.validate(pDataset, pResult, false);

        // Convert OpenSearch subsetting clause
        pDataset.setSubsettingClause(openSearchService.parse(pDataset.getOpenSearchSubsettingClause()));

        Dataset dataSet = service.update(pDatasetId, pDataset);
        final Resource<Dataset> resource = toResource(dataSet);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /**
     * Entry point to handle dissociation of {@link Dataset} specified by its id to other entities
     *
     * @param pDatasetId {@link Dataset} id
     * @param pToBeDissociated entity to dissociate
     * @return {@link Dataset} as a {@link Resource}
     * @throws ModuleException if error occurs
     */
    @RequestMapping(method = RequestMethod.PUT, value = DATASET_ID_DISSOCIATE_PATH)
    @ResourceAccess(description = "Dissociate a list of entities from a dataset")
    public ResponseEntity<Resource<Dataset>> dissociateDataset(@PathVariable("dataset_id") Long pDatasetId,
            @Valid @RequestBody Set<UniformResourceName> pToBeDissociated) throws ModuleException {
        final Dataset dataSet = service.dissociate(pDatasetId, pToBeDissociated);
        final Resource<Dataset> resource = toResource(dataSet);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /**
     * Entry point to handle association of {@link Dataset} specified by its id to other entities
     *
     * @param pDatasetId {@link Dataset} id
     * @param pToBeAssociatedWith entities to be associated
     * @return {@link Dataset} as a {@link Resource}
     * @throws ModuleException if error occurs
     */
    @RequestMapping(method = RequestMethod.PUT, value = DATASET_ID_ASSOCIATE_PATH)
    @ResourceAccess(description = "associate the list of entities to the dataset")
    public ResponseEntity<Resource<Dataset>> associateDataset(@PathVariable("dataset_id") Long pDatasetId,
            @Valid @RequestBody Set<UniformResourceName> pToBeAssociatedWith) throws ModuleException {
        final Dataset dataset = service.associate(pDatasetId, pToBeAssociatedWith);
        final Resource<Dataset> resource = toResource(dataset);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /**
     * Retrieve data attributes of datasets of given URNs and given model name
     * @param pUrns the URNs of datasets
     * @param pModelName the model name
     * @param pPageable the page
     * @param pAssembler the resources assembler
     * @return the page of attribute models wrapped in an HTTP response
     * @throws ModuleException
     */
    @RequestMapping(method = RequestMethod.GET, value = DATASET_DATA_ATTRIBUTES_PATH)
    @ResponseBody
    @ResourceAccess(description = "Retrieves data attributes of given datasets")
    public ResponseEntity<PagedResources<Resource<AttributeModel>>> retrieveDataAttributes(
            @RequestParam(name = "datasetIds", required = false) Set<UniformResourceName> pUrns,
            @RequestParam(name = "modelName", required = false) String pModelName, final Pageable pPageable,
            final PagedResourcesAssembler<AttributeModel> pAssembler) throws ModuleException {
        Page<AttributeModel> result = service.getDataAttributeModels(pUrns, pModelName, pPageable);
        return new ResponseEntity<>(pAssembler.toResource(result), HttpStatus.OK);
    }

    @Override
    public Resource<Dataset> toResource(Dataset pElement, Object... pExtras) {
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
                                MethodParamFactory.build(Dataset.class), MethodParamFactory.build(BindingResult.class));
        resourceService.addLink(resource, this.getClass(), "dissociateDataset", "dissociate",
                                MethodParamFactory.build(Long.class, pElement.getId()),
                                MethodParamFactory.build(Set.class));
        resourceService.addLink(resource, this.getClass(), "associateDataset", "associate",
                                MethodParamFactory.build(Long.class, pElement.getId()),
                                MethodParamFactory.build(Set.class));
        return resource;
    }

}
