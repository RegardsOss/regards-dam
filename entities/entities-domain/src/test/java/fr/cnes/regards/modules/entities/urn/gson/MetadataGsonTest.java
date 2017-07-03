package fr.cnes.regards.modules.entities.urn.gson;

import org.junit.Test;

import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.cnes.regards.framework.gson.adapters.MultimapAdapter;
import fr.cnes.regards.modules.entities.domain.Metadata;

/**
 * Created by oroussel on 30/06/17.
 */
public class MetadataGsonTest {
    private Gson gson = new GsonBuilder().registerTypeAdapter(Multimap.class, new MultimapAdapter()).create();

    @Test
    public void test() {
        Metadata metadata = new Metadata();
        metadata.addGroup("pouet", "ipId1");
        metadata.addGroup("pouet", "ipId2");

        metadata.addGroup("pouet2", "ipId1");
        metadata.addGroup("pouet2", "ipId2");

        metadata.addGroup("pouet3", "ipId1");
        System.out.println(gson.toJson(metadata));

        metadata.addModelId(1, "ipId1");
        System.out.println(gson.toJson(metadata));

        System.out.println(metadata.getModelIds());

        metadata.removeDatasetIpId("ipId1");
        System.out.println(gson.toJson(metadata));

    }
}
