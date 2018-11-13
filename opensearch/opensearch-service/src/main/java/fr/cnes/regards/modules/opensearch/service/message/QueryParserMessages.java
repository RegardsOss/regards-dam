package fr.cnes.regards.modules.opensearch.service.message;

import org.apache.lucene.queryparser.flexible.messages.NLS;

/**
 * Flexible Query Parser message bundle class. The messages can be found in
 * <a>resources/QueryParserMessages.properties</a>
 *
 * Do <b>*NOT*</b> listen to SonarQube about making the fields final and providing accessors.
 * This code implements the correct {@link NLS} API (it is inspired from Lucene classes), whether or not SonarQube is happy about it.
 *
 * @author Xavier-Alexandre Brochard
 */
public final class QueryParserMessages extends NLS {

    private static final String BUNDLE_NAME = QueryParserMessages.class.getName();

    // static string must match the strings in the property files.
    public static String FIELD_TYPE_UNDETERMINATED; // NOSONAR

    public static String UNSUPPORTED_ATTRIBUTE_TYPE; // NOSONAR

    public static String UNSUPPORTED_ATTRIBUTE_TYPE_FOR_RANGE_QUERY; // NOSONAR

    public static String MIDDLE_WILDCARD_NOT_ALLOWED; // NOSONAR

    public static String RANGE_NUMERIC_CANNOT_BE_EMPTY; // NOSONAR

    public static String INDETERMINATED_RELATIONSHIP; // NOSONAR

    private QueryParserMessages() {
        // Do not instantiate
    }

    static {
        // register all string ids with NLS class and initialize static string
        // values
        NLS.initializeMessages(BUNDLE_NAME, QueryParserMessages.class);
    }

}
