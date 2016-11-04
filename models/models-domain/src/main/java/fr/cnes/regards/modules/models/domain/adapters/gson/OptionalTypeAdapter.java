/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.domain.adapters.gson;

import java.io.IOException;
import java.util.Optional;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 *
 * Optional type adapter
 * 
 * @author Marc Sordi
 *
 */
public class OptionalTypeAdapter<T> extends TypeAdapter<Optional<T>> {

    @Override
    public void write(JsonWriter pOut, Optional<T> pValue) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public Optional<T> read(JsonReader pIn) throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

}
