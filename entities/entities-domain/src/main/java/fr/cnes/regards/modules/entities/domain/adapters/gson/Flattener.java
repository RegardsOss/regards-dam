/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.adapters.gson;

import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * GSON adapters utilities
 *
 * @author Marc Sordi
 *
 */
public final class Flattener {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Flattener.class);

    private Flattener() {
    }

    /**
     * Flatten a {@link JsonElement} carrying key and value in separated fields into a single field whose key is the
     * value of the key field and value the value of the value field
     *
     * @param pJsonElement
     *            {@link JsonElement} to flatten
     * @param pKeyName
     *            key name
     * @param pValueName
     *            value name
     * @return flattened {@link JsonElement}
     */
    public static JsonElement flatten(JsonElement pJsonElement, String pKeyName, String pValueName) {
        LOGGER.debug(String.format("Flattening %s", pJsonElement));

        if (!pJsonElement.isJsonObject()) {
            String format = "JSON element must be an object containing 2 members whose names are \"%s\" and \"%s\"";
            String errorMessage = String.format(format, pKeyName, pValueName);
            LOGGER.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        JsonObject current = pJsonElement.getAsJsonObject();

        // Init flattened element
        JsonObject flattened = new JsonObject();
        // Get key : must be a string
        JsonElement key = current.get(pKeyName);
        // Get value
        JsonElement val = current.get(pValueName);

        if (val.isJsonArray()) {
            // Flattening array elements
            JsonObject flattenedObject = new JsonObject();
            Iterator<JsonElement> nestedIter = val.getAsJsonArray().iterator();
            while (nestedIter.hasNext()) {
                JsonObject nested = nestedIter.next().getAsJsonObject();
                for (Map.Entry<String, JsonElement> e : nested.entrySet()) {
                    flattenedObject.add(e.getKey(), e.getValue());
                }
            }
            flattened.add(key.getAsString(), flattenedObject);
        } else {
            flattened.add(key.getAsString(), val);
        }

        LOGGER.debug(String.format("Flattened object : \"%s\" -> \"%s\"", pJsonElement, flattened));

        return flattened;
    }

    public static JsonElement restore(JsonElement pJsonElement, String pKeyName, String pValueName) {
        LOGGER.debug(String.format("Restoring %s", pJsonElement));

        if (!pJsonElement.isJsonObject()) {
            String errorMessage = "JSON element must be an object.";
            LOGGER.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        JsonObject current = pJsonElement.getAsJsonObject();

        // Init restored element
        JsonObject restored = new JsonObject();
        // Restore members
        for (Map.Entry<String, JsonElement> e : current.entrySet()) {
            restored.addProperty(pKeyName, e.getKey());
            JsonElement val = e.getValue();
            if (val.isJsonObject()) {
                // Restoring array but not element structure
                JsonArray restoredArray = new JsonArray();
                for (Map.Entry<String, JsonElement> nestedEntry : val.getAsJsonObject().entrySet()) {
                    JsonObject nestedObject = new JsonObject();
                    nestedObject.add(nestedEntry.getKey(), nestedEntry.getValue());
                    restoredArray.add(nestedObject);
                }
                restored.add(pValueName, restoredArray);
            } else {
                restored.add(pValueName, val);
            }
        }

        LOGGER.debug(String.format("Restored object : \"%s\" -> \"%s\"", pJsonElement, restored));

        return restored;
    }

}
