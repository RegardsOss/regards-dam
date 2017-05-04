package fr.cnes.regards.modules.queryparser.service.queryparser;

import org.apache.lucene.queryparser.flexible.messages.NLS;

/**
 * Flexible Query Parser message bundle class. The messages can be found in
 * <a>resources/RegardsQueryParserMessages.properties</a>
 *
 * Do <b>*NOT*</b> listen to SonarQube about making the fields final and providing accessors.
 * This code implements the correct {@link NLS} API (it is inspired from Lucene classes), wether or not SonarQube is happy about it.
 *
 * @author Xavier-Alexandre Brochard
 */
public class RegardsQueryParserMessages extends NLS {

    private static final String BUNDLE_NAME = RegardsQueryParserMessages.class.getName();

    // static string must match the strings in the property files.
    public static String FIELD_TYPE_UNDETERMINATED; // NOSONAR

    public static String UNSUPPORTED_ATTRIBUTE_TYPE; // NOSONAR

    public static String MIDDLE_WILDCARD_NOT_ALLOWED; // NOSONAR

    public static String RANGE_NUMERIC_CANNOT_BE_EMPTY; // NOSONAR

    private RegardsQueryParserMessages() {
        // Do not instantiate
    }

    static {
        // register all string ids with NLS class and initialize static string
        // values
        NLS.initializeMessages(BUNDLE_NAME, RegardsQueryParserMessages.class);
    }

}
