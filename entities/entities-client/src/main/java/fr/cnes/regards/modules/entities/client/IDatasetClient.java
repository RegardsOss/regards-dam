/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.client;

import java.util.Set;

import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import fr.cnes.regards.framework.feign.annotation.RestClient;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@RestClient(name = "rs-dam")
@RequestMapping(value = IDatasetClient.DATASET_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public interface IDatasetClient {

    public static final String DATASET_PATH = "/datasets";

    public static final String DATASET_ID_PATH = "/{dataset_id}";

    public static final String DATASET_ID_ASSOCIATE_PATH = DATASET_ID_PATH + "/associate";

    public static final String DATASET_ID_DISSOCIATE_PATH = DATASET_ID_PATH + "/dissociate";

    public static final String DATASET_ID_DESCRIPTION_PATH = DATASET_ID_PATH + "/description";

    // FIXME
    /*    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<Resource<Dataset>> createDataset(@RequestPart("dataset") Dataset pDataset,
            @RequestPart("file") MultipartFile descriptionFile);*/

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<PagedResources<Resource<Dataset>>> retrieveDatasets(@RequestParam("page") int pPage,
            @RequestParam("size") int pSize);

    @RequestMapping(method = RequestMethod.GET, value = DATASET_ID_PATH)
    @ResponseBody
    public ResponseEntity<Resource<Dataset>> retrieveDataset(@PathVariable("dataset_id") Long pDatasetId);

    @RequestMapping(method = RequestMethod.DELETE, value = DATASET_ID_PATH)
    @ResponseBody
    public ResponseEntity<Void> deleteDataset(@PathVariable("dataset_id") Long pDatasetId);

    @RequestMapping(method = RequestMethod.PUT, value = DATASET_ID_PATH)
    @ResponseBody
    public ResponseEntity<Resource<Dataset>> updateDataset(@PathVariable("dataset_id") Long pDatasetId,
            @RequestBody Dataset pDataset);

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
    public ResponseEntity<Resource<Dataset>> dissociateDataset(@PathVariable("dataset_id") Long pDatasetId,
            @RequestBody Set<UniformResourceName> pToBeDissociated);

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
    public ResponseEntity<Resource<Dataset>> associateDataset(@PathVariable("dataset_id") Long pDatasetId,
            @RequestBody Set<UniformResourceName> pToBeAssociatedWith);

}
