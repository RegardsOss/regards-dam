package fr.cnes.regards.modules.model.domain.adapter;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import fr.cnes.regards.modules.model.domain.Model;

/**
 * Model adapter to be used by AbstractEntity gson serialization.
 * The aim is just to manage name and id properties, not avoid loosing crucial informations when serialize/deserialize
 * entities.
 * @author oroussel
 */
public class ModelAdapter extends TypeAdapter<Model> {

    @Override
    public void write(JsonWriter out, Model model) throws IOException {
        out.beginObject();
        out.name("name").value(model.getName());
        out.name("id").value(model.getId());
        out.endObject();
    }

    @Override
    public Model read(JsonReader in) throws IOException {
        Model model = new Model();
        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "id":
                    model.setId(in.nextLong());
                    break;
                case "name":
                    model.setName(in.nextString());
                    break;
                default:
                    break;
            }
        }
        in.endObject();
        return model;
    }

}
