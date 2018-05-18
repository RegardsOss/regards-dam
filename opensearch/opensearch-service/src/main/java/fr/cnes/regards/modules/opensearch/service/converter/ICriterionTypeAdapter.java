package fr.cnes.regards.modules.opensearch.service.converter;

import java.io.IOException;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import fr.cnes.regards.framework.gson.annotation.GsonTypeAdapterBean;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.opensearch.service.IOpenSearchService;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchParseException;

/**
 * Type adapter reading an OpenSearch query string and converting it to an {@link ICriterion}
 * @author Xavier-Alexandre Brochard
 */
@GsonTypeAdapterBean(adapted = ICriterion.class)
public class ICriterionTypeAdapter extends TypeAdapter<ICriterion> {

    /**
     * The OpenSearch service building {@link ICriterion} from a request string. Autowired by Spring.
     */
    private final IOpenSearchService openSearchService;

    /**
     * Gson. Autowired by Spring.
     */
    private final Gson gson;

    /**
     * @param openSearchService The OpenSearch service building {@link ICriterion} from a request string. Autowired by
     *            Spring. Must not be null.
     * @param gson Gson. Autowired by Spring. Must not be null.
     */
    public ICriterionTypeAdapter(IOpenSearchService openSearchService, Gson gson) {
        super();
        Preconditions.checkNotNull(openSearchService);
        Preconditions.checkNotNull(gson);
        this.openSearchService = openSearchService;
        this.gson = gson;
    }

    @Override
    public ICriterion read(JsonReader in) throws IOException {
        try {
            String query = in.nextString();
            return openSearchService.parse(query);
        } catch (OpenSearchParseException e) {
            throw new JsonSyntaxException("An error occured during the parsing of the OpenSearch query", e);
        }

    }

    @Override
    public void write(JsonWriter out, ICriterion value) throws IOException {
        out.jsonValue(gson.toJson(value));
    }

}
