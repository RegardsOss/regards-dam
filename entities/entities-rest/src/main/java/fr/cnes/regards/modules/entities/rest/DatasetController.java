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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
import fr.cnes.regards.modules.entities.service.DatasetService;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.plugins.utils.PluginUtilsException;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@RestController
@RequestMapping(value = DatasetController.DATASET_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class DatasetController implements IResourceController<Dataset> {

    public static final String DATASET_PATH = "/datasets";

    public static final String DATASET_ID_PATH = "/{dataset_id}";

    public static final String DATASET_ID_ASSOCIATE_PATH = DATASET_ID_PATH + "/associate";

    public static final String DATASET_ID_DISSOCIATE_PATH = DATASET_ID_PATH + "/dissociate";

    public static final String DATASET_ID_DESCRIPTION_PATH = DATASET_ID_PATH + "/description";

    @Autowired
    private IResourceService resourceService;

    @Autowired
    private DatasetService service;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    @ResourceAccess(description = "create and send the dataset")
    public HttpEntity<Resource<Dataset>> createDataset(@Valid @RequestPart("dataset") Dataset pDataset,
            @RequestPart("file") MultipartFile descriptionFile, BindingResult pResult)
            throws ModuleException, IOException, PluginUtilsException {

        // Validate dynamic model
        service.validate(pDataset, pResult, false);

        Dataset created = service.create(pDataset, descriptionFile);
        return new ResponseEntity<>(toResource(created), HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    @ResourceAccess(description = "endpoint to retrieve the list of all datasets")
    public HttpEntity<PagedResources<Resource<Dataset>>> retrieveDatasets(final Pageable pPageable,
            final PagedResourcesAssembler<Dataset> pAssembler) {

        final Page<Dataset> datasets = service.retrieveDatasets(pPageable);
        final PagedResources<Resource<Dataset>> resources = toPagedResources(datasets, pAssembler);
        return new ResponseEntity<>(resources, HttpStatus.OK);

    }

    @RequestMapping(method = RequestMethod.GET, value = DATASET_ID_PATH)
    @ResponseBody
    @ResourceAccess(description = "Retrieves a dataset")
    public HttpEntity<Resource<Dataset>> retrieveDataset(@PathVariable("dataset_id") Long pDatasetId)
            throws EntityNotFoundException {
        Dataset dataSet = service.retrieveDataset(pDatasetId);
        final Resource<Dataset> resource = toResource(dataSet);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = DATASET_ID_PATH)
    @ResponseBody
    @ResourceAccess(description = "Deletes a dataset")
    public HttpEntity<Void> deleteDataset(@PathVariable("dataset_id") Long pDatasetId)
            throws EntityNotFoundException, PluginUtilsException {
        service.delete(pDatasetId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(method = RequestMethod.PUT, value = DATASET_ID_PATH)
    @ResponseBody
    @ResourceAccess(description = "Updates a Dataset")
    public HttpEntity<Resource<Dataset>> updateDataset(@PathVariable("dataset_id") Long pDatasetId,
            @Valid @RequestBody Dataset pDataset, BindingResult pResult) throws ModuleException, PluginUtilsException {

        // Validate dynamic model
        service.validate(pDataset, pResult, false);

        Dataset dataSet = service.update(pDatasetId, pDataset);
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
    @ResponseBody
    @ResourceAccess(description = "Dissociate a list of entities from a dataset")
    public HttpEntity<Resource<Dataset>> dissociateDataset(@PathVariable("dataset_id") Long pDatasetId,
            @Valid @RequestBody Set<UniformResourceName> pToBeDissociated) throws ModuleException {
        final Dataset dataSet = (Dataset) service.dissociate(pDatasetId, pToBeDissociated);
        final Resource<Dataset> resource = toResource(dataSet);
        return new ResponseEntity<>(resource, HttpStatus.OK);
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
    @ResponseBody
    @ResourceAccess(description = "associate the list of entities to the dataset")
    public HttpEntity<Resource<Dataset>> associateDataset(@PathVariable("dataset_id") Long pDatasetId,
            @Valid @RequestBody Set<UniformResourceName> pToBeAssociatedWith) throws ModuleException {
        final Dataset dataset = (Dataset) service.associate(pDatasetId, pToBeAssociatedWith);
        final Resource<Dataset> resource = toResource(dataset);
        return new ResponseEntity<>(resource, HttpStatus.OK);
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
