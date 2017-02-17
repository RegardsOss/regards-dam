/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.rest;

import java.io.IOException;
import java.util.List;
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
import fr.cnes.regards.modules.entities.domain.DataSet;
import fr.cnes.regards.modules.entities.domain.DescriptionFile;
import fr.cnes.regards.modules.entities.service.DataSetService;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.plugins.utils.PluginUtilsException;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@RestController
@RequestMapping(value = DataSetController.DATASET_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class DataSetController implements IResourceController<DataSet> {

    public static final String DATASET_PATH = "/datasets";

    public static final String DATASET_ID_PATH = "/{dataset_id}";

    public static final String DATASET_ID_ASSOCIATE_PATH = DATASET_ID_PATH + "/associate";

    public static final String DATASET_ID_DISSOCIATE_PATH = DATASET_ID_PATH + "/dissociate";

    public static final String DATASET_ID_DESCRIPTION_PATH = DATASET_ID_PATH + "/description";

    public static final String DATASET_ID_SERVICES_PATH = DATASET_ID_PATH + "/services";

    @Autowired
    private IResourceService resourceService;

    @Autowired
    private DataSetService service;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    @ResourceAccess(description = "create and send the dataset")
    public HttpEntity<Resource<DataSet>> createDataSet(@Valid @RequestPart("dataset") DataSet pDataSet,
            @RequestPart("file") MultipartFile descriptionFile, BindingResult pResult)
            throws ModuleException, IOException, PluginUtilsException {

        // Validate dynamic model
        service.validate(pDataSet, pResult, false);

        DataSet created = service.create(pDataSet, descriptionFile);
        return new ResponseEntity<>(toResource(created), HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    @ResourceAccess(description = "endpoint to retrieve the list of all datasets")
    public HttpEntity<PagedResources<Resource<DataSet>>> retrieveDataSetList(final Pageable pPageable,
            final PagedResourcesAssembler<DataSet> pAssembler) {

        final Page<DataSet> datasets = service.retrieveDataSetList(pPageable);
        final PagedResources<Resource<DataSet>> resources = toPagedResources(datasets, pAssembler);
        return new ResponseEntity<>(resources, HttpStatus.OK);

    }

    @RequestMapping(method = RequestMethod.GET, value = DATASET_ID_PATH)
    @ResponseBody
    @ResourceAccess(description = "Retrieves a dataset")
    public HttpEntity<Resource<DataSet>> retrieveDataSet(@PathVariable("dataset_id") Long pDataSetId)
            throws EntityNotFoundException {
        DataSet dataSet = service.retrieveDataSet(pDataSetId);
        final Resource<DataSet> resource = toResource(dataSet);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = DATASET_ID_PATH)
    @ResponseBody
    @ResourceAccess(description = "Retrieves a dataset")
    public HttpEntity<Void> deleteDataSet(@PathVariable("dataset_id") Long pDataSetId)
            throws EntityNotFoundException, PluginUtilsException {
        service.delete(pDataSetId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(method = RequestMethod.PUT, value = DATASET_ID_PATH)
    @ResponseBody
    @ResourceAccess(description = "Updates a DataSet")
    public HttpEntity<Resource<DataSet>> updateDataSet(@PathVariable("dataset_id") Long pDataSetId,
            @Valid @RequestBody DataSet pDataSet, BindingResult pResult) throws ModuleException, PluginUtilsException {

        // Validate dynamic model
        service.validate(pDataSet, pResult, false);

        DataSet dataSet = service.update(pDataSetId, pDataSet);
        final Resource<DataSet> resource = toResource(dataSet);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = DATASET_ID_SERVICES_PATH)
    @ResponseBody
    @ResourceAccess(description = "Retrieves the list of configurations of Services of a DataSet")
    public HttpEntity<List<Long>> retrieveDataSetServices(@PathVariable("dataset_id") Long pDataSetId)
            throws EntityNotFoundException {
        List<Long> services = service.retrieveDataSetServices(pDataSetId);
        return new ResponseEntity<>(services, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = DATASET_ID_DESCRIPTION_PATH)
    @ResponseBody
    @ResourceAccess(description = "Retrieve the description file of the specified DataSet")
    public HttpEntity<byte[]> retrieveDescription(@PathVariable("dataset_id") Long pDataSetId)
            throws EntityNotFoundException {
        DescriptionFile descriptionToSend = service.retrieveDataSetDescription(pDataSetId);
        return null;
    }

    /**
     * Entry point to handle dissociation of {@link DataSet} specified by its id to other entities
     *
     * @param pDataSetId
     *            {@link DataSet} id
     * @param pToBeDissociated
     *            entity to dissociate
     * @return {@link DataSet} as a {@link Resource}
     * @throws ModuleException
     *             if error occurs
     */
    @RequestMapping(method = RequestMethod.PUT, value = DATASET_ID_DISSOCIATE_PATH)
    @ResponseBody
    @ResourceAccess(description = "Dissociate a dataset from  a list of entities")
    public HttpEntity<Resource<DataSet>> dissociateDataSet(@PathVariable("dataset_id") Long pDataSetId,
            @Valid @RequestBody Set<UniformResourceName> pToBeDissociated) throws ModuleException {
        final DataSet dataSet = (DataSet) service.dissociate(pDataSetId, pToBeDissociated);
        final Resource<DataSet> resource = toResource(dataSet);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    /**
     * Entry point to handle association of {@link DataSet} specified by its id to other entities
     *
     * @param pDataSetId
     *            {@link DataSet} id
     * @param pToBeAssociatedWith
     *            entities to be associated
     * @return {@link DataSet} as a {@link Resource}
     * @throws ModuleException
     *             if error occurs
     */
    @RequestMapping(method = RequestMethod.PUT, value = DATASET_ID_ASSOCIATE_PATH)
    @ResponseBody
    @ResourceAccess(description = "associate the dataset of id dataset_id to the list of entities in parameter")
    public HttpEntity<Resource<DataSet>> associateDataSet(@PathVariable("dataset_id") Long pDataSetId,
            @Valid @RequestBody Set<UniformResourceName> pToBeAssociatedWith) throws ModuleException {
        final DataSet dataset = (DataSet) service.associate(pDataSetId, pToBeAssociatedWith);
        final Resource<DataSet> resource = toResource(dataset);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    @Override
    public Resource<DataSet> toResource(DataSet pElement, Object... pExtras) {
        final Resource<DataSet> resource = resourceService.toResource(pElement);
        resourceService.addLink(resource, this.getClass(), "retrieveDataSet", LinkRels.SELF,
                                MethodParamFactory.build(Long.class, pElement.getId()));
        resourceService.addLink(resource, this.getClass(), "retrieveDataSetList", LinkRels.LIST);
        resourceService.addLink(resource, this.getClass(), "deleteDataSet", LinkRels.DELETE,
                                MethodParamFactory.build(Long.class, pElement.getId()));
        resourceService.addLink(resource, this.getClass(), "updateDataSet", LinkRels.UPDATE,
                                MethodParamFactory.build(Long.class, pElement.getId()),
                                MethodParamFactory.build(DataSet.class), MethodParamFactory.build(BindingResult.class));
        resourceService.addLink(resource, this.getClass(), "dissociateDataSet", "dissociate",
                                MethodParamFactory.build(Long.class, pElement.getId()),
                                MethodParamFactory.build(Set.class));
        resourceService.addLink(resource, this.getClass(), "associateDataSet", "associate",
                                MethodParamFactory.build(Long.class, pElement.getId()),
                                MethodParamFactory.build(Set.class));
        return resource;
    }

}
