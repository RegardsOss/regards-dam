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
package fr.cnes.regards.modules.entities.client;

import fr.cnes.regards.framework.feign.annotation.RestClient;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import org.springframework.core.io.InputStreamResource;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Set;

/**
 * @author Sylvain Vissiere-Guerinet
 */
@RestClient(name = "rs-dam")
@RequestMapping(value = IDatasetClient.DATASET_PATH, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public interface IDatasetClient {

    public static final String DATASET_PATH = "/datasets";

    public static final String DATASET_ID_PATH = "/{dataset_id}";

    public static final String DATASET_ID_ASSOCIATE_PATH = DATASET_ID_PATH + "/associate";

    public static final String DATASET_ID_DISSOCIATE_PATH = DATASET_ID_PATH + "/dissociate";

    public static final String DATASET_IPID_PATH_FILE = "/{dataset_ipId}/file";


    @RequestMapping(method = RequestMethod.GET)
    ResponseEntity<PagedResources<Resource<Dataset>>> retrieveDatasets(@RequestParam("page") int pPage,
                                                                       @RequestParam("size") int pSize);

    /**
     * Retrieve a dataset using its id
     * @param pDatasetId
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = DATASET_ID_PATH)
    ResponseEntity<Resource<Dataset>> retrieveDataset(@PathVariable("dataset_id") Long pDatasetId);

    /**
     * Delete dataset
     * @param pDatasetId
     * @return
     */
    @RequestMapping(method = RequestMethod.DELETE, value = DATASET_ID_PATH)
    ResponseEntity<Void> deleteDataset(@PathVariable("dataset_id") Long pDatasetId);

    /**
     * Update dataset
     * @param pDatasetId
     * @param pDataset
     * @return
     */
    @RequestMapping(method = RequestMethod.PUT, value = DATASET_ID_PATH)
    ResponseEntity<Resource<Dataset>> updateDataset(@PathVariable("dataset_id") Long pDatasetId,
                                                    @RequestBody Dataset pDataset);

    /**
     * Entry point to handle dissociation of {@link Dataset} specified by its id to other entities
     *
     * @param pDatasetId       {@link Dataset} id
     * @param pToBeDissociated entity to dissociate
     * @return {@link Dataset} as a {@link Resource}
     * @throws ModuleException if error occurs
     */
    @RequestMapping(method = RequestMethod.PUT, value = DATASET_ID_DISSOCIATE_PATH)
    ResponseEntity<Resource<Dataset>> dissociateDataset(@PathVariable("dataset_id") Long pDatasetId,
                                                        @RequestBody Set<UniformResourceName> pToBeDissociated);

    /**
     * Entry point to handle association of {@link Dataset} specified by its id to other entities
     *
     * @param pDatasetId          {@link Dataset} id
     * @param pToBeAssociatedWith entities to be associated
     * @return {@link Dataset} as a {@link Resource}
     * @throws ModuleException if error occurs
     */
    @RequestMapping(method = RequestMethod.PUT, value = DATASET_ID_ASSOCIATE_PATH)
    ResponseEntity<Resource<Dataset>> associateDataset(@PathVariable("dataset_id") Long pDatasetId,
                                                       @RequestBody Set<UniformResourceName> pToBeAssociatedWith);


    /**
     * Returns the dataset description file²
     *
     * @param datasetIpId
     * @return
     * @throws EntityNotFoundException
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.GET, value = DATASET_IPID_PATH_FILE)
    @ResponseBody
    ResponseEntity<StreamingResponseBody> retrieveDatasetDescription(@PathVariable("dataset_ipId") String datasetIpId,
                                                                     HttpServletResponse response)
            throws EntityNotFoundException, IOException;

}
