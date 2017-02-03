/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.rest;

import java.util.List;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import fr.cnes.regards.framework.hateoas.IResourceController;
import fr.cnes.regards.framework.hateoas.IResourceService;
import fr.cnes.regards.framework.hateoas.LinkRels;
import fr.cnes.regards.framework.hateoas.MethodParamFactory;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.DataSet;
import fr.cnes.regards.modules.entities.service.DataSetService;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@RestController
@RequestMapping(value = DataSetController.DATASET_PATH)
public class DataSetController implements IResourceController<DataSet> {

    public static final String DATASET_PATH = "/datasets";

    public static final String DATASET_ID_PATH = "/{dataset_id}";

    public static final String DATASET_ID_SERVICES_PATH = DATASET_ID_PATH + "/services";

    @Autowired
    private IResourceService resourceService;

    @Autowired
    private DataSetService service;

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResourceAccess(description = "create and send the dataset")
    public HttpEntity<Resource<DataSet>> createDataSet(@Valid @RequestBody DataSet pDataSet, BindingResult pResult)
            throws ModuleException {

        // Validate dynamic model
        service.validate(pDataSet, pResult, false);

        DataSet created = service.create(pDataSet);
        return new ResponseEntity<>(toResource(created), HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResourceAccess(description = "endpoint to retrieve the list of all datasets")
    public HttpEntity<PagedResources<Resource<DataSet>>> retrieveDataSetList(final Pageable pPageable,
            final PagedResourcesAssembler<DataSet> pAssembler) {

        final Page<DataSet> datasets = service.retrieveDataSetList(pPageable);
        final PagedResources<Resource<DataSet>> resources = toPagedResources(datasets, pAssembler);
        return new ResponseEntity<>(resources, HttpStatus.OK);

    }

    @RequestMapping(method = RequestMethod.GET, value = DATASET_ID_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResourceAccess(description = "Retrieves a dataset")
    public HttpEntity<Resource<DataSet>> retrieveDataSet(@PathVariable("dataset_id") Long pDataSetId)
            throws EntityNotFoundException {
        DataSet dataSet = service.retrieveDataSet(pDataSetId);
        final Resource<DataSet> resource = toResource(dataSet);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = DATASET_ID_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResourceAccess(description = "Retrieves a dataset")
    public HttpEntity<Void> deleteDataSet(@PathVariable("dataset_id") Long pDataSetId) throws EntityNotFoundException {
        service.delete(pDataSetId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(method = RequestMethod.PUT, value = DATASET_ID_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResourceAccess(description = "Updates a DataSet")
    public HttpEntity<Resource<DataSet>> updateDataSet(@PathVariable("dataset_id") Long pDataSetId,
            @Valid @RequestBody DataSet pDataSet, BindingResult pResult) throws ModuleException {

        // Validate dynamic model
        service.validate(pDataSet, pResult, false);

        DataSet dataSet = service.update(pDataSetId, pDataSet);
        final Resource<DataSet> resource = toResource(dataSet);
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = DATASET_ID_SERVICES_PATH,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResourceAccess(description = "Retrieves the list of configurations of Services of a DataSet")
    public HttpEntity<List<Long>> retrieveDataSetServices(@PathVariable("dataset_id") Long pDataSetId)
            throws EntityNotFoundException {
        List<Long> services = service.retrieveDataSetServices(pDataSetId);
        return new ResponseEntity<>(services, HttpStatus.OK);
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
                                MethodParamFactory.build(Collection.class));
        return resource;
    }

}
