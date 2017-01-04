/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.adapters.gson;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import fr.cnes.regards.modules.entities.domain.AbstractEntity;

/**
 * Allows to customize GSON (de)serialization for an entity inheriting from {@link AbstractEntity}.
 *
 * @author Marc Sordi
 *
 * @param <T>
 *            concrete entity class
 */
public abstract class AbstractEntityTypeAdapterFactory<T> implements TypeAdapterFactory {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEntityTypeAdapterFactory.class);

    /**
     * attributes
     */
    private static final String ATTRIBUTE_FIELD_NAME = "attributes";

    /**
     * Entity to customize
     */
    private final Class<T> entityClass;

    public AbstractEntityTypeAdapterFactory(Class<T> pEntityClass) {
        this.entityClass = pEntityClass;
        LOGGER.debug("Entity type adapter factory created for {}", pEntityClass);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> TypeAdapter<E> create(Gson pGson, TypeToken<E> pType) {
        final Class<? super E> requestedType = pType.getRawType();
        if (!AbstractEntity.class.isAssignableFrom(requestedType) || (requestedType != entityClass)) {
            return null;
        }

        return (TypeAdapter<E>) customTypeAdapter(pGson, (TypeToken<T>) pType);
    }

    /**
     * Create custom {@link TypeAdapter}
     *
     * @param pGson
     *            GSON instance
     * @param pType
     *            linked type
     * @return custom {@link TypeAdapter}
     */
    private TypeAdapter<T> customTypeAdapter(Gson pGson, TypeToken<T> pType) {
        final TypeAdapter<T> delegate = pGson.getDelegateAdapter(this, pType);
        final TypeAdapter<JsonElement> elementAdapter = pGson.getAdapter(JsonElement.class);
        return new TypeAdapter<T>() {

            @Override
            public void write(JsonWriter pOut, T pValue) throws IOException {
                JsonElement tree = delegate.toJsonTree(pValue);
                beforeWrite(pValue, tree);
                elementAdapter.write(pOut, tree);
            }

            @Override
            public T read(JsonReader pIn) throws IOException {
                JsonElement tree = elementAdapter.read(pIn);
                beforeRead(tree);
                return delegate.fromJsonTree(tree);
            }
        };
    }

    protected void beforeWrite(T pSource, JsonElement pJsonElement) {
        LOGGER.debug("Before write");

        if (!pJsonElement.isJsonObject()) {
            // TODO throw error
        }

        JsonObject entity = pJsonElement.getAsJsonObject();
        JsonElement attEl = entity.get(ATTRIBUTE_FIELD_NAME);
        if (attEl != null) {
            if (attEl.isJsonArray()) {
                entity.add(ATTRIBUTE_FIELD_NAME, mergeArray(attEl.getAsJsonArray()));
            } else {
                // TODO throw error
            }
        }
    }

    protected void beforeRead(JsonElement pJsonElement) {
        LOGGER.debug("Before read");

        if (!pJsonElement.isJsonObject()) {
            // TODO throw error
        }

        JsonObject entity = pJsonElement.getAsJsonObject();
        JsonElement attEl = entity.get(ATTRIBUTE_FIELD_NAME);
        if (attEl != null) {
            if (attEl.isJsonObject()) {
                entity.add(ATTRIBUTE_FIELD_NAME, restoreArray(attEl.getAsJsonObject()));
            }
        }

    }

    private JsonObject mergeArray(JsonArray pJsonArray) {
        JsonObject mergedObject = new JsonObject();
        Iterator<JsonElement> nestedIter = pJsonArray.iterator();
        while (nestedIter.hasNext()) {
            JsonElement nested = nestedIter.next();
            if (nested.isJsonObject()) {
                JsonObject nestedObject = nested.getAsJsonObject();
                for (Map.Entry<String, JsonElement> e : nestedObject.entrySet()) {
                    mergedObject.add(e.getKey(), e.getValue());
                }
            } else {
                // TODO exception
            }
        }
        return mergedObject;
    }

    private JsonArray restoreArray(JsonObject pJsonObject) {
        JsonArray restoredArray = new JsonArray();
        for (Map.Entry<String, JsonElement> nestedEntry : pJsonObject.entrySet()) {
            JsonObject nestedObject = new JsonObject();
            nestedObject.add(nestedEntry.getKey(), nestedEntry.getValue());
            restoredArray.add(nestedObject);
        }
        return restoredArray;
    }
}
