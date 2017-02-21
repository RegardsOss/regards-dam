/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.service.adapters.gson;

import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fr.cnes.regards.framework.gson.adapters.PolymorphicTypeAdapterFactory;
import fr.cnes.regards.framework.gson.annotation.GsonTypeAdapterFactory;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.Document;
import fr.cnes.regards.modules.models.domain.EntityType;

/**
 *
 * Entity adapter factory
 *
 * @author Marc Sordi
 *
 */
@GsonTypeAdapterFactory
public class EntityAdapterFactory extends PolymorphicTypeAdapterFactory<AbstractEntity> {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityAdapterFactory.class);

    /**
     * attributes
     */
    private static final String ATTRIBUTE_FIELD_NAME = "attributes";

    public EntityAdapterFactory() {
        super(AbstractEntity.class, "type", true);
        registerSubtype(Collection.class, EntityType.COLLECTION);
        registerSubtype(Dataset.class, EntityType.DATASET);
        registerSubtype(Document.class, EntityType.DOCUMENT);
        registerSubtype(DataObject.class, EntityType.DATA);
    }

    @Override
    protected JsonElement beforeWrite(JsonElement pJsonElement, Class<?> pSubType) {

        // Do injection
        JsonElement clone = super.beforeWrite(pJsonElement, pSubType);

        LOGGER.debug("Before write");

        if (!clone.isJsonObject()) {
            throw objectRequiredException(clone);
        }

        JsonObject entity = clone.getAsJsonObject();
        JsonElement attEl = entity.get(ATTRIBUTE_FIELD_NAME);
        if ((attEl != null) && !attEl.isJsonNull()) {
            if (attEl.isJsonArray()) {
                entity.add(ATTRIBUTE_FIELD_NAME, mergeArray(attEl.getAsJsonArray()));
            } else {
                String errorMessage = String.format("Unexpected JSON element %s. Array required.", clone.toString());
                LOGGER.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
        }
        return entity;
    }

    @Override
    protected JsonElement beforeRead(JsonElement pJsonElement, String pDiscriminator, Class<?> pSubType) {
        LOGGER.debug("Before read");

        if (!pJsonElement.isJsonObject()) {
            throw objectRequiredException(pJsonElement);
        }

        JsonObject entity = pJsonElement.getAsJsonObject();
        JsonElement attEl = entity.get(ATTRIBUTE_FIELD_NAME);
        if (attEl != null) {
            if (attEl.isJsonObject()) {
                entity.add(ATTRIBUTE_FIELD_NAME, restoreArray(attEl.getAsJsonObject()));
            } else {
                throw objectRequiredException(attEl);
            }
        }
        return entity;
    }

    /**
     * Merge {@link JsonArray} flattening elements in a single {@link JsonObject}
     *
     * @param pJsonArray
     *            {@link JsonArray} to flatten
     * @return {@link JsonObject}
     */
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
                throw objectRequiredException(nested);
            }
        }
        return mergedObject;
    }

    /**
     * Restore {@link JsonArray} from flattened {@link JsonObject} elements (reverse merge)
     *
     * @param pJsonObject
     *            {@link JsonObject} to transform
     * @return {@link JsonArray}
     */
    private JsonArray restoreArray(JsonObject pJsonObject) {
        JsonArray restoredArray = new JsonArray();
        for (Map.Entry<String, JsonElement> nestedEntry : pJsonObject.entrySet()) {
            JsonObject nestedObject = new JsonObject();
            nestedObject.add(nestedEntry.getKey(), nestedEntry.getValue());
            restoredArray.add(nestedObject);
        }
        return restoredArray;
    }

    private IllegalArgumentException objectRequiredException(JsonElement pJsonElement) {
        String errorMessage = String.format("Unexpected JSON element %s. Object required.", pJsonElement.toString());
        LOGGER.error(errorMessage);
        return new IllegalArgumentException(errorMessage);
    }
}
