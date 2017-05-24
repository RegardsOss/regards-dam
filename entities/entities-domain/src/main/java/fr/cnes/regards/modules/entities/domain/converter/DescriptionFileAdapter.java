package fr.cnes.regards.modules.entities.domain.converter;

import java.io.IOException;

import org.springframework.http.MediaType;

import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import fr.cnes.regards.framework.gson.annotation.GsonTypeAdapter;
import fr.cnes.regards.modules.entities.domain.DescriptionFile;

/**
 * {@link DescriptionFile} GSON adapter allows to skip the content. That especially means that the content is not sent to ES.
 *
 * @author Sylvain VISSIERE-GUERINET
 */
@GsonTypeAdapter(adapted = DescriptionFile.class) // For reading...
public class DescriptionFileAdapter extends TypeAdapter<DescriptionFile> {

    @Override
    public void write(JsonWriter out, DescriptionFile descriptionFile) throws IOException {
        out.beginObject();
        out.name("type");
        MediaType type=descriptionFile.getType();
        out.value(type==null?null:type.toString());
        out.name("url");
        out.value(descriptionFile.getUrl());
        out.endObject();
    }

    @Override
    public DescriptionFile read(JsonReader in) throws IOException {
        DescriptionFile desc=new DescriptionFile();
        in.beginObject();
        while(in.hasNext()) {
            switch (in.nextName()) {
                case "url":
                    desc.setUrl(in.nextString());
                    break;
                case "type":
                    desc.setType(MediaType.valueOf(in.nextString()));
                    break;
                default:
                    throw new JsonParseException("Unexpected field encountered while reading DescriptionFile: "+in.nextName());
            }
        }
        in.endObject();
        return desc;
    }
}
