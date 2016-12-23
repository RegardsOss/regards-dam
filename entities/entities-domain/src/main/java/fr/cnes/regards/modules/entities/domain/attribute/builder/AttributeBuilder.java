/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.attribute.builder;

import java.time.LocalDateTime;
import java.util.Arrays;

import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.BooleanAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DateArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DateAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DateIntervalAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.FloatArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.FloatAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.FloatIntervalAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.GeometryAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.IntegerArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.IntegerAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.IntegerIntervalAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.ObjectAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.StringArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.StringAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.value.Interval;

/**
 *
 * Attribute builder
 *
 * @author Marc Sordi
 *
 */
public final class AttributeBuilder {

    private AttributeBuilder() {
    }

    public static BooleanAttribute buildBoolean(String pName, Boolean pValue) {
        BooleanAttribute att = new BooleanAttribute();
        att.setName(pName);
        att.setValue(pValue);
        return att;
    }

    public static DateArrayAttribute buildDateArray(String pName, LocalDateTime... pLocalDateTimes) {
        DateArrayAttribute att = new DateArrayAttribute();
        att.setName(pName);
        att.setValue(pLocalDateTimes);
        return att;
    }

    public static DateAttribute buildDate(String pName, LocalDateTime pLocalDateTime) {
        DateAttribute att = new DateAttribute();
        att.setName(pName);
        att.setValue(pLocalDateTime);
        return att;
    }

    public static DateIntervalAttribute buildDateInterval(String pName, LocalDateTime pLowerBoundDate,
            LocalDateTime pUpperBoundDate) {
        DateIntervalAttribute att = new DateIntervalAttribute();
        att.setName(pName);
        Interval<LocalDateTime> interval = new Interval<>();
        interval.setLowerBound(pLowerBoundDate);
        interval.setUpperBound(pUpperBoundDate);
        att.setValue(interval);
        return att;
    }

    public static FloatArrayAttribute buildFloatArray(String pName, Double... pValues) {
        FloatArrayAttribute att = new FloatArrayAttribute();
        att.setName(pName);
        att.setValue(pValues);
        return att;
    }

    public static FloatAttribute buildFloat(String pName, Double pValue) {
        FloatAttribute att = new FloatAttribute();
        att.setName(pName);
        att.setValue(pValue);
        return att;
    }

    public static FloatIntervalAttribute buildFloatInterval(String pName, Double pLowerBoundFloat,
            Double pUpperBoundFloat) {
        FloatIntervalAttribute att = new FloatIntervalAttribute();
        att.setName(pName);
        Interval<Double> interval = new Interval<>();
        interval.setLowerBound(pLowerBoundFloat);
        interval.setUpperBound(pUpperBoundFloat);
        att.setValue(interval);
        return att;
    }

    public static GeometryAttribute buildGeometry(String pName, String pGeometry) {
        GeometryAttribute att = new GeometryAttribute();
        att.setName(pName);
        att.setValue(pGeometry);
        return att;
    }

    public static IntegerArrayAttribute buildIntegerArray(String pName, Integer... pValues) {
        IntegerArrayAttribute att = new IntegerArrayAttribute();
        att.setName(pName);
        att.setValue(pValues);
        return att;
    }

    public static IntegerAttribute buildInteger(String pName, Integer pValue) {
        IntegerAttribute att = new IntegerAttribute();
        att.setName(pName);
        att.setValue(pValue);
        return att;
    }

    public static IntegerIntervalAttribute buildIntegerInterval(String pName, Integer pLowerBoundInteger,
            Integer pUpperBoundInteger) {
        IntegerIntervalAttribute att = new IntegerIntervalAttribute();
        att.setName(pName);
        Interval<Integer> interval = new Interval<>();
        interval.setLowerBound(pLowerBoundInteger);
        interval.setUpperBound(pUpperBoundInteger);
        att.setValue(interval);
        return att;
    }

    public static ObjectAttribute buildObject(String pName, AbstractAttribute<?>... pAttributes) {
        ObjectAttribute att = new ObjectAttribute();
        att.setName(pName);
        att.setValue(Arrays.asList(pAttributes));
        return att;
    }

    public static StringArrayAttribute buildStringArray(String pName, String... pValues) {
        StringArrayAttribute att = new StringArrayAttribute();
        att.setName(pName);
        att.setValue(pValues);
        return att;
    }

    public static StringAttribute buildString(String pName, String pValue) {
        StringAttribute att = new StringAttribute();
        att.setName(pName);
        att.setValue(pValue);
        return att;
    }
}
