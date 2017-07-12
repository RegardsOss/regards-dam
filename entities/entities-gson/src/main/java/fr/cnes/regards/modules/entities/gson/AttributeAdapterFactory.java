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
package fr.cnes.regards.modules.entities.gson;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import fr.cnes.regards.framework.gson.adapters.PolymorphicTypeAdapterFactory;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.ObjectAttribute;

/**
 *
 * Factory for (de)serialize {@link AbstractAttribute}
 *
 * @author Marc Sordi
 *
 */
@Deprecated
@SuppressWarnings("rawtypes")
public class AttributeAdapterFactory extends PolymorphicTypeAdapterFactory<AbstractAttribute> {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AttributeAdapterFactory.class);

    /**
     * Discriminator field
     */
    private static final String DISCRIMINATOR_FIELD_NAME = "name";

    /**
     * Discriminator field
     */
    private static final String VALUE_FIELD_NAME = "value";

    /**
     * Namespace separator
     */
    private static final String NS_SEPARATOR = ".";

    /**
     * Regexp escape character
     */
    private static final String REGEXP_ESCAPE = "\\";

    public AttributeAdapterFactory() {
        super(AbstractAttribute.class, DISCRIMINATOR_FIELD_NAME);
    }

    public void registerSubtype(Class<?> pType, String pDiscriminatorFieldValue, String pNamespace) {
        registerSubtype(pType, pNamespace.concat(NS_SEPARATOR).concat(pDiscriminatorFieldValue));
    }

    @Override
    protected JsonElement beforeRead(JsonElement pJsonElement, String pDiscriminator, Class<?> pSubType) {
        if (pSubType == ObjectAttribute.class) {
            addNamespaceToChildren(pJsonElement, pDiscriminator);
        }
        removeParentNamespace(pJsonElement);
        return pJsonElement;
    }

    /**
     * Add namespace to {@link JsonElement} children of {@link ObjectAttribute}
     *
     * @param pJsonElement
     *            {@link JsonElement}
     * @param pDiscriminator
     *            discriminator value
     */
    protected void addNamespaceToChildren(JsonElement pJsonElement, String pDiscriminator) {

        if (pJsonElement.isJsonObject()) {
            final JsonElement children = pJsonElement.getAsJsonObject().get(VALUE_FIELD_NAME);

            if (children == null) {
                throw missingFieldException(pJsonElement, VALUE_FIELD_NAME);
            }

            if (children.isJsonArray()) {
                final Iterator<JsonElement> childrenIter = children.getAsJsonArray().iterator();
                while (childrenIter.hasNext()) {
                    addNamespaceToChild(childrenIter.next(), pDiscriminator);
                }
            } else {
                throw objectRequiredException(pJsonElement);
            }
        } else {
            throw objectRequiredException(pJsonElement);
        }
    }

    /**
     * Add namespace to {@link JsonElement} child
     *
     * @param pJsonElement
     *            {@link JsonElement}
     * @param pDiscriminator
     *            discriminator value
     */
    protected void addNamespaceToChild(JsonElement pJsonElement, String pDiscriminator) {

        if (pJsonElement.isJsonObject()) {

            // Backup for logging
            String logOriginal = pJsonElement.toString();
            JsonObject o = pJsonElement.getAsJsonObject();

            JsonElement originalElement = o.get(DISCRIMINATOR_FIELD_NAME);
            if (originalElement == null) {
                throw missingFieldException(pJsonElement, DISCRIMINATOR_FIELD_NAME);
            }

            // Compute and inject name with its namespace
            String originalName = originalElement.getAsString();
            String nsName = pDiscriminator.concat(NS_SEPARATOR).concat(originalName);
            o.add(DISCRIMINATOR_FIELD_NAME, new JsonPrimitive(nsName));

            LOGGER.debug(String.format("Namespace added : \"%s\" -> \"%s\"", logOriginal, pJsonElement.toString()));
        } else {
            throw objectRequiredException(pJsonElement);
        }
    }

    /**
     * Remove namespace from {@link JsonElement}
     *
     * @param pJsonElement
     *            target {@link JsonElement}
     */
    protected void removeParentNamespace(JsonElement pJsonElement) {

        if (pJsonElement.isJsonObject()) {

            // Backup for logging
            String logOriginal = pJsonElement.toString();

            final JsonObject o = pJsonElement.getAsJsonObject();
            final JsonElement nsElement = o.get(DISCRIMINATOR_FIELD_NAME);
            if (nsElement == null) {
                throw missingFieldException(pJsonElement, DISCRIMINATOR_FIELD_NAME);
            }

            // Compute and inject name without its namespace
            String nsName = nsElement.getAsString();
            String[] splitNsName = nsName.split(REGEXP_ESCAPE + NS_SEPARATOR);
            o.add(DISCRIMINATOR_FIELD_NAME, new JsonPrimitive(splitNsName[splitNsName.length - 1]));

            if (LOGGER.isDebugEnabled()) {
                if (splitNsName.length > 1) {
                    LOGGER.debug(String.format("Namespace removed : \"%s\" -> \"%s\"", logOriginal,
                                               pJsonElement.toString()));
                } else {
                    LOGGER.debug(String.format("No namespace to remove : \"%s\" -> \"%s\"", logOriginal,
                                               pJsonElement.toString()));
                }
            }
        } else {
            throw objectRequiredException(pJsonElement);
        }
    }

    private IllegalArgumentException objectRequiredException(JsonElement pJsonElement) {
        String errorMessage = String.format("Unexpected JSON element %s. Object required.", pJsonElement.toString());
        LOGGER.error(errorMessage);
        return new IllegalArgumentException(errorMessage);
    }

    private IllegalArgumentException missingFieldException(JsonElement pJsonElement, String pFieldName) {
        String errorMessage = String.format("JSON element %s must contains a \"%s\" field", pJsonElement.toString(),
                                            pFieldName);
        LOGGER.error(errorMessage);
        return new IllegalArgumentException(errorMessage);
    }
}
