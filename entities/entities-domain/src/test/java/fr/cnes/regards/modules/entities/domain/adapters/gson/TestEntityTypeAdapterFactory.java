/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.adapters.gson;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

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
 * @author Marc Sordi
 *
 */
public class TestEntityTypeAdapterFactory implements TypeAdapterFactory {

    @SuppressWarnings("unchecked")
    @Override
    public <E> TypeAdapter<E> create(Gson pGson, TypeToken<E> pType) {
        final Class<? super E> requestedType = pType.getRawType();
        if (!AbstractEntity.class.isAssignableFrom(requestedType)) {
            return null;
        }

        return (TypeAdapter<E>) customTypeAdapter(pGson, (TypeToken<Car>) pType);
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
    private TypeAdapter<Car> customTypeAdapter(Gson pGson, TypeToken<Car> pType) {
        final TypeAdapter<Car> delegate = pGson.getDelegateAdapter(this, pType);
        final TypeAdapter<JsonElement> elementAdapter = pGson.getAdapter(JsonElement.class);
        return new TypeAdapter<Car>() {

            @Override
            public void write(JsonWriter pOut, Car pValue) throws IOException {
                JsonElement tree = delegate.toJsonTree(pValue);
                beforeWrite(pValue, tree);
                elementAdapter.write(pOut, tree);
            }

            @Override
            public Car read(JsonReader pIn) throws IOException {
                JsonElement tree = elementAdapter.read(pIn);
                afterRead(tree);
                return delegate.fromJsonTree(tree);
            }
        };
    }

    protected void beforeWrite(Car pSource, JsonElement pToSerialize) {
        JsonObject entity = pToSerialize.getAsJsonObject();
        JsonElement attEl = entity.get("attributes");
        if (attEl != null) {
            if (attEl.isJsonArray()) {
                JsonArray attOb = attEl.getAsJsonArray();
                // Flatten element
                JsonObject flatten = new JsonObject();
                Iterator<JsonElement> nestedIter = attOb.iterator();
                while (nestedIter.hasNext()) {
                    JsonObject nested = nestedIter.next().getAsJsonObject();
                    for (Map.Entry<String, JsonElement> e : nested.entrySet()) {
                        flatten.add(e.getKey(), e.getValue());
                    }
                }
                entity.add("attributes", flatten);
            } else {
                // TODO
            }
        }

    }

    protected void afterRead(JsonElement pDeserialized) {
    }

}
