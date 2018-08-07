package fr.cnes.regards.modules.dam.domain.entities.attribute.adapter;

import java.io.IOException;

import com.google.common.collect.Range;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import fr.cnes.regards.modules.dam.domain.entities.attribute.AbstractAttribute;
import fr.cnes.regards.modules.indexer.domain.IMapping;

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
        extends TypeAdapter<A> {
    @Override
    public void write(JsonWriter out, A value) throws IOException {
        out.beginObject();
        out.name(IMapping.NAME);
        out.value(value.getName());
        out.name(IMapping.VALUE);
        out.beginObject();
        out.name(IMapping.RANGE_LOWER_BOUND);
        this.writeValueLowerBound(out, value);
        out.name(IMapping.RANGE_UPPER_BOUND);
        this.writeValueUpperBound(out, value);
        out.endObject();
        out.endObject();
    }

    protected abstract void writeValueLowerBound(JsonWriter out, AbstractAttribute<Range<T>> value) throws IOException;

    protected abstract void writeValueUpperBound(JsonWriter out, AbstractAttribute<Range<T>> value) throws IOException;

    @Override
    public A read(JsonReader in) throws IOException {
        // read "{"
        in.beginObject();
        // read "name"
        in.nextName();
        // read "value"
        String name = in.nextString();
        // read "value"
        in.nextName();
        // read "{"
        in.beginObject();
        Range<T> range = this.readRangeFromInnerJsonObject(in);
        in.endObject();
        in.endObject();
        return this.createRangeAttribute(name, range);
    }

    protected abstract Range<T> readRangeFromInnerJsonObject(JsonReader in) throws IOException;

    protected abstract A createRangeAttribute(String name, Range<T> range);
}
