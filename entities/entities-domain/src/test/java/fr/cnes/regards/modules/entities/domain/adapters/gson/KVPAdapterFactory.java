/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.adapters.gson;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

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
@SuppressWarnings("rawtypes")
public class KVPAdapterFactory extends PolymorphicTypeAdapterFactory<AbstractAttribute> {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(KVPAdapterFactory.class);

    /**
     * Discriminator field
     */
    private static final String DISCRIMINATOR_FIELD_NAME = "name";

    /**
     * Discriminator field
     */
    private static final String VALUE_FIELD = "value";

    /**
     * Namespace separator
     */
    private static final String NS_SEPARATOR = ".";

    /**
     * Regexp escape character
     */
    private static final String REGEXP_ESCAPE = "\\";

    public KVPAdapterFactory() {
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

    // CHECKSTYLE:OFF
    @Override
    public <T> TypeAdapter<T> create(Gson pGson, TypeToken<T> pType) { // NOSONAR
        // If factory not already created, refresh not needed
        refreshMapping = false;

        final Class<? super T> requestedType = pType.getRawType();
        if (!baseType.isAssignableFrom(requestedType)) {
            return null;
        }

        final Map<String, TypeAdapter<?>> discriminatorToDelegate = new LinkedHashMap<>();
        final Map<Class<?>, TypeAdapter<?>> subtypeToDelegate = new LinkedHashMap<>();

        // Register TypeAdapter delegation mapping from discriminator and type
        doMapping(pGson, discriminatorToDelegate, subtypeToDelegate);

        return new TypeAdapter<T>() { // NOSONAR

            /**
             * Delegate writing to default type adapter
             */
            @SuppressWarnings("unchecked")
            @Override
            public void write(JsonWriter pOut, T pValue) throws IOException {

                final Class<?> srcType = pValue.getClass();

                // registration requires that subtype extends base type
                TypeAdapter<T> delegate = (TypeAdapter<T>) subtypeToDelegate.get(srcType);

                if (delegate == null) {
                    // Try to refresh
                    if (refreshMapping) {
                        doMapping(pGson, discriminatorToDelegate, subtypeToDelegate);
                        delegate = (TypeAdapter<T>) subtypeToDelegate.get(srcType);
                        refreshMapping = false;
                    }

                    // If delegate still null
                    if (delegate == null) {
                        String errorMessage = String.format(
                                                            "Cannot serialize %s. Did you forget to register a subtype?",
                                                            srcType.getName());
                        LOGGER.error(errorMessage);
                        throw new JsonParseException(errorMessage);
                    }
                }

                // AbstractAttribute attribute = (AbstractAttribute) pValue;
                // pOut.beginObject();
                // pOut.name(attribute.getName());
                //
                // pGson.getDelegateAdapter(this, TypeToken.get(attribute.getValue().getClass()));
                // pOut.value(attribute.getValue());
                // pOut.endObject();

                // json.add(property, value);
                Streams.write(beforeWrite(delegate.toJsonTree(pValue), srcType), pOut);
            }

            private JsonElement beforeWrite(JsonElement pElement, Class<?> pSubType) {

                if (pSubType == ObjectAttribute.class) {
                    // Object attribute with multiple elements
                    JsonObject current = (JsonObject) pElement;
                    JsonObject kvp = new JsonObject();
                    // Key value pair
                    JsonElement key = current.get(DISCRIMINATOR_FIELD_NAME);
                    JsonArray val = current.getAsJsonArray(VALUE_FIELD);
                    LOGGER.debug(String.format("KVP : \"%s\" -> \"%s\".", key, val));
                    // Flatten element
                    JsonObject objectContent = new JsonObject();
                    Iterator<JsonElement> nestedIter = val.iterator();
                    while (nestedIter.hasNext()) {
                        JsonObject nested = nestedIter.next().getAsJsonObject();
                        for (Map.Entry<String, JsonElement> e : nested.entrySet()) {
                            objectContent.add(e.getKey(), e.getValue());
                        }
                    }
                    kvp.add(key.getAsString(), objectContent);
                    return kvp;

                } else {
                    // Single attribute
                    JsonObject current = (JsonObject) pElement;
                    JsonObject kvp = new JsonObject();
                    // Key value pair
                    JsonElement key = current.get(DISCRIMINATOR_FIELD_NAME);
                    JsonElement val = current.get(VALUE_FIELD);
                    LOGGER.debug(String.format("KVP : \"%s\" -> \"%s\".", key, val));
                    kvp.add(key.getAsString(), val);
                    return kvp;
                }
            }

            /**
             * Delegate reading to type adapter mapped to extracted discriminator value
             */
            @SuppressWarnings("unchecked")
            @Override
            public T read(JsonReader pIn) throws IOException {

                // Compute raw JSON object
                final JsonElement jsonElement = Streams.parse(pIn);

                // Discriminator value
                JsonElement discriminatorEl = jsonElement.getAsJsonObject().get(discriminatorFieldName);

                // Check value found
                if (discriminatorEl == null) {
                    String errorMessage = String.format(
                                                        "Cannot deserialize %s because it does not define a field named %s.",
                                                        baseType, discriminatorFieldName);
                    LOGGER.error(errorMessage);
                    throw new JsonParseException(errorMessage);
                }

                final String discriminator = discriminatorEl.getAsString();
                // registration requires that sub type extends T
                TypeAdapter<T> delegate = (TypeAdapter<T>) discriminatorToDelegate.get(discriminator);

                if (delegate == null) {
                    // Try to refresh
                    if (refreshMapping) {
                        doMapping(pGson, discriminatorToDelegate, subtypeToDelegate);
                        delegate = (TypeAdapter<T>) discriminatorToDelegate.get(discriminator);
                        refreshMapping = false;
                    }

                    // If delegate still null
                    if (delegate == null) {
                        String errorMessage = String.format(
                                                            "Cannot deserialize %s subtype named %s. Did you forget to register a subtype?",
                                                            baseType, discriminator);
                        LOGGER.error(errorMessage);
                        throw new JsonParseException(errorMessage);
                    }
                }

                beforeRead(jsonElement, discriminator, discriminatorToSubtype.get(discriminator));

                return delegate.fromJsonTree(jsonElement);
            }
        }.nullSafe();
    }
    // CHECKSTYLE:ON

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
            final JsonElement children = pJsonElement.getAsJsonObject().get(VALUE_FIELD);

            if (children == null) {
                throw missingFieldException(pJsonElement, VALUE_FIELD);
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
        String errorMessage = String.format("JSON element %s must contains a %s field", pJsonElement.toString(),
                                            pFieldName);
        LOGGER.error(errorMessage);
        return new IllegalArgumentException(errorMessage);
    }
}
