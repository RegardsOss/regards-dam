/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.feature.dto;

import java.util.HashSet;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import fr.cnes.regards.framework.oais.urn.DataType;
import fr.cnes.regards.framework.utils.file.validation.HandledMessageDigestAlgorithm;

/**
 * @author Marc SORDI
 *
 */
public class FeatureFile {

    @NotNull(message = "Data type is required")
    private DataType dataType;

    /**
     * File locations (a file can be stored at several locations)
     */
    @Valid
    @NotEmpty(message = "At least one location is required")
    private Set<FeatureFileLocation> locations = new HashSet<>();

    /**
     * The file name
     */
    @NotBlank(message = "Filename is required")
    private String filename;

    /**
     * The checksum algorithm (<b>required</b> if data object is not a reference)
     */
    @HandledMessageDigestAlgorithm
    private String algorithm;

    /**
     * The checksum (<b>required</b> if data object is not a reference)
     */
    private String checksum;

    /**
     * The file size
     */
    private Long fileSize;

}
