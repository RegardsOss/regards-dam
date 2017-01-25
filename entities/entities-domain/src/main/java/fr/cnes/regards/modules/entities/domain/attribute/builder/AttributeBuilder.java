/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.domain.attribute.builder;

import java.time.LocalDateTime;
import java.util.Arrays;

import com.google.common.collect.Range;

import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.BooleanAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DateArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DateAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DateIntervalAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DoubleArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DoubleAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.DoubleIntervalAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.GeometryAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.IntegerArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.IntegerAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.IntegerIntervalAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.ObjectAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.StringArrayAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.StringAttribute;

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
        att.setValue(Range.closed(pLowerBoundDate, pUpperBoundDate));
        return att;
    }

    public static DoubleArrayAttribute buildDoubleArray(String pName, Double... pValues) {
        DoubleArrayAttribute att = new DoubleArrayAttribute();
        att.setName(pName);
        att.setValue(pValues);
        return att;
    }

    public static DoubleAttribute buildDouble(String pName, Double pValue) {
        DoubleAttribute att = new DoubleAttribute();
        att.setName(pName);
        att.setValue(pValue);
        return att;
    }

    public static DoubleIntervalAttribute buildDoubleInterval(String pName, Double pLowerBoundDouble,
            Double pUpperBoundDouble) {
        DoubleIntervalAttribute att = new DoubleIntervalAttribute();
        att.setName(pName);
        att.setValue(Range.closed(pLowerBoundDouble, pUpperBoundDouble));
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
        att.setValue(Range.closed(pLowerBoundInteger, pUpperBoundInteger));
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
