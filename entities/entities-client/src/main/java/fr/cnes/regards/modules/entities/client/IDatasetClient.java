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

    String DATASET_PATH = "/datasets";

    String DATASET_ID_PATH = "/{dataset_id}";

    String DATASET_ID_ASSOCIATE_PATH = DATASET_ID_PATH + "/associate";

    String DATASET_ID_DISSOCIATE_PATH = DATASET_ID_PATH + "/dissociate";

    String DATASET_ID_DESCRIPTION_PATH = DATASET_ID_PATH + "/description";

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
