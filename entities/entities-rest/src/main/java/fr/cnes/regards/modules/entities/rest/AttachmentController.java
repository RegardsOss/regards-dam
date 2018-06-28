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

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.net.HttpHeaders;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.oais.urn.DataType;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.framework.security.annotation.ResourceAccess;
import fr.cnes.regards.framework.security.role.DefaultRole;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.service.IEntityService;
import fr.cnes.regards.modules.entities.service.LocalStorageService;
import fr.cnes.regards.modules.indexer.domain.DataFile;

/**
 * Manage attachments for all entities in a generic way
 * @author Marc Sordi
 *
 */
@RestController
@RequestMapping(path = AttachmentController.TYPE_MAPPING)
public class AttachmentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentController.class);

    public static final String TYPE_MAPPING = "/entities/{urn}/files";

    /**
     * POST additional mapping
     */
    public static final String ATTACHMENTS_MAPPING = "/{dataType}";

    /**
     * GET and REMOVE additional mapping
     */
    public static final String ATTACHMENT_MAPPING = "/{checksum}";

    @Autowired
    private IEntityService<AbstractEntity<?>> entityService;

    @RequestMapping(method = RequestMethod.POST, value = ATTACHMENTS_MAPPING)
    @ResourceAccess(description = "Attach files of a same data type to an entity")
    public ResponseEntity<Resource<AbstractEntity<?>>> attachFiles(@Valid @PathVariable UniformResourceName urn,
            @PathVariable DataType dataType, @RequestPart MultipartFile[] attachments)
            throws ModuleException, NoSuchMethodException, SecurityException {

        LOGGER.debug("Attaching files of type \"{}\" to entity \"{}\"", dataType, urn.toString());

        // Build local URI template
        ControllerLinkBuilder controllerLinkBuilder = ControllerLinkBuilder
                .linkTo(this.getClass(),
                        this.getClass().getMethod("getFile", String.class, UniformResourceName.class, String.class,
                                                  HttpServletResponse.class),
                        urn, LocalStorageService.FILE_CHECKSUM_URL_TEMPLATE);

        // Attach files to the entity
        AbstractEntity<?> entity = entityService.attachFiles(urn, dataType, attachments,
                                                             controllerLinkBuilder.toUri().toString());
        return ResponseEntity.ok(new Resource<>(entity));
    }

    @RequestMapping(method = RequestMethod.GET, value = ATTACHMENT_MAPPING)
    @ResourceAccess(description = "Retrieve file with specified checksum for given entity", role = DefaultRole.PUBLIC)
    public void getFile(@RequestParam(required = false) String origin, @Valid @PathVariable UniformResourceName urn,
            @PathVariable String checksum, HttpServletResponse response) throws ModuleException, IOException {

        LOGGER.debug("Downloading file with checksum \"{}\" for entity \"{}\"", checksum, urn.toString());

        // Retrieve file properties
        DataFile dataFile = entityService.getFile(urn, checksum);
        // Build response
        if (origin != null) {
            response.setHeader(HttpHeaders.X_FRAME_OPTIONS, "ALLOW-FROM " + origin);
        }
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=" + dataFile.getName());
        response.setContentType(dataFile.getMimeType().toString());
        response.setContentLengthLong(dataFile.getSize());
        entityService.downloadFile(urn, checksum, response.getOutputStream());
        response.getOutputStream().flush();
        response.setStatus(HttpStatus.OK.value());
    }

    /**
     * Entry point to delete a file from a document
     * @param id {@link Document} id
     * @return the deleted document
     */
    @RequestMapping(method = RequestMethod.DELETE, value = ATTACHMENT_MAPPING)
    @ResourceAccess(description = "delete the document file using its id")
    public ResponseEntity<Resource<AbstractEntity<?>>> removeFile(@Valid @PathVariable UniformResourceName urn,
            @PathVariable String checksum) throws ModuleException, IOException {

        LOGGER.debug("Removing file with checksum \"{}\" from entity \"{}\"", checksum, urn.toString());

        // Attach files to the entity
        AbstractEntity<?> entity = entityService.removeFile(urn, checksum);
        return ResponseEntity.ok(new Resource<>(entity));
    }
}
