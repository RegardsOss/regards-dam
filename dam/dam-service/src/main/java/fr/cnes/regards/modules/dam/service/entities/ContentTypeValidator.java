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
package fr.cnes.regards.modules.dam.service.entities;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.http.MediaType;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.oais.urn.DataType;
import fr.cnes.regards.modules.dam.service.entities.exception.InvalidCharsetException;
import fr.cnes.regards.modules.dam.service.entities.exception.InvalidContentTypeException;
import fr.cnes.regards.modules.dam.service.entities.exception.InvalidFilenameException;

/**
 * Utility class to validate supported content type for a file data type.
 *
 * @author Marc Sordi
 *
 */
public final class ContentTypeValidator {

    /**
     * Before the RFC7763 (March 2016), some browser were using a different MediaType.
     */
    public final static String TEXT_MARKDOWN_ALTERNATIVE_MEDIATYPE = "text/x-markdown";

    private ContentTypeValidator() {
        // Nothing to do
    }

    public static void supports(DataType dataType, String filename, String contentType) throws ModuleException {

        // Original file must be given
        if ((filename == null) || filename.isEmpty()) {
            throw new InvalidFilenameException();
        }

        switch (dataType) {
            case DESCRIPTION:
                checkFileSupported(contentType, Arrays.asList(MediaType.APPLICATION_PDF_VALUE, MediaType.TEXT_MARKDOWN_VALUE,
                        TEXT_MARKDOWN_ALTERNATIVE_MEDIATYPE));
                break;
            default:
                // No restriction
                break;
        }
    }

    public static void supportsForReference(DataType dataType, String filename, String contentType)
            throws ModuleException {

        // Original file must be given
        if ((filename == null) || filename.isEmpty()) {
            throw new InvalidFilenameException();
        }

        switch (dataType) {
            case DESCRIPTION:
                checkFileSupported(contentType, Arrays.asList(MediaType.APPLICATION_PDF_VALUE, MediaType.TEXT_MARKDOWN_VALUE,
                                                 MediaType.TEXT_HTML_VALUE, TEXT_MARKDOWN_ALTERNATIVE_MEDIATYPE));
                break;
            default:
                // No restriction
                break;
        }

    }

    private static void checkFileSupported(String contentType, Collection<String> expectedContentTypes)
            throws InvalidCharsetException, InvalidContentTypeException {

        // Check content types
        if (expectedContentTypes != null) {
            String shortContentType = getContentType(contentType);
            if (!expectedContentTypes.contains(shortContentType)) {
                throw new InvalidContentTypeException(expectedContentTypes, shortContentType);
            }
        }
    }

    private static String getContentType(String contentType) {
        if (contentType != null) {
            int charsetIdx = contentType.indexOf(";charset");
            return (charsetIdx == -1) ? contentType.trim() : contentType.substring(0, charsetIdx).trim();
        }
        return null;
    }

    private static String getCharset(String contentType) {
        if (contentType != null) {
            int charsetIdx = contentType.indexOf("charset=");
            return (charsetIdx == -1) ? null : contentType.substring(charsetIdx + 8).trim().toUpperCase();
        }
        return null;
    }
}
