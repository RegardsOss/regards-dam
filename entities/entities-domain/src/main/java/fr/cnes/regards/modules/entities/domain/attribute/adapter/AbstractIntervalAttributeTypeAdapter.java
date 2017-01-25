package fr.cnes.regards.modules.entities.domain.attribute.adapter;

import java.io.IOException;

import com.google.common.collect.Range;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;

/**
 * Adapter abstraction to serialize range attribute following a simplified format.<br/>
 * <pre>
 * {
 *   "name": &lt;attribute name>
 *   "value" {
 *     "lowerBound": &lt;lower bound value>,
 *     "upperBound": &lt;upper bound value>
 *   }
 * }
 * </pre>
 * NB : this format is then changed by FlattenedAttributeAdapterFactory (from entities-service) into
 * <pre>
 * {
 *   "&lt;attribute name>" : {
 *     "lowerBound": &lt;lower bound value>,
 *     "upperBound": &lt;upper bound value>
 *   }
 * }</pre>
 * @param <T> range value type, must be comparable (at least with a super type, like LocalDateTime which is Comparable&lt;ChronoLocalDateTime>)
 * @param <A> real attribute range type (not just AbstractAttribute&lt;Range&lt;T>>
 * @author oroussel
 */
public abstract class AbstractIntervalAttributeTypeAdapter<T extends Comparable<? super T>, A extends AbstractAttribute<Range<T>>>
        extends TypeAdapter<A>

{

    @Override
    public void write(JsonWriter pOut, A pValue) throws IOException {
        pOut.beginObject();
        pOut.name("name");
        pOut.value(pValue.getName());
        pOut.name("value");
        pOut.beginObject();
        pOut.name("lowerBound");
        this.writeValueLowerBound(pOut, pValue);
        pOut.name("upperBound");
        this.writeValueUpperBound(pOut, pValue);
        pOut.endObject();
        pOut.endObject();
    }

    protected abstract void writeValueLowerBound(JsonWriter pOut, AbstractAttribute<Range<T>> pValue)
            throws IOException;

    protected abstract void writeValueUpperBound(JsonWriter pOut, AbstractAttribute<Range<T>> pValue)
            throws IOException;

    @Override
    public A read(JsonReader pIn) throws IOException {
        // read "{"
        pIn.beginObject();
        // read "name"
        pIn.nextName();
        // read "value"
        String name = pIn.nextString();
        // read "value"
        pIn.nextName();
        // read "{"
        pIn.beginObject();
        Range<T> range = this.readRangeFromInnerJsonObject(pIn);
        pIn.endObject();
        pIn.endObject();
        return this.createRangeAttribute(name, range);
    }

    protected abstract Range<T> readRangeFromInnerJsonObject(JsonReader pIn) throws IOException;

    protected abstract A createRangeAttribute(String pName, Range<T> pRange);
}
